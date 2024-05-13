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
 * 20190108  	         Linhb                   Create
 * ===========================================================================================
 */

package com.evp.eemv.exception;

public abstract class AGeneralException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String errModule;
    private final int errCode;
    private final String errMsg;

    AGeneralException(String module, int errCode, String errMsg) {
        super(module + "#" + errCode + "(" + errMsg + ")");
        this.errModule = module;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }


    AGeneralException(String module, int errCode, String errMsg, Throwable throwable) {
        super(module + "#" + errCode + "(" + errMsg + ")", throwable);
        this.errModule = module;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String getErrModule() {
        return this.errModule;
    }

    public int getErrCode() {
        return this.errCode;
    }

    public String getErrMsg() {
        return this.errMsg;
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.exception.AGeneralException
 * JD-Core Version:    0.6.0
 */