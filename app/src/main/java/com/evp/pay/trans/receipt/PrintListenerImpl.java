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
package com.evp.pay.trans.receipt;

import android.content.Context;
import android.content.DialogInterface;
import android.os.ConditionVariable;
import android.view.KeyEvent;

import com.evp.pay.app.FinancialApplication;
import com.evp.view.dialog.CustomAlertDialog;

/**
 * The type Print listener.
 */
public class PrintListenerImpl implements PrintListener {

    private Context context;
    private CustomAlertDialog showMsgDialog;
    private CustomAlertDialog confirmDialog;
    private ConditionVariable cv;
    private Status result = Status.OK;

    /**
     * Instantiates a new Print listener.
     *
     * @param context the context
     */
    public PrintListenerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void onShowMessage(final String title, final String message) {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (showMsgDialog == null) {
                    showMsgDialog = new CustomAlertDialog(context, CustomAlertDialog.PROGRESS_TYPE);
                    showMsgDialog.show();
                    showMsgDialog.setCancelable(false);
                    showMsgDialog.setTitleText(title);
                    showMsgDialog.setContentText(message);

                } else {
                    if (!showMsgDialog.isShowing()) {
                        showMsgDialog.show();
                    }
                    showMsgDialog.setTitleText(title);
                    showMsgDialog.setContentText(message);
                }
            }
        });
    }

    @Override
    public Status onConfirm(final String title, final String message) {
        cv = new ConditionVariable();
        result = Status.OK;
        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (confirmDialog != null) {
                    confirmDialog.dismiss();
                }
                confirmDialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE);
                confirmDialog.show();
                confirmDialog.setTimeout(30);
                confirmDialog.setTitleText(title);
                confirmDialog.setContentText(message);
                confirmDialog.setCancelable(false);
                confirmDialog.setCanceledOnTouchOutside(false);
                confirmDialog.showCancelButton(true);
                confirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() { // AET-77
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
                confirmDialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        result = Status.CANCEL;
                        alertDialog.dismiss();
                    }
                });
                confirmDialog.showConfirmButton(true);
                confirmDialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        result = Status.CONTINUE;
                        alertDialog.dismiss();
                    }
                });
                confirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (result == Status.OK) {
                            result = Status.CANCEL;
                        }
                        if (cv != null) {
                            cv.open();
                        }
                    }
                });
                confirmDialog.show();

            }
        });
        cv.block();
        return result;
    }

    @Override
    public void onEnd() {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showMsgDialog != null && showMsgDialog.isShowing()) {
                    showMsgDialog.dismiss();
                }
                if (confirmDialog != null && confirmDialog.isShowing()) {
                    confirmDialog.dismiss();
                }
            }
        });
    }

}
