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
 * card searching event
 */
public class SearchCardEvent extends Event {
    /**
     * The enum Status.
     */
    public enum Status {
        /**
         * Icc update card info status.
         */
        ICC_UPDATE_CARD_INFO,
        /**
         * Icc confirm card num status.
         */
        ICC_CONFIRM_CARD_NUM,
        /**
         * Clss light status not ready status.
         */
        CLSS_LIGHT_STATUS_NOT_READY,
        /**
         * Clss light status idle status.
         */
        CLSS_LIGHT_STATUS_IDLE,
        /**
         * Clss light status ready for txn status.
         */
        CLSS_LIGHT_STATUS_READY_FOR_TXN,
        /**
         * Clss light status processing status.
         */
        CLSS_LIGHT_STATUS_PROCESSING,
        /**
         * Clss light status remove card status.
         */
        CLSS_LIGHT_STATUS_REMOVE_CARD,
        /**
         * Clss light status complete status.
         */
        CLSS_LIGHT_STATUS_COMPLETE,
        /**
         * Clss light status error status.
         */
        CLSS_LIGHT_STATUS_ERROR,
    }

    /**
     * Instantiates a new Search card event.
     *
     * @param status the status
     */
    public SearchCardEvent(Status status) {
        super(status);
    }

    /**
     * Instantiates a new Search card event.
     *
     * @param status the status
     * @param data   the data
     */
    public SearchCardEvent(Status status, Object data) {
        super(status, data);
    }
}
