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

import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.Capk;
import com.evp.eemv.entity.Config;
import com.evp.eemv.entity.InputParam;
import com.evp.eemv.exception.EmvException;

import java.util.List;

public interface IEmvBase {
    void init() throws EmvException;

    void setConfig(Config config);

    Config getConfig();

    byte[] getTlv(int tag);

    void setTlv(int tag, byte[] value) throws EmvException;

    CTransResult process(InputParam inputParam) throws EmvException;

    void setCapkList(List<Capk> capkList);

    void setAidParamList(List<AidParam> aidParamList);

    String getVersion();
}
