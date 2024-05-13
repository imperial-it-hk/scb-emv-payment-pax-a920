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

/**
 * The interface Print listener.
 */
public interface PrintListener {
    /**
     * The enum Status.
     */
    enum Status {
        /**
         * Ok status.
         */
        OK,
        /**
         * Continue status.
         */
        CONTINUE,
        /**
         * Cancel status.
         */
        CANCEL,
    }

    /**
     * print prompt
     *
     * @param title   title
     * @param message message
     */
    void onShowMessage(String title, String message);

    /**
     * printer abnormal
     *
     * @param title   the title
     * @param message the message
     * @return the status
     */
    Status onConfirm(String title, String message);

    /**
     * On end.
     */
    void onEnd();
}
