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

import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.exception.EmvException;
import com.pax.dal.exceptions.PedDevException;
import com.pax.jemv.clcommon.Clss_ProgramID_II;

import java.util.List;

public interface IClssListener {
    void onComfirmCardInfo(String track1, String track2, String track3) throws EmvException;

    int onCvmResult(ECvmResult result);

    EOnlineResult onOnlineProc(CTransResult result) throws PedDevException;

    boolean onDetect2ndTap();

    boolean onCheckDemoMode();

    int onIssScrCon();

    void onPromptRemoveCard();

    void onPromptRetry();

    int onDisplaySeePhone();

    List<Clss_ProgramID_II> onGetProgramId();

    int onDcc() throws PedDevException;
}
