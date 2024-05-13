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

import android.content.Context;

import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.IDAL;
import com.pax.linhb.nativetouchevent.NativeTouchEvent;
import com.pax.neptunelite.api.NeptuneLiteUser;
/**
 * neptune entry
 */
public class Sdk {
    private static final String TAG = "SDK";
    private static Sdk instance = null;
    private IDAL dal;

    public static class TouchEvent extends NativeTouchEvent{

    }

    private TouchEvent touchEvent = new TouchEvent();

    private Sdk() {
    }

    public static Sdk getInstance() {
        if (instance == null) {
            instance = new Sdk();
        }
        return instance;
    }

    public IDAL getDal(Context context) {
        if (Utils.isPaxDevice()) {
            LogUtils.i(TAG, "before NeptuneUser");
            try {
                dal = NeptuneLiteUser.getInstance().getDal(context);
            } catch (Exception e) {
                LogUtils.w(TAG, e);
            }
            LogUtils.i(TAG, "after NeptuneUser");
        } else {
            return new DemoDal(context);
        }
        return dal;
    }

    public TouchEvent getTouchEvent(){
        return touchEvent;
    }

    public static boolean isPaxDevice() {
        return Utils.isPaxDevice();
    }
}
