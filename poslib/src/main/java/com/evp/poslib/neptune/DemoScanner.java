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

import com.pax.dal.IScanner;
import com.pax.dal.entity.EUartPort;
/**
 * neptune IScanner
 */
class DemoScanner implements IScanner {
    DemoScanner() {
        //do nothing
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public void start(IScanListener iScanListener) {
        //do nothing
    }

    @Override
    public void close() {
        //do nothing
    }

    @Override
    public void setTimeOut(int i) {
        //do nothing
    }

    @Override
    public boolean setContinuousTimes(int i) {
        return false;
    }

    @Override
    public void setContinuousInterval(int i) {

    }

    @Override
    public void setPort(EUartPort eUartPort) {

    }

    @Override
    public boolean setFlashOn(boolean b) {
        return false;
    }
}
