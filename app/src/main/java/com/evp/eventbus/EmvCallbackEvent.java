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
 * 20190108  	         huangmuhua              Create
 * ===========================================================================================
 */
package com.evp.eventbus;

/**
 * emv callback event
 */
public class EmvCallbackEvent extends Event {
    /**
     * The enum Status.
     */
    public enum Status {
        /**
         * Offline pin enter ready status.
         */
        OFFLINE_PIN_ENTER_READY,
        /**
         * Card num confirm success status.
         */
        CARD_NUM_CONFIRM_SUCCESS,
        /**
         * Card num confirm error status.
         */
        CARD_NUM_CONFIRM_ERROR,
        /**
         * Timeout status.
         */
        TIMEOUT
    }

    /**
     * Instantiates a new Emv callback event.
     *
     * @param status the status
     */
    public EmvCallbackEvent(Status status) {
        super(status);
    }

    /**
     * Instantiates a new Emv callback event.
     *
     * @param status the status
     * @param data   the data
     */
    public EmvCallbackEvent(Status status, Object data) {
        super(status, data);
    }
}
