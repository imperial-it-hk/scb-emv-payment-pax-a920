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
package com.evp.eventbus;

/**
 * abstract event for event bus
 */
public abstract class Event {

    private Object status;
    private Object data = null;

    /**
     * Instantiates a new Event.
     *
     * @param status the status
     */
    public Event(Object status) {
        this.setStatus(status);
    }

    /**
     * Instantiates a new Event.
     *
     * @param status the status
     * @param data   the data
     */
    public Event(Object status, Object data) {
        this.setStatus(status);
        this.setData(data);
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public Object getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(Object status) {
        this.status = status;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(Object data) {
        this.data = data;
    }
}
