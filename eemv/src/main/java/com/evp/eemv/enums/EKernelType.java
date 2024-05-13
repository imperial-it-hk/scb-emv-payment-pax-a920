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

package com.evp.eemv.enums;

public enum EKernelType {
    DEF(0),
    JCB(1),
    MC(2),
    VIS(3),
    PBOC(4),
    AE(5),
    ZIP(6),
    FLASH(7),
    EFT(8),
    KERNTYPE_RUPAY(9);

    private byte kernelType;

    EKernelType(int kernelType) {
        this.kernelType = (byte) kernelType;
    }

    public byte getKernelType() {
        return this.kernelType;
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.enums.EKernelType
 * JD-Core Version:    0.6.0
 */