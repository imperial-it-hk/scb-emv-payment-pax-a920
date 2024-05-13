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
package com.evp.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.view.KeyEvent;

import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.pay.PaymentActivity;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.SettleTrans;
import com.evp.payment.evpscb.R;
import com.evp.view.dialog.CustomAlertDialog.OnCustomClickListener;

/**
 * The type Dialog utils.
 */
public class DialogUtils {
    /**
     * 提示错误信息
     *
     * @param context  the context
     * @param title    the title
     * @param msg      the msg
     * @param listener the listener
     * @param timeout  the timeout
     */
    public static void showErrMessage(final Context context, final String title, final String msg,
                                      final OnDismissListener listener, final int timeout) {
        if (context == null) {
            return;
        }

        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, true, timeout);
                if (context == ActivityStack.getInstance().top()) {
                    dialog.setTitleText(title);
                    dialog.setContentText(msg);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            return keyCode == KeyEvent.KEYCODE_BACK;
                        }
                    });
                    dialog.setOnDismissListener(listener);
                    Device.beepErr();
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * Show processing message custom alert dialog.
     *
     * @param context the context
     * @param title   the title
     * @param timeout the timeout
     * @return the custom alert dialog
     */
    public static CustomAlertDialog showProcessingMessage(final Context context, final String title, final int timeout) {
        if (context == null) {
            return null;
        }
        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.PROGRESS_TYPE);

        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.showContentText(false);
                dialog.setTitleText(title);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setTimeout(timeout);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
            }
        });
        return dialog;
    }

    /**
     * 单行提示成功信息
     *
     * @param context  the context
     * @param title    the title
     * @param listener the listener
     * @param timeout  the timeout
     */
    public static void showSuccMessage(final Context context, final String title,
                                       final OnDismissListener listener, final int timeout) {
        if (context == null) {
            return;
        }
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.SUCCESS_TYPE, true, timeout);
                if (title != null) {
                    dialog.setTitleText(title);
                } else if (FinancialApplication.getCurrentETransType() != null) {
                    dialog.setTitleText(FinancialApplication.getCurrentETransType().getTransName());
                }
                dialog.setContentText(ConfigUtils.getInstance().getString("transactionSuccess"));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
                dialog.setOnDismissListener(listener);
                Device.beepOk();
            }
        });
    }

    public static void showMessage(final Context context, final String msg,
                                   final OnDismissListener listener, final int timeout) {
        if (context == null) {
            return;
        }
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.SUCCESS_TYPE, true, timeout);
                dialog.setContentText(msg);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
                dialog.setOnDismissListener(listener);
                Device.beepOk();
            }
        });
    }

    /**
     * 退出当前应用
     *
     * @param context the context
     */
    public static void showExitAppDialog(final Context context) {
        showConfirmDialog(context, context.getString(R.string.exit_app), null, new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                alertDialog.dismiss();
                Device.enableStatusBar(true);
                Device.enableHomeRecentKey(true);
                Intent intent = new Intent(context, PaymentActivity.class);
                intent.putExtra(PaymentActivity.TAG_EXIT, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(intent);
            }
        });
    }

    /**
     * Show confirm dialog.
     *
     * @param context              the context
     * @param content              the content
     * @param cancelClickListener  the cancel click listener
     * @param confirmClickListener the confirm click listener
     */
    public static void showConfirmDialog(final Context context, final String content,
                                         final OnCustomClickListener cancelClickListener, final OnCustomClickListener confirmClickListener) {
        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);

        final OnCustomClickListener clickListener = new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                alertDialog.dismiss();
            }
        };

        dialog.setCancelClickListener(cancelClickListener == null ? clickListener : cancelClickListener);
        dialog.setConfirmClickListener(confirmClickListener == null ? clickListener : confirmClickListener);
        dialog.show();
        dialog.setNormalText(content);
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }

    /**
     * 应用更新或者参数更新提示，点击确定则进行直接结算
     *
     * @param context the context
     * @param prompt  the prompt
     */
    public static void showUpdateDialog(final Context context, final String prompt) {

        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
        dialog.setCancelClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
            }
        });
        dialog.setConfirmClickListener(new OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                new SettleTrans(context, null).execute();
            }
        });
        dialog.show();
        dialog.setNormalText(prompt);
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }
}
