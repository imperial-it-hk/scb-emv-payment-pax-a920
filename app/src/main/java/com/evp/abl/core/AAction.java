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
package com.evp.abl.core;

import com.evp.pay.trans.TransContext;

/**
 * Action abstract class
 *
 * @author Steven.W
 */
public abstract class AAction {

    /**
     * The constant TAG.
     */
    protected static final String TAG = "AAction";

    private boolean isFinished = false; //AET156 161 162

    /**
     * action start callback
     */
//@FunctionalInterface un-comment it for JAVA8, but now is JAVA7, so ignore it and same cases on Sonar.
    public interface ActionStartListener {
        /**
         * On start.
         *
         * @param action the action
         */
        void onStart(AAction action);
    }

    /**
     * action end listener
     */
    public interface ActionEndListener {
        /**
         * On end.
         *
         * @param action the action
         * @param result the result
         */
        void onEnd(AAction action, ActionResult result);
    }

    private ActionStartListener startListener;
    private ActionEndListener endListener;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public AAction(ActionStartListener listener) {
        this.startListener = listener;
    }

    /**
     * set{@link ActionEndListener} before run action.
     * in this method {@link ActionStartListener#onStart(AAction)} will be called.
     * and then {@link AAction#process} will be called right after that.
     */
    public void execute() {
        execute(true);
    }

    /**
     * Execute.
     *
     * @param saveCurrentAction the save current action
     */
    public void execute(boolean saveCurrentAction) {
        if (saveCurrentAction) {
            TransContext.getInstance().setCurrentAction(this);
        }
        if (startListener != null) {
            startListener.onStart(this);
        }
        process();
    }

    /**
     * end listener setter
     *
     * @param listener {@link ActionEndListener}
     */
    public void setEndListener(ActionEndListener listener) {
        this.endListener = listener;
    }

    /**
     * action process
     */
    protected abstract void process();

    /**
     * set action result, {@link ActionEndListener#onEnd(AAction, ActionResult)} will be called
     *
     * @param result {@link ActionResult}
     */
    public void setResult(ActionResult result) {
        if (endListener != null) {
            endListener.onEnd(this, result);
        }
    }

    /**
     * check if the action is finished
     *
     * @return true /false
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * reset action status
     *
     * @param finished true/false
     */
    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}