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

import com.evp.poslib.gl.comm.ICommHelper;
import com.evp.poslib.gl.convert.IConvert;
import com.evp.poslib.gl.imgprocessing.IImgProcessing;

/**
 * GL interface
 */
public interface IGL {
    //communication interface
    ICommHelper getCommHelper();
    //data convert interface
    IConvert getConvert();
    //Apdu、IIso8583、TLV data convert interface
    IPacker getPacker();
    //receipt generate interface
    IImgProcessing getImgProcessing();
}