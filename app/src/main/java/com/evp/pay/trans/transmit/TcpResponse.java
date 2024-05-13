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
package com.evp.pay.trans.transmit;

/**
 * The type Tcp response.
 */
public class TcpResponse {
    //result code
    private int retCode;
    //result data
    private byte[] data;

    /**
     * init tpc response
     *
     * @param retCode result code
     * @param data    result data
     */
    public TcpResponse(int retCode, byte[] data) {
        this.retCode = retCode;
        this.data = data;
    }

    /**
     * get result code
     *
     * @return result code
     */
    public int getRetCode() {
        return retCode;
    }

    /**
     * set result code
     *
     * @param retCode result code
     */
    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    /**
     * get result data
     *
     * @return result data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * set result data
     *
     * @param data result data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

}
