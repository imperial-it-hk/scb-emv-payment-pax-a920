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
package com.evp.pay.app.quickclick;


/**
 * quick click protection, workaround of handling multi-touch
 *
 * @author Steven.W
 */
abstract class AQuickClickProtection extends AutoRecoveredValueSetter<Boolean> {
    /**
     * Instantiates a new A quick click protection.
     *
     * @param timeoutMs the timeout ms
     */
    AQuickClickProtection(long timeoutMs) {
        setTimeoutMs(timeoutMs);
        setValue(false);
        setRecoverTo(false);
    }

    /**
     * def 500ms
     */
    AQuickClickProtection() {
        setTimeoutMs(500);
        setValue(false);
        setRecoverTo(false);
    }

    /**
     * check if it is protecting
     *
     * @return true /false
     */
    public boolean isStarted() {
        return getValue();
    }

    /**
     * start the click protection
     */
    public void start() {
        setValue(true);
        autoRecover();
    }

    /**
     * force to stop the protection
     */
    public void stop() {
        recover();
    }
}
