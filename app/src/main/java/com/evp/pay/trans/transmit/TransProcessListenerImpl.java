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
package com.evp.pay.trans.transmit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.ConditionVariable;
import android.view.KeyEvent;

import com.evp.abl.core.AAction;
import com.evp.abl.core.AAction.ActionEndListener;
import com.evp.abl.core.AAction.ActionStartListener;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.convert.IConvert;
import com.evp.view.dialog.CustomAlertDialog;

import java.util.Objects;

/**
 * The type Trans process listener.
 */
public class TransProcessListenerImpl implements TransProcessListener {

    private static final String TAG = "TransProcessListener";

    private Context context;
    private CustomAlertDialog dialog;

    private IConvert convert = ConvertHelper.getConvert();
    private ConditionVariable cv;
    private boolean isShowMessage;
    private String title;
    private int result;
    private CustomAlertDialog cfmDialog;

    /**
     * Instantiates a new Trans process listener.
     *
     * @param context the context
     */
    public TransProcessListenerImpl(Context context) {
        this.context = context;
        this.isShowMessage = true;
    }

    /**
     * Instantiates a new Trans process listener.
     *
     * @param context       the context
     * @param isShowMessage the is show message
     */
    public TransProcessListenerImpl(Context context, boolean isShowMessage) {
        this.context = context;
        this.isShowMessage = isShowMessage;
    }

    private void showDialog(final String message, final int timeout, final int alertType) {
        if (!isShowMessage) {
            return;
        }
        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (dialog == null) {
                    dialog = new CustomAlertDialog(context, alertType);
                    if (ActivityStack.getInstance().top() != context) {
                        dialog.dismiss();
                    } else {
                        dialog.show();
                        dialog.setCancelable(false);
                        if (alertType == CustomAlertDialog.WARN_TYPE) {
                            dialog.setImage(R.drawable.ic16);
                        }
                        dialog.setTimeout(timeout);
                        dialog.setTitleText(title);
                        dialog.setContentText(message);
                    }
                } else {
                    //progress style ,update message and title
                    if (dialog.isShowing()) {
                        dialog.setTimeout(timeout);
                        dialog.setTitleText(title);
                        dialog.setContentText(message);
                    }
                }

            }
        });
    }

    @Override
    public void onShowProgress(final String message, final int timeout) {
        showDialog(message, timeout, CustomAlertDialog.PROGRESS_TYPE);
    }

    @Override
    public void onShowWarning(String message, int timeout) {
        showDialog(message, timeout, CustomAlertDialog.WARN_TYPE);
    }

    private int onShowMessage(final String message, final int timeout, final int alertType, final boolean confirmable) {
        if (!isShowMessage) {
            return -1;
        }
        onHideProgress();
        cv = new ConditionVariable();
        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (cfmDialog == null) {
                    cfmDialog = new CustomAlertDialog(context, alertType, timeout);
                } else {
                    cfmDialog.setTimeout(timeout);
                }
                LogUtils.d(TAG, "run:onShowMessage " + context);
                LogUtils.d(TAG, "current context " + ActivityStack.getInstance().top());
                if (alertType == CustomAlertDialog.ERROR_TYPE && FinancialApplication.getCurrentETransType() != null) {
                    cfmDialog.setTitleText(FinancialApplication.getCurrentETransType().getTransName());
                }
                cfmDialog.setContentText(message);
                if (context == ActivityStack.getInstance().top()) {
                    cfmDialog.show();
                    cfmDialog.showConfirmButton(confirmable);
                    cfmDialog.setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface arg0) {
                            cv.open();
                        }
                    });
                } else {
                    cfmDialog.dismiss();
                }

            }
        });

        cv.block();
        return 0;
    }

    private void onShowRemoveMessage(final String message, final int timeout, final int alertType, final boolean confirmable) {
        LogUtils.i("onShowRemoveMessage", "message:" + message);
        if (!isShowMessage) {
            return;
        }
        onHideProgress();
        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (cfmDialog == null) {
                    cfmDialog = new CustomAlertDialog(context, alertType, timeout);
                }
                LogUtils.d(TAG, "run:onShowMessage " + context);
                LogUtils.d(TAG, "current context " + ActivityStack.getInstance().top());

                cfmDialog.setContentText(message);
                if (context == ActivityStack.getInstance().top()) {
                    cfmDialog.show();
                    cfmDialog.showConfirmButton(confirmable);
                    cfmDialog.setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface arg0) {
                        }
                    });
                } else {
                    cfmDialog.dismiss();
                }

            }
        });
    }

    @Override
    public int onShowNormalMessage(final String message, final int timeout, boolean confirmable) {
        return onShowMessage(message, timeout, CustomAlertDialog.NORMAL_TYPE, confirmable);
    }

    @Override
    public int onShowErrMessage(final String message, final int timeout, boolean confirmable) {
        Device.beepErr();
        return onShowMessage(message, timeout, CustomAlertDialog.ERROR_TYPE, confirmable);
    }

    @Override
    public int onShowErrMessage(final String title, final String message, final int timeout, final boolean confirmable) {
        onHideProgress();
        cv = new ConditionVariable();
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, true, timeout);
                dialog.setTitleText(title);
                dialog.setContentText(message);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
                dialog.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        cv.open();
                    }
                });
            }
        });

        cv.block();
        return 0;
    }

    @Override
    public void onHideProgress() {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
    }

    @Override
    public void onHideMessage() {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cfmDialog != null && cfmDialog.isShowing()) {
                    cfmDialog.dismiss();
                    cfmDialog = null;
                }
            }
        });
    }

    @Override
    public void onUpdateProgressTitle(String title) {
        if (!isShowMessage) {
            return;
        }
        this.title = title;
    }

    @Override
    public int onInputOnlinePin(final TransData transData) {
        cv = new ConditionVariable();
        result = 0;
        final ETransType eTransType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        final String totalAmount = Objects.requireNonNull(eTransType).isSymbolNegative() ? "-" + transData.getAmount() : transData.getAmount();
        final String tipAmount = eTransType.isSymbolNegative() ? null : transData.getTipAmount();

        ActionEnterPin actionEnterPin = new ActionEnterPin(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(context,
                        eTransType.getTransName(), transData.getPan(), true,
                        context.getString(R.string.prompt_pin),
                        context.getString(R.string.prompt_no_pin),
                        totalAmount, tipAmount,
                        ActionEnterPin.EEnterPinType.ONLINE_PIN,
                        KeyUtils.getTpkIndex(transData.getAcquirer().getTleKeySetId()));

            }
        });

        actionEnterPin.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult actionResult) {
                int ret = actionResult.getRet();
                if (ret == TransResult.SUCC) {
                    String data = (String) actionResult.getData();
                    transData.setPin(data);
                    if (data != null && !data.isEmpty()) {
                        transData.setHasPin(true);
                    } else {
                        transData.setHasPin(false);
                    }
                    result = 0;
                    cv.open();
                } else {
                    result = -1;
                    cv.open();
                }
                ActivityStack.getInstance().pop();
            }
        });
        actionEnterPin.execute();

        cv.block();
        return result;
    }

    @Override
    public void onShowRemoveMessage(String message, int timeout, boolean conformable) {
        onShowRemoveMessage(message, timeout, CustomAlertDialog.NORMAL_TYPE, conformable);
    }
}
