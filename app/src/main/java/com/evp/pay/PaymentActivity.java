/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;

import com.evp.abl.core.AAction;
import com.evp.abl.core.AAction.ActionEndListener;
import com.evp.abl.core.AAction.ActionStartListener;
import com.evp.abl.core.ATransaction.TransEndListener;
import com.evp.abl.core.ActionResult;
import com.evp.abl.utils.EncUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.service.ParseResp;
import com.evp.pay.trans.PreAuthTrans;
import com.evp.pay.trans.RefundTrans;
import com.evp.pay.trans.SaleTrans;
import com.evp.pay.trans.SaleVoidTrans;
import com.evp.pay.trans.SettleTrans;
import com.evp.pay.trans.TransContext;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionUpdateParam;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.Controller;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.unifiedsdk.message.BaseRequest;
import com.pax.unifiedsdk.message.BaseResponse;
import com.pax.unifiedsdk.message.MessageUtils;
import com.pax.unifiedsdk.message.PreAuthMsg;
import com.pax.unifiedsdk.message.PrintBitmapMsg;
import com.pax.unifiedsdk.message.RefundMsg;
import com.pax.unifiedsdk.message.ReprintTotalMsg;
import com.pax.unifiedsdk.message.ReprintTransMsg;
import com.pax.unifiedsdk.message.SaleMsg;
import com.pax.unifiedsdk.message.TransResponse;
import com.pax.unifiedsdk.message.VoidMsg;
import com.pax.unifiedsdk.sdkconstants.SdkConstants;

/**
 * The type Payment activity.
 */
public class PaymentActivity extends Activity {

    /**
     * The constant REQ_SELF_TEST.
     */
    public static final int REQ_SELF_TEST = 1;

    private static final String TAG = "EDC PAYMENT";
    /**
     * The constant TAG_EXIT.
     */
    public static final String TAG_EXIT = "EXIT";

    private boolean needSelfTest = true;

    private int commandType;
    private BaseRequest request = null;

    private TransEndListener endListener = new TransEndListener() {

        @Override
        public void onEnd(ActionResult result) {
            transFinish(result);
        }
    };

    private TransEndListener settleEndListener = new TransEndListener() {

        @Override
        public void onEnd(final ActionResult result) {
            if (result.getRet() != TransResult.SUCC) {
                transFinish(result);
                return;
            }

            ActionUpdateParam actionUpdateParam = new ActionUpdateParam(new ActionStartListener() {
                @Override
                public void onStart(AAction action) {
                    ((ActionUpdateParam) action).setParam(ActivityStack.getInstance().top(), false);
                }
            });
            actionUpdateParam.setEndListener(new ActionEndListener() {
                @Override
                public void onEnd(AAction action, ActionResult result1) {
                    transFinish(result);
                }
            });
            actionUpdateParam.execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_null);
        super.onCreate(savedInstanceState);
        onCheckArgs();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onExit(intent);
    }

    private boolean onExit(Intent intent) {
        if (intent != null) {
            boolean isExit = intent.getBooleanExtra(TAG_EXIT, false);
            if (isExit) {
                transFinish(new ActionResult(TransResult.ERR_ABORTED, null));
                return true;
            }
        }
        return false;
    }

