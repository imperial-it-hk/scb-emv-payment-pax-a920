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
package com.evp.poslib.gl;

import com.pax.gl.pack.IApdu;
import com.pax.gl.pack.IIso8583;
import com.pax.gl.pack.ITlv;

/**
 * this interface provide interface for operate apdu、iso8583、tlv Message
 */
public interface IPacker {
    /**
     * interface for operate apdu message
     */
    IApdu getApdu();

    /**
     * interface for operate iso8583 message
     * @return IIso8583
     */
    IIso8583 getIso8583();

    /**
     * interface for operate tlv message
     * @return ITlv
     */
    ITlv getTlv();
}