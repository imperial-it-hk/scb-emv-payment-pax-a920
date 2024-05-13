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

import com.pax.dal.IMag;
import com.pax.dal.entity.TrackData;
import com.pax.dal.exceptions.MagDevException;
/**
 * neptune IMag
 */
class DemoMag implements IMag {

    DemoMag() {
        //do nothing
    }

    @Override
    public void open() {
        //do nothing
    }

    @Override
    public void reset() {
        //do nothing
    }

    @Override
    public boolean isSwiped() {
        return true;
    }

    @Override
    public TrackData read() {
        TrackData data = new TrackData();
        data.setResultCode(0);
        data.setTrack1("Test Card");
        data.setTrack2("6228480030569279315=49121205685180000");
        data.setTrack3("996228480030569279315=156156000000000000000000000011414144912==000000000000=000000000000=058435500000000");
        return data;
    }

    @Override
    public void close() {
        //do nothing
    }

    @Override
    public TrackData readExt() throws MagDevException {
        return null;
    }

    @Override
    public void setup(byte b) throws MagDevException {

    }
}
