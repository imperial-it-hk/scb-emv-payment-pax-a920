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
package com.evp.eemv.entity;

import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;

/**
 * emv result
 */
public class CTransResult {

    private ETransResult transResult;
    private ECvmResult cvmResult;

    public CTransResult(ETransResult transResult) {
        this.transResult = transResult;
        this.cvmResult = ECvmResult.CONSUMER_DEVICE;
    }

    public CTransResult(ETransResult transResult, ECvmResult cvmType) {
        this.transResult = transResult;
        this.cvmResult = cvmType;
    }

    public ETransResult getTransResult() {
        return this.transResult;
    }

    public void setTransResult(ETransResult transResult) {
        this.transResult = transResult;
    }

    public ECvmResult getCvmResult() {
        return this.cvmResult;
    }

    public void setCvmResult(ECvmResult cvmResult) {
        this.cvmResult = cvmResult;
    }

}
