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

import com.pax.dal.ISignPad;
import com.pax.dal.entity.EUartPort;
import com.pax.dal.entity.SignPadResp;
/**
 * neptune ISignPad
 */
class DemoSignPad implements ISignPad {
    DemoSignPad() {
        //do nothing
    }

    @Override
    public SignPadResp signStart(String s) {
        return null;
    }

    @Override
    public int displayWord(int i, int i1, byte b, byte b1, byte b2, int i2) {
        return 0;
    }

    @Override
    public int cancel() {
        return 0;
    }

    @Override
    public int showIdleScreen() {
        return 0;
    }

    @Override
    public void setPort(EUartPort eUartPort) {

    }
}