    private void onCheckArgs() {
        if (getIntent() != null) {
            if (onExit(getIntent())) {
                return;
            }
            request = MessageUtils.generateRequest(getIntent());
            if (request == null || !MessageUtils.checkArgs(request)) {
                transFinish(new ActionResult(TransResult.ERR_PARAM, null));
                return;
            }
            commandType = MessageUtils.getType(request);

            if (needSelfTest && FinancialApplication.getController().isFirstRun()) {
                SelfTestActivity.onSelfTest(PaymentActivity.this, REQ_SELF_TEST);
            } else {
                doTrans();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityStack.getInstance().popTo(this);
        //FinancialApplication.getSysParam().init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_SELF_TEST:
                if (resultCode == RESULT_CANCELED) {
                    transFinish(new ActionResult(TransResult.ERR_ABORTED, null));
                    return;
                }
                onHandleSelfTest();
                break;
            case SdkConstants.REQUEST_CODE:
                transFinish(new ActionResult(TransResult.SUCC, null));
                return;
            default:
                finish();
                break;
        }
    }

    private void onHandleSelfTest() {
        needSelfTest = false;
        Controller controller = FinancialApplication.getController();
        int cnt = 0;
        while (controller.get(Controller.NEED_DOWN_AID) == Controller.Constant.YES && controller.get(Controller.NEED_DOWN_CAPK) == Controller.Constant.YES) {
            SystemClock.sleep(500);
            ++cnt;
            if (cnt > 3) {
                transFinish(new ActionResult(TransResult.ERR_PARAM, null));
                return;
            }
        }
        doTrans();
    }

    private void doTrans() {
        ActivityStack.getInstance().popTo(this);
        try {
            switch (commandType) {
                case SdkConstants.PRE_AUTH:
                    startAuth((PreAuthMsg.Request) request);
                    break;
                case SdkConstants.SALE:
                    startSale((SaleMsg.Request) request);
                    break;
                case SdkConstants.VOID:
                    startVoid((VoidMsg.Request) request);
                    break;
                case SdkConstants.REFUND:
                    startRefund((RefundMsg.Request) request);
                    break;
                case SdkConstants.SETTLE:
                    startSettle();
                    break;
                case SdkConstants.REPRINT_TRANS:
                    startReprintTrans((ReprintTransMsg.Request) request);
                    break;
                case SdkConstants.REPRINT_TOTAL:
                    startReprintTotal((ReprintTotalMsg.Request) request);
                    break;
                case SdkConstants.PRINT_BITMAP:
                    startPrintBitmap((PrintBitmapMsg.Request) request);
                    break;
                default:
                    throw new Exception("wrong processing");
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
            transFinish(new ActionResult(TransResult.ERR_PARAM, null));
        }

    }

    private void transFinish(final ActionResult result) {
        Intent intent = new Intent();
        BaseResponse response = ParseResp.generate(commandType, result);
        boolean isTransResponse = response instanceof TransResponse;
        if (response != null) {
            if (isTransResponse){
                intent.putExtras(MessageUtils.toBundle((TransResponse)response, new Bundle()));
            }else {
                intent.putExtras(MessageUtils.toBundle(response, new Bundle()));
            }
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        ActivityStack.getInstance().popAll(); //finish
        TransContext.getInstance().setCurrentAction(null);
        Device.enableStatusBar(true);
        Device.enableHomeRecentKey(true);
    }

    /**
     * sale
     *
     * @param requestData
     */
    private void startSale(SaleMsg.Request requestData) {
        new SaleTrans(PaymentActivity.this, Long.toString(requestData.getAmount()), Long.toString(requestData.getTipAmount()), (byte) -1, true,
                endListener).setIsFromThirdParty(true).execute();
    }

    /**
     * void
     *
     * @param requestData
     */
    private void startVoid(VoidMsg.Request requestData) {
        int voucherNo = requestData.getVoucherNo();
        if (voucherNo <= 0) {
            new SaleVoidTrans(PaymentActivity.this, endListener).setIsFromThirdParty(true).execute();
        } else {
            new SaleVoidTrans(PaymentActivity.this, Component.getPaddedNumber(voucherNo, 6), endListener).setIsFromThirdParty(true).execute();
        }
    }

    /**
     * refund
     *
     * @param requestData
     */
    private void startRefund(RefundMsg.Request requestData) {
        long amount = requestData.getAmount();
        if (amount <= 0) {// enter amount
            new RefundTrans(PaymentActivity.this, endListener).setIsFromThirdParty(true).execute();
        } else {
            new RefundTrans(PaymentActivity.this, Long.toString(amount), endListener).setIsFromThirdParty(true).execute();
        }
    }

    /**
     * pre-authorization
     *
     * @param requestData
     */
    private void startAuth(PreAuthMsg.Request requestData) {
        new PreAuthTrans(PaymentActivity.this, Long.toString(requestData.getAmount()), endListener).setIsFromThirdParty(true).execute();
    }

    /**
     * settle
     */
    private void startSettle() {
        new SettleTrans(PaymentActivity.this, settleEndListener).setIsFromThirdParty(true).execute();
    }

    /**
     * reprint the last transaction, or reprint the transaction by transNo
     *
     * @param requestData
     */
    private void startReprintTrans(ReprintTransMsg.Request requestData) {
        if (requestData.getVoucherNo() <= 0) {
            startPrnLast();
        } else {
            startPrnTransByTransNo(requestData.getVoucherNo());
        }
    }

    /**
     * print last transaction
     */
    private void startPrnLast() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                int result = PrinterUtils.printLastTrans(PaymentActivity.this);
                transFinish(new ActionResult(result, null));
            }
        });
    }

    private void startPrnTransByTransNo(int transNo) {
        final TransData transData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(transNo);

        if (transData == null) {
            transFinish(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
        }
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                PrinterUtils.printTransAgain(PaymentActivity.this,transData);
                transFinish(new ActionResult(TransResult.SUCC, null));
            }
        });
    }

    private void startReprintTotal(ReprintTotalMsg.Request requestData) {
        switch (requestData.getReprintType()) {
            case ReprintTotalMsg.Request.SUMMARY:
                doPrnTotal();
                break;
            case ReprintTotalMsg.Request.DETAIL:
                doPrnDetail();
                break;
            case ReprintTotalMsg.Request.LAST_SETTLE:
                doPrnLastBatch();
                break;
            default:
                break;
        }
    }

    private void startPrintBitmap(final PrintBitmapMsg.Request requestData) {
        if (requestData.getBitmap() != null) {
            FinancialApplication.getApp().runInBackground(new Runnable() {
                @Override
                public void run() {
                    PrinterUtils.printBitmapStr(requestData.getBitmap(),PaymentActivity.this);
                    transFinish(new ActionResult(TransResult.SUCC, null));
                }
            });
        } else {
            transFinish(new ActionResult(TransResult.ERR_PARAM, null));
        }
    }

    /**
     * print detail
     */
    private void doPrnDetail() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                int result = PrinterUtils.printHistoryTransList(PaymentActivity.this, FinancialApplication.getAcqManager().getCurAcq());
                transFinish(new ActionResult(result, null));
            }
        });
    }

    /**
     * print total
     */
    private void doPrnTotal() {
        //FIXME may have bug the getCurAcq
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                PrinterUtils.printHistoryTransTotal(PaymentActivity.this, FinancialApplication.getAcqManager().getCurAcq());
                transFinish(new ActionResult(TransResult.SUCC, null));
            }
        });
    }

    /**
     * print last batch
     */
    private void doPrnLastBatch() {
       FinancialApplication.getApp().runInBackground(new Runnable() {
           @Override
           public void run() {
               int result = PrinterUtils.printLastTransTotal(PaymentActivity.this, null);
               transFinish(new ActionResult(result, null));
           }
       });
    }

    /**
     * terminal setting
     */
    private void doSetting() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(PaymentActivity.this, 8,
                        getString(R.string.prompt_sys_pwd), null);
            }
        });

        inputPasswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    transFinish(result);
                    return;
                }

                String data = EncUtils.sha1((String) result.getData());
                if (!data.equals(SysParam.getInstance().getString(R.string.SEC_SYS_PWD))) {
                    transFinish(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }

                Intent intent = new Intent(PaymentActivity.this, ConfigFirstActivity.class);
                startActivityForResult(intent, SdkConstants.REQUEST_CODE);
            }
        });

        inputPasswordAction.execute();
    }
}
