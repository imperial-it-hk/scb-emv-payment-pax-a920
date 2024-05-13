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

package com.evp.eemv;

import com.evp.eemv.entity.Amounts;
import com.evp.eemv.entity.CandList;
import com.evp.eemv.enums.EOnlineResult;
import com.pax.dal.exceptions.PedDevException;

import java.util.List;

public interface IEmvListener {
    Amounts onGetAmounts();

    int onWaitAppSelect(boolean isFirstSelect, List<CandList> candList);

    int onConfirmCardNo(String cardNo);

    int onCardHolderPwd(boolean bOnlinePin, int leftTimes, byte[] pinData);

    EOnlineResult onOnlineProc() throws PedDevException;

    boolean onChkExceptionFile();

    void setCvmResult(byte[] cvmResult);

    int onDcc() throws PedDevException;

    int onAdditionalProcess();

    boolean isPinAllowedForTransaction();
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.IEmvListener
 * JD-Core Version:    0.6.0
 */