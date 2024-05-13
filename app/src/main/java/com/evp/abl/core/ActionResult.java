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

/**
 * action result
 *
 * @author Steven.W
 */
public class ActionResult {
    /**
     * return code
     */
    private int ret;
    /**
     * return data
     */
    private Object data;
    private Object data1;

    /**
     * Instantiates a new Action result.
     *
     * @param ret  the ret
     * @param data the data
     */
    public ActionResult(int ret, Object data) {
        this.ret = ret;
        this.data = data;
    }

    /**
     * Instantiates a new Action result.
     *
     * @param ret   the ret
     * @param data  the data
     * @param data1 the data 1
     */
    public ActionResult(int ret, Object data, Object data1) {
        this.ret = ret;
        this.data = data;
        this.data1 = data1;
    }

    /**
     * Gets ret.
     *
     * @return the ret
     */
    public int getRet() {
        return ret;
    }

    /**
     * Sets ret.
     *
     * @param ret the ret
     */
    public void setRet(int ret) {
        this.ret = ret;
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

    /**
     * Gets data 1.
     *
     * @return the data 1
     */
    public Object getData1() {
        return data1;
    }

    /**
     * Sets data 1.
     *
     * @param data1 the data 1
     */
    public void setData1(Object data1) {
        this.data1 = data1;
    }
}
