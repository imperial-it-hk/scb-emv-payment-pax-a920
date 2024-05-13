/*
 *  * ===========================================================================================
 *  * = COPYRIGHT
 *  *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or nondisclosure
 *  *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *  *   disclosed except in accordance with the terms in that agreement.
 *  *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 *  * Description: // Detail description about the voidction of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date                  Author	                 Action
 *  * 20200618  	        Joshua Huang                   Modify
 *  * ===========================================================================================
 *
 */
package com.evp.commonlib.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Debug;

public class MemoryUtils {

    /**
     * It is not suggested to use in time sensitive place,like Contactless
     * @return Memory Size
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getMemory() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        int totalPss = memoryInfo.getTotalPss();
        int totalSwappablePss = memoryInfo.getTotalSwappablePss();
//        int total = totalPrivateClean + totalPrivateDirty /*+ totalPss + totalSharedClean + totalSharedDirty + totalSwappablePss*/;
        int total = totalPss + totalSwappablePss;
        return total / 1024;
    }

    public static void gc() {
        System.runFinalization();
        System.gc();
        Runtime.getRuntime().gc();
    }
}
