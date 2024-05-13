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
package com.evp.pay.trans.action;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.view.dialog.InputPwdDialog;

import java.lang.ref.WeakReference;

/**
 * The type Action input password.
 */
public class ActionInputPassword extends AAction {
    private Context context;
    private int maxLen;
    private String title;
    private String subTitle;
    private boolean allowCanceledOnTouchOutside = true;
    private int backKeyResult = TransResult.ERR_ABORTED;

    private ProcessRunnable processRunnable = null;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionInputPassword(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context  the context
     * @param maxLen   the max len
     * @param title    the title
     * @param subTitle the sub title
     */
    public void setParam(Context context, int maxLen, String title, String subTitle) {
        this.context = context;
        this.maxLen = maxLen;
        this.title = title;
        this.subTitle = subTitle;
        this.allowCanceledOnTouchOutside = true;
    }

    /**
     * Sets param.
     *
     * @param backKeyResult the back key result
     */
    public void setParam(int backKeyResult) {
        this.backKeyResult = backKeyResult;
    }

    /**
     * Sets param.
     *
     * @param context                     the context
     * @param maxLen                      the max len
     * @param title                       the title
     * @param subTitle                    the sub title
     * @param allowCanceledOnTouchOutside the allow canceled on touch outside
     */
    public void setParam(Context context, int maxLen, String title, String subTitle, boolean allowCanceledOnTouchOutside) {
        this.context = context;
        this.maxLen = maxLen;
        this.title = title;
        this.subTitle = subTitle;
        this.allowCanceledOnTouchOutside = allowCanceledOnTouchOutside;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        processRunnable = new ProcessRunnable(this);
        FinancialApplication.getApp().runOnUiThreadDelay(processRunnable, 100);
    }

    @Override
    public void setResult(ActionResult result) {
        if (processRunnable != null && result.getRet() == TransResult.ERR_TIMEOUT)
            processRunnable.dialog.dismiss();
        else
            super.setResult(result);
    }

    private static class ProcessRunnable implements Runnable {

        private WeakReference<ActionInputPassword> weakRefActionInputpwd;

        /**
         * The Dialog.
         */
        InputPwdDialog dialog = null;

        /**
         * Instantiates a new Process runnable.
         *
         * @param actionInputPassword the action input password
         */
        ProcessRunnable(ActionInputPassword actionInputPassword) {
            this.weakRefActionInputpwd = new WeakReference<>(actionInputPassword);
        }

        private void setOnKeyListener() {
            dialog.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        ActionInputPassword actionInputPassword = weakRefActionInputpwd.get();
                        if (null != actionInputPassword) {
                            actionInputPassword.setResult(new ActionResult(actionInputPassword.backKeyResult, null));
                        }

                        dialog.dismiss();
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        dialog.dismiss();
                    }
                    return false;
                }
            });
            dialog.setCancelable(false);
        }

        private void setPwdListener() {
            dialog.setPwdListener(new InputPwdDialog.OnPwdListener() {
                @Override
                public void onSucc(String data) {
                    ActionInputPassword actionInputPassword = weakRefActionInputpwd.get();
                    if (null != actionInputPassword) {
                        actionInputPassword.setResult(new ActionResult(TransResult.SUCC, data));
                    }
                    dialog.dismiss();
                }

                @Override
                public void onErr() {
                    ActionInputPassword actionInputPassword = weakRefActionInputpwd.get();
                    if (null != actionInputPassword) {
                        actionInputPassword.setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                    dialog.dismiss();
                }
            });
        }

        private void setOnCancelListener() {
            //AET-50
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    ActionInputPassword actionInputPassword = weakRefActionInputpwd.get();
                    if (null != actionInputPassword) {
                        actionInputPassword.setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                    dialog.dismiss();
                }
            });
            ActionInputPassword actionInputPassword = weakRefActionInputpwd.get();
            if (null == actionInputPassword) {
                return;
            }
            dialog.setCanceledOnTouchOutside(actionInputPassword.allowCanceledOnTouchOutside); // AET-17
        }

        @Override
        public void run() {
            ActionInputPassword actionInputPassword = weakRefActionInputpwd.get();
            if (null == actionInputPassword) {
                return;
            }
            dialog = new InputPwdDialog(actionInputPassword.context, actionInputPassword.maxLen,
                    actionInputPassword.title, actionInputPassword.subTitle);
            setOnKeyListener();
            setPwdListener();
            setOnCancelListener();
            dialog.show();
        }
    }
}
