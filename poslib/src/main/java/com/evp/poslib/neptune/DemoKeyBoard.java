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

import com.pax.dal.IKeyBoard;
import com.pax.dal.entity.EKeyCode;
/**
 * neptune IKeyBoard
 */
class DemoKeyBoard implements IKeyBoard {
    DemoKeyBoard() {
        //do nothing
    }

    @Override
    public boolean isHit() {
        return false;
    }

    @Override
    public void clear() {
        //do nothing
    }

    @Override
    public EKeyCode getKey() {
        return EKeyCode.NO_KEY;
    }

    @Override
    public void setMute(boolean b) {
        //do nothing
    }
}
