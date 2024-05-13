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

import android.os.SystemClock;

import com.evp.pay.app.FinancialApplication;

/**
 * auto recovered value for quick click protection
 *
 * @param <T> the type parameter
 * @author Steven.W
 */
class AutoRecoveredValueSetter<T> {

    private T value;
    private T recoveredTo;
    private long timeoutMs;

    /**
     * Sets value.
     *
     * @param value the value
     */
    protected void setValue(T value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    protected T getValue() {
        return value;
    }

    /**
     * Sets recover to.
     *
     * @param value the value
     */
    void setRecoverTo(T value) {
        this.recoveredTo = value;
    }

    /**
     * Sets timeout ms.
     *
     * @param timeoutMs the timeout ms
     */
    void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Recover.
     */
    void recover() {
        this.value = recoveredTo;
    }

    /**
     * Auto recover.
     */
    void autoRecover() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(timeoutMs);
                setValue(recoveredTo);
            }
        });
    }

}
