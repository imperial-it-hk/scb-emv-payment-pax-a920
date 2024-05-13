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
 * Date	                 Author	                Action
 * 20200917  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.commonlib.utils;

import android.content.Context;
import android.os.PowerManager;

public class DeviceUtils {
    /**
     * check whether isScreenOn
     * @param context applicationContext
     * @return isScreenOn
     */
    @SuppressWarnings("deprecation")
    public static boolean isScreenOn(Context context){
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean interactive;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            interactive = powerManager.isInteractive();
        }else {
            interactive = powerManager.isScreenOn();
        }
        return interactive;
    }
}
