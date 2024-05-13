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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.task;

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.evp.abl.core.AAction;
import com.evp.abl.core.AAction.ActionEndListener;
import com.evp.abl.core.ATransaction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.TransContext;
import com.evp.pay.utils.TransResultUtils;
import com.evp.view.dialog.DialogUtils;

/**
 * The type Base task.
 */
public abstract class BaseTask extends ATransaction {
    /**
     * The Context.
     */
    protected Context context;
    /**
     * transaction listener
     */
    protected TransEndListener transListener;

    private String currentState;

    /**
     * Instantiates a new Base task.
     *
     * @param context       the context
     * @param transListener the trans listener
     */
    public BaseTask(Context context, TransEndListener transListener) {
        super();
        this.context = context;
        this.transListener = transListener;
    }

    /**
     * transaction result prompt
     *
     * @param result the result
     */
    protected void transEnd(final ActionResult result) {
        clear(); // no memory leak
        TransContext.getInstance().setCurrentAction(null);
        if (transListener != null) {
            transListener.onEnd(result);
        }
    }

    /**
     * transaction result prompt and deal with remove card
     *
     * @param transName       the trans name
     * @param result          the result
     * @param dismissListener the dismiss listener
     */
    protected void dispResult(String transName, final ActionResult result, OnDismissListener dismissListener) {
        if (result.getRet() == TransResult.SUCC) {
            if (transName.equals(ETransType.SETTLE.getTransName())) {
                DialogUtils.showMessage(getCurrentContext(), ConfigUtils.getInstance().getString("settlementSuccessLabel"), dismissListener,
                        Constants.SUCCESS_DIALOG_SHOW_TIME);
            } else {
                DialogUtils.showSuccMessage(getCurrentContext(), transName, dismissListener,
                        Constants.SUCCESS_DIALOG_SHOW_TIME);
            }
        } else if (result.getRet() == TransResult.ERR_ABORTED || result.getRet() == TransResult.ERR_HOST_REJECT) {
            // ERR_ABORTED AND ERR_HOST_REJECT  not prompt error message
            if (dismissListener != null) {
                dismissListener.onDismiss(null);
            }
        } else {
            DialogUtils.showErrMessage(getCurrentContext(), transName,
                    TransResultUtils.getMessage(result.getRet()), dismissListener,
                    Constants.FAILED_DIALOG_SHOW_TIME);
        }
    }

    /**
     * Bind.
     *
     * @param state            the state
     * @param action           the action
     * @param forceEndWhenFail the force end when fail
     */
    protected void bind(String state, AAction action, final boolean forceEndWhenFail) {
        super.bind(state, action);
        if (action != null) {
            action.setEndListener(new ActionEndListener() {

                @Override
                public void onEnd(AAction action, final ActionResult result) {
                    FinancialApplication.getApp().runOnUiThread(new ActionEndRunnable(forceEndWhenFail, result));
                }
            });
        }
    }

    private class ActionEndRunnable implements Runnable {
        /**
         * The Force end when fail.
         */
        final boolean forceEndWhenFail;
        /**
         * The Result.
         */
        final ActionResult result;

        /**
         * Instantiates a new Action end runnable.
         *
         * @param forceEndWhenFail the force end when fail
         * @param result           the result
         */
        ActionEndRunnable(final boolean forceEndWhenFail, final ActionResult result) {
            this.forceEndWhenFail = forceEndWhenFail;
            this.result = result;
        }

        @Override
        public void run() {
            onEndRun(forceEndWhenFail, result);
        }

        private void onEndRun(final boolean forceEndWhenFail, final ActionResult result) {
            try {
                if (forceEndWhenFail && result.getRet() != TransResult.SUCC) {
                    transEnd(result);
                } else {
                    onActionResult(currentState, result);
                }
            } catch (Exception e) {
                LogUtils.w(TAG, "", e);
                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            }
        }
    }

    @Override
    protected void bind(String state, AAction action) {
        this.bind(state, action, false);
    }

    @Override
    public void gotoState(String state) {
        this.currentState = state;
        super.gotoState(state);
    }

    @Override
    public void resetState(String state) {
        this.currentState = state;
        super.resetState(state);
    }

    /**
     * Gets string.
     *
     * @param redId the red id
     * @return the string
     */
    @NonNull
    protected String getString(@StringRes int redId) {
        return context.getString(redId);
    }

    /**
     * deal action result
     *
     * @param currentState ：current State
     * @param result       ：current action result
     */
    public abstract void onActionResult(String currentState, ActionResult result);

    /**
     * Gets current context.
     *
     * @return the current context
     */
    protected Context getCurrentContext() {
        return ActivityStack.getInstance().top();
    }
}
