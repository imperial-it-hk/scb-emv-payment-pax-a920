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
package com.evp.eemv;

import com.evp.eemv.entity.ClssInputParam;
import com.evp.eemv.entity.ClssTornLogRecord;
import com.evp.eemv.enums.EKernelType;
import com.evp.eemv.exception.EmvException;

import java.util.List;

public interface IClss extends IEmvBase {
    IClss getClss();

    EKernelType getKernelType();

    void preTransaction(ClssInputParam inputParam) throws EmvException;

    void setListener(IClssListener listener);

    void setTornLogRecords(List<ClssTornLogRecord> clssTornLogRecord);

    List<ClssTornLogRecord> getTornLogRecords();
}
