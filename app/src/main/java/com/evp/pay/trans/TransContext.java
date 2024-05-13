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
package com.evp.pay.trans;

import com.evp.abl.core.AAction;

/**
 * The type Trans context.
 */
public class TransContext {
    private static TransContext transContext;

    private AAction currentAction;

    private TransContext() {

    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static synchronized TransContext getInstance() {
        if (transContext == null) {
            transContext = new TransContext();
        }
        return transContext;
    }

    /**
     * get current action
     *
     * @return current action
     */
    public AAction getCurrentAction() {
        return currentAction;
    }

    /**
     * set current action
     *
     * @param currentAction current action
     */
    public void setCurrentAction(AAction currentAction) {
        this.currentAction = currentAction;
    }

}
