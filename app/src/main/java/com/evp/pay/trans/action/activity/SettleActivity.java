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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */

package com.evp.pay.trans.action.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.view.KeyEvent;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.mvp.contract.SettleContract;
import com.evp.mvp.presenter.SettlePresenter;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.record.PrinterUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.evp.view.dialog.CustomAlertDialog;
import com.evp.view.dialog.DialogUtils;

import java.util.ArrayList;

/**
 * The type Settle activity.
 */
public class SettleActivity extends BaseActivityWithTickForAction implements SettleContract.View {
    private SettlePresenter presenter;
    private String navTitle;
    private Boolean navBack = false;
    private boolean thisIsAutoSettle;
    private TextView acquirerName;
    private TextView merchantName;
    private TextView merchantId;
    private TextView terminalId;
    private TextView batchNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new SettlePresenter(this);
        presenter.attachView(this);
        super.onCreate(savedInstanceState);
        tickTimer.stop();
        presenter.doSettlement(thisIsAutoSettle);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settle_layout;
    }

    @Override
    protected void loadParam() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ArrayList<String> selectAcqs = extras.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
            presenter.init(selectAcqs);
        }
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        thisIsAutoSettle = getIntent().getBooleanExtra(EUIParamKeys.AUTO_SETTLE.toString(), false);
    }

    @Override
    protected void initViews() {
        acquirerName = (TextView) findViewById(R.id.settle_acquirer_name);
        merchantName = (TextView) findViewById(R.id.settle_merchant_name);
        merchantId = (TextView) findViewById(R.id.settle_merchant_id);
        terminalId = (TextView) findViewById(R.id.settle_terminal_id);
        batchNo = (TextView) findViewById(R.id.settle_batch_num);

        presenter.setCurrAcquirerContent(presenter.selectAcqs.get(0));
    }

    @Override
    protected void setListeners() {
        enableBackAction(navBack);
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void setCurrAcquirerContent(String acquirerName, Acquirer acquirer, String saleAmt, String refundAmt, String voidSaleAmt, String voidRefundAmt, String offlineAmt, TransTotal total) {
        this.acquirerName.setText(acquirerName);
        //AET-39
        merchantName.setText(SysParam.getInstance().getString(R.string.EDC_MERCHANT_NAME_EN));
        merchantId.setText(acquirer.getMerchantId());
        terminalId.setText(acquirer.getTerminalId());
        batchNo.setText(String.valueOf(acquirer.getCurrBatchNo()));

        ((TextView) findViewById(R.id.sale_total_sum)).setText(String.valueOf(total.getSaleTotalNum()));
        ((TextView) findViewById(R.id.sale_total_amount)).setText(saleAmt);
        ((TextView) findViewById(R.id.refund_total_sum)).setText(String.valueOf(total.getRefundTotalNum()));
        ((TextView) findViewById(R.id.refund_total_amount)).setText(refundAmt);

        ((TextView) findViewById(R.id.void_sale_total_sum)).setText(String.valueOf(total.getSaleVoidTotalNum()));
        ((TextView) findViewById(R.id.void_sale_total_amount)).setText(voidSaleAmt);
        ((TextView) findViewById(R.id.void_refund_total_sum)).setText(String.valueOf(total.getRefundVoidTotalNum()));
        ((TextView) findViewById(R.id.void_refund_total_amount)).setText(voidRefundAmt);
        ((TextView) findViewById(R.id.offline_total_sum)).setText(String.valueOf(total.getOfflineTotalNum()));
        ((TextView) findViewById(R.id.offline_total_amount)).setText(offlineAmt);

    }

    @Override
    public void finish(ActionResult result) {
        super.finish(result);
    }

    @Override
    public void printDetail(Boolean isFailDetail) {
        ConditionVariable cv2 = new ConditionVariable();
        FinancialApplication.getApp().runOnUiThread(new PrintDetailRunnable(isFailDetail, cv2));
        cv2.block();
    }

    @Override
    protected boolean onKeyBackDown() {
        if(!thisIsAutoSettle) {
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        }
        return true;
    }

    @Override
    protected boolean onKeyDel() {
        return onKeyBackDown();
    }

    private class PrintDetailRunnable implements Runnable {
        /**
         * The Cv.
         */
        final ConditionVariable cv;
        /**
         * The Is fail detail.
         */
        final boolean isFailDetail;

        /**
         * Instantiates a new Print detail runnable.
         *
         * @param isFailDetail the is fail detail
         * @param cv           the cv
         */
        PrintDetailRunnable(boolean isFailDetail, ConditionVariable cv) {
            this.isFailDetail = isFailDetail;
            this.cv = cv;
        }

        @Override
        public void run() {
            final CustomAlertDialog dialog = new CustomAlertDialog(SettleActivity.this,
                    CustomAlertDialog.IMAGE_TYPE);
            String info = getString(R.string.settle_print_detail_or_not);
            if (isFailDetail) {
                info = getString(R.string.settle_print_fail_detail_or_not);
            }
            //AET-76
            dialog.setTimeout(30);
            dialog.setContentText(info);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() { // AET-77
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DEL) {
                        dialog.dismiss();
                        cv.open();
                        return true;
                    }
                    return false;
                }
            });
            dialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
                @Override
                public void onClick(CustomAlertDialog alertDialog) {
                    dialog.dismiss();
                    cv.open();
                }
            });
            dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
                @Override
                public void onClick(CustomAlertDialog alertDialog) {
                    dialog.dismiss();
                    FinancialApplication.getApp().runInBackground(new Runnable() {

                        @Override
                        public void run() {

                            int result;
                            if (isFailDetail) {
                                // 打印失败交易明细
                                result = PrinterUtils.printFailDetail(SettleActivity.this);
                            } else {
                                // 打印交易明细
                                result = PrinterUtils.printSettleTransList(SettleActivity.this, presenter.acquirer);
                            }
                            if (result != TransResult.SUCC) {
                                DialogUtils.showErrMessage(SettleActivity.this,
                                        getString(R.string.transType_print),
                                        getString(isFailDetail ?
                                                R.string.err_no_failed_trans :
                                                R.string.err_no_succ_trans), new DialogInterface.OnDismissListener() {

                                            @Override
                                            public void onDismiss(DialogInterface arg0) {
                                                cv.open();
                                            }
                                        }, Constants.FAILED_DIALOG_SHOW_TIME);

                            } else {
                                cv.open();
                            }

                        }
                    });

                }
            });

            dialog.show();
            dialog.setImage(R.drawable.ic19);
        }
    }
}
