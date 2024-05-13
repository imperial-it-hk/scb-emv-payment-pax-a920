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
 * 20200338  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.commonlib.utils;

import androidx.core.util.Pools;

/**
 * Object pool
 */
public class ObjectPoolHelper {
    private ObjectPoolHelper() {
    }
    private static final Pools.SynchronizedPool<float[]> floatArrayPool = new Pools.SynchronizedPool<>(10);
    private static final Pools.SynchronizedPool<StringBuilder> stringBuilderPool = new Pools.SynchronizedPool<>(10);

    /**
     * get float array from object pool
     * @param len array size
     * @return float array
     */
    public static float[] obtainFloatArray(int len){
        float[] instance = floatArrayPool.acquire();
        if (instance == null){
            return new float[len];
        }
        return instance;
    }

    /**
     * release float array from object pool
     * @param arrays float array
     */
    public static void releaseFloatArray(float[] arrays){
        floatArrayPool.release(arrays);
    }

    /**
     * get StringBuilder from object pool
     * @return StringBuilder
     */
    public static StringBuilder obtainStringBuilder(){
        StringBuilder instance = stringBuilderPool.acquire();
        if (instance == null){
            return new StringBuilder();
        } else {
            instance.setLength(0);
        }
        return instance;
    }

    /**
     * release StringBuilder from object pool
     * @param stringBuilder StringBuilder
     */
    public static void releaseStringBuilder(StringBuilder stringBuilder){
        stringBuilderPool.release(stringBuilder);
    }
}
