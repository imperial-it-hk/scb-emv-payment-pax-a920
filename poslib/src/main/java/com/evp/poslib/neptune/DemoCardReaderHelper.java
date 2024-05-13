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
package com.evp.poslib.neptune;

import android.os.SystemClock;

import com.pax.dal.ICardReaderHelper;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;

/**
 * neptune ICardReaderHelper
 */
class DemoCardReaderHelper implements ICardReaderHelper {
    private PollingResult result;
    private int cnt = 0;

    DemoCardReaderHelper() {
        result = new PollingResult();
    }

    @Override
    public PollingResult polling(EReaderType eReaderType, int i) throws MagDevException, IccDevException, PiccDevException {
        if (cnt > 10) {
            result.setOperationType(PollingResult.EOperationType.CANCEL);
            return result;
        }
        SystemClock.sleep(5000);
        result.setOperationType(PollingResult.EOperationType.OK);
        result.setReaderType(EReaderType.MAG);
        result.setTrack1("Test Card");
        result.setTrack2("6228480030569279315=49121205685180000");
        result.setTrack3("996228480030569279315=156156000000000000000000000011414144912==000000000000=000000000000=058435500000000");
        cnt++;
        return result;
    }

    @Override
    public PollingResult polling(EReaderType eReaderType, int i, boolean b) throws MagDevException, IccDevException, PiccDevException {
        return polling(eReaderType,i);
    }

    @Deprecated
    @Override
    public void setIsPause(boolean b) {
        //do nothing
    }

    @Override
    public void stopPolling() {
    }
}
