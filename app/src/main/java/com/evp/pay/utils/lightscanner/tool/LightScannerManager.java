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
 * 20190108  	         Steven.S                Create
 * ===========================================================================================
 */

package com.evp.pay.utils.lightscanner.tool;

import android.content.Context;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.pax.dal.IScanCodec;
import com.pax.dal.entity.DecodeResult;

/**
 * Created by Steven.S on 2018/5/21/0021.
 */
public class LightScannerManager {
    private static final String TAG = LightScannerManager.class.getSimpleName();

    /**
     * The constant WIDTH.
     */
    public static final int WIDTH = 480;
    /**
     * The constant HEIGHT.
     */
    public static final int HEIGHT = 480;
    private IScanCodec scanCodec;

    private LightScannerManager(){}
    private static class LazyHolder {
        /**
         * The constant INSTANCE.
         */
        public static final LightScannerManager INSTANCE = new LightScannerManager();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static LightScannerManager getInstance() {
        LightScannerManager instance = LazyHolder.INSTANCE;
        instance.scanCodec = FinancialApplication.getDal().getScanCodec();
        return instance;
    }

    /**
     * Disable format.
     *
     * @param format the format
     */
    public void disableFormat(int format){
        scanCodec.disableFormat(format);
        LogUtils.i(TAG, "disableFormat");
    }

    /**
     * Enable format.
     *
     * @param format the format
     */
    public void enableFormat(int format){
        scanCodec.enableFormat(format);
        LogUtils.i(TAG, "enableFormat");
    }

    /**
     * Init.
     *
     * @param context the context
     * @param width   the width
     * @param height  the height
     */
    public void init(Context context, int width, int height){
        scanCodec.init(context, width, height);
        LogUtils.i(TAG, "init");
    }

    /**
     * Decode decode result.
     *
     * @param data the data
     * @return the decode result
     */
    public DecodeResult decode(byte[] data){
        DecodeResult result = scanCodec.decode(data);
//        LogUtils.i(TAG, "decode");
        return result;
    }

    /**
     * Release.
     */
    public void release(){
        scanCodec.release();
        LogUtils.i(TAG, "release");
    }
}
