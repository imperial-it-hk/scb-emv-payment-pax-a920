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
 * 20190108  	         lixc                    Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigUtils;
import com.evp.eventbus.OnPrintEvent;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.sankuai.waimai.router.Router;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

/**
 * The type Print preview activity.
 */
public class PrintPreviewActivity extends BaseActivityWithTickForAction {
    private Bitmap bitmap;
    private LinearLayout linPrintPreview;
    private Button btnCancel;
    private Button btnPrint;
    private PrintType printType;
    private boolean isReprint = false;
    private Disposable disposable;
    private boolean enablePaperless = true;
    /**
     * The constant CANCEL_BUTTON.
     */
    public static final String CANCEL_BUTTON = "CANCEL";
    /**
     * The constant PRINT_BUTTON.
     */
    public static final String PRINT_BUTTON = "PRINT";
    /**
     * The constant SMS_BUTTON.
     */
    public static final String SMS_BUTTON = "SMS";
    /**
     * The constant EMAIL_BUTTON.
     */
    public static final String EMAIL_BUTTON = "EMAIL";

    private Animation receiptOutAnim;

    private TransData transData;

    private TransTotal transTotal;

    private List<TransData> transDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void loadParam() {
        //get data
        Intent intent = getIntent();
        if (null != intent) {
            transData = (TransData) intent.getSerializableExtra(EUIParamKeys.TRANSDATA.toString());
            printType = (PrintType) intent.getSerializableExtra(EUIParamKeys.PRINT_TYPE.toString());
            isReprint = (boolean) intent.getSerializableExtra(EUIParamKeys.IS_REPRINT.toString());
            if (null == transData) {
                LogUtils.e("PrintPreviewActivity", "parameter pass error: transdata");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.paperless_action, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_print_preview_layout;
    }


    @Override
    protected String getTitleString() {
        return getString(R.string.receipt_preview);
    }

    @Override
    protected void initViews() {

        enableBackAction(false);
        linPrintPreview = findViewById(R.id.print_preview);
        if (printType == PrintType.SUMMARY_REPORT) {
            switch (transData.getAcquirer().getName()) {
                case AppConstants.QR_ACQUIRER:
                    List<String> list = new ArrayList<>();
                    list.add(ETransType.SALE.name());
                    list.add(ETransType.VOID.name());
                    list.add(ETransType.REFUND.name());
                    List<TransData.ETransStatus> filter = new ArrayList<>();
                    filter.add(TransData.ETransStatus.VOIDED);
                    filter.add(TransData.ETransStatus.SUSPENDED);
                    transDataList = GreendaoHelper.getTransDataHelper().findTransData(list, filter, transData.getAcquirer());
                    if (transDataList != null && transDataList.size() != 0) {
                        View receiptView = Router.callMethod(ReceiptConst.SUMMARY_REPORT, FinancialApplication.getApp(), transDataList, ConfigUtils.getInstance());
                        linPrintPreview.addView(receiptView);
                    }
                    break;
                default:
                    transTotal = GreendaoHelper.getTransTotalHelper().calcTotal(transData.getAcquirer());
                    if (!transTotal.isZero()) {
                        View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSTOTAL, FinancialApplication.getApp(), transTotal,
                                getString(R.string.report_summary_report), ConfigUtils.getInstance());
                        linPrintPreview.addView(receiptView);
                    }
                    break;
            }
        } else if (printType == PrintType.AUDIT_REPORT) {
            List<String> list = new ArrayList<>();
            list.add(ETransType.SALE.name());
            list.add(ETransType.VOID.name());
            list.add(ETransType.REFUND.name());
            //region IPP OLS
            list.add(ETransType.INSTALLMENT.name());
            list.add(ETransType.REDEEM.name());
            //endregion
            List<TransData.ETransStatus> filter = new ArrayList<>();
            filter.add(TransData.ETransStatus.VOIDED);
            filter.add(TransData.ETransStatus.SUSPENDED);
            transDataList = GreendaoHelper.getTransDataHelper().findTransData(list, filter, transData.getAcquirer());
            if (transDataList != null && transDataList.size() != 0) {
                switch (transData.getAcquirer().getName()) {
                    case AppConstants.QR_ACQUIRER: {
                        View receiptView = Router.callMethod(ReceiptConst.AUDIT_REPORT, FinancialApplication.getApp(), transDataList, ConfigUtils.getInstance());
                        linPrintPreview.addView(receiptView);
                        break;
                    }
                    default: {
                        View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSLIST, FinancialApplication.getApp(), transDataList,
                                getString(R.string.report_audit_report), ConfigUtils.getInstance());
                        linPrintPreview.addView(receiptView);
                        break;
                    }
                }
            }
        } else if (printType == PrintType.LAST_SETTLEMENT) {
            transTotal = GreendaoHelper.getTransTotalHelper().findLastTransTotal(transData.getAcquirer(), true);
            if (transTotal != null) {
                switch (transData.getAcquirer().getName()) {
                    case AppConstants.QR_ACQUIRER: {
                        View receiptView = Router.callMethod(ReceiptConst.SETTLEMENT_RECEIPT, FinancialApplication.getApp(), transTotal, ConfigUtils.getInstance());
                        linPrintPreview.addView(receiptView);
                    }
                    break;
                    default:
                        if (!transTotal.isZero()) {
                            View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSTOTAL, FinancialApplication.getApp(), transTotal,
                                    getString(R.string.report_summary_report), ConfigUtils.getInstance());
                            linPrintPreview.addView(receiptView);
                        }
                        break;
                }
            } else {
                finish(new ActionResult(TransResult.ERR_NO_TRANS, null));
            }
        } else {
            int copy = 0;
            if (printType == PrintType.RECEIPT_CUSTOMER) {
                TextView tv = findViewById(R.id.tv_print_customer_copy);
                tv.setText(ConfigUtils.getInstance().getString("print_customer_copy"));
                tv.setVisibility(View.VISIBLE);
                copy = 1;
            }
            View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSDETAIL, getApplicationContext(), transData, isReprint, copy, ConfigUtils.getInstance());
            linPrintPreview.addView(receiptView);
        }

        btnCancel = (Button) findViewById(R.id.cancel_button);
        btnCancel.setText(ConfigUtils.getInstance().getString("buttonCancel"));

        btnPrint = (Button) findViewById(R.id.print_button);
        btnPrint.setText(ConfigUtils.getInstance().getString("buttonPrint"));

        enablePaperless = SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_PAPERLESS);
        receiptOutAnim = AnimationUtils.loadAnimation(this, R.anim.receipt_out);

        if (enablePaperless) {
            disposable = Utils.callPermission(PrintPreviewActivity.this, Manifest.permission.SEND_SMS, new Action() {
                @Override
                public void run() throws Exception {
                    LogUtils.e(TAG, "{run}");
                }
            }, getString(R.string.permission_rationale_sms));
        }


        if (printType == PrintType.LAST_SETTLEMENT) {
        } else if (transDataList != null && transDataList.size() == 0) {
            finish(new ActionResult(TransResult.ERR_NO_TRANS, null));
        } else if (transTotal != null && transTotal.isZero()) {
            finish(new ActionResult(TransResult.ERR_NO_TRANS, null));
        }
    }

    @Override
    protected void setListeners() {
        btnCancel.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {
        LogUtils.i("On Click", v.toString());
        switch (v.getId()) {
            case R.id.cancel_button:
                btnPrint.setClickable(false); //AET-200
                btnCancel.setClickable(false); //AET-240
                //end trans
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, CANCEL_BUTTON));
                break;
            case R.id.print_button:
                btnPrint.setClickable(false); //AET-240
                btnCancel.setClickable(false); //AET-200
                //print
                finish(new ActionResult(TransResult.SUCC, PRINT_BUTTON));
                break;
            default:
                break;
        }

    }

    /**
     * On print.
     *
     * @param event the event
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPrint(OnPrintEvent event) {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != linPrintPreview) {
                    linPrintPreview.startAnimation(receiptOutAnim);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //cannot be here
                return true;
            case R.id.dialog_sms:
                if (enablePaperless && Utils.isSMSAvailable(PrintPreviewActivity.this)) {
                    finish(new ActionResult(TransResult.SUCC, SMS_BUTTON));
                } else {
                    ToastUtils.showMessage(R.string.err_unsupported_func);
                }
                return true;
            case R.id.dialog_email:
                if (enablePaperless && Utils.isNetworkAvailable(PrintPreviewActivity.this)) {
                    finish(new ActionResult(TransResult.SUCC, EMAIL_BUTTON));
                } else {
                    ToastUtils.showMessage(R.string.err_unsupported_func);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // AET-102
    @Override
    protected void onTimerFinish() {
        quickClickProtection.stop();
        btnPrint.performClick();
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (null != linPrintPreview) {
            linPrintPreview.clearAnimation();
            receiptOutAnim.cancel();
            receiptOutAnim = null;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnPrint.setClickable(true);
        btnCancel.setClickable(true);
        btnPrint.requestFocus();
    }
}
