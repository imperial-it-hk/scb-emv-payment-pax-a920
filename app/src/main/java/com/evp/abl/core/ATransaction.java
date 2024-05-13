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

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.trans.TransContext;

import java.util.HashMap;
import java.util.Map;

/**
 * finite state machine for actions
 *
 * @author Steven.W
 */
public abstract class ATransaction {
    /**
     * The constant TAG.
     */
    protected static final String TAG = "ATransaction";
    /**
     * state and action binding table
     */
    private Map<String, AAction> actionMap;

    /**
     * state and transaction binding table
     */
    private Map<String, ATransaction> transactionMap;

    /**
     * transaction end listener
     *
     * @author Steven.W
     */
    public interface TransEndListener {
        /**
         * On end.
         *
         * @param result the result
         */
        void onEnd(ActionResult result);
    }

    /**
     * single state bind transaction
     *
     * @param state       state
     * @param transaction cannot be this
     */
    protected void bind(String state, ATransaction transaction) {
        if (transactionMap == null) {
            transactionMap = new HashMap<>(8);
        }
        if (!this.equals(transaction)) {
            transactionMap.put(state, transaction);
        }
    }

    /**
     * single state bind action
     *
     * @param state  state
     * @param action target action
     */
    protected void bind(String state, AAction action) {
        if (actionMap == null) {
            actionMap = new HashMap<>(16);
        }
        actionMap.put(state, action);
    }

    /**
     * clear the action map
     */
    protected void clear() {
        if (actionMap != null) {
            actionMap.clear();
            actionMap = null;
        }
        if (transactionMap != null) {
            transactionMap.clear();
            transactionMap = null;
        }
    }

    /**
     * execute action bound by state
     *
     * @param state state
     */
    public void gotoState(String state) {
        AAction action = actionMap.get(state);
        if (action != null) {
            action.setFinished(false); //AET-191
            action.execute();
        } else {
            ATransaction transaction = transactionMap.get(state);
            if (transaction != null) {
                transaction.execute();
            } else {
                LogUtils.e(TAG, "Invalid State:" + state);
            }
        }
    }

    /**
     * reset action state
     *
     * @param state state
     */
    public void resetState(String state) {
        AAction action = actionMap.get(state);
        if (action != null) {
            action.setFinished(false);
            TransContext.getInstance().setCurrentAction(action);
        }
    }

    /**
     * execute transaction
     */
    public void execute() {
        bindStateOnAction();
    }

    /**
     * call {@link #bind(String, AAction)} in this method to bind all states to actions,
     * and call {@link #gotoState(String)} to run the first action in the end of this method.
     */
    protected abstract void bindStateOnAction();

}
