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

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

class Utils {

    private static final List<String> NO_PAX_DEVICE = new ArrayList<>();
    static {
        NO_PAX_DEVICE.add("N6F27I"); //Nexus6
    }

    private Utils() {
        //do nothing
    }

    /**
     * check whether is pax device
     * @return check result
     */
    static boolean isPaxDevice() {
        return NO_PAX_DEVICE.indexOf(Build.DISPLAY) == -1;
    }
}
