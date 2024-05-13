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
 * 20190108  	         Joshua Huang            Create
 * ===========================================================================================
 */
package com.evp.pay.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.evp.commonlib.utils.LogUtils;

/**
 * Created by zhangyp on 2019/4/19
 */
public class AppActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ActivityStack.getInstance().push(activity);
        LogUtils.i("AppActivityLifecycleCallbacks::onCreated", "currentActivity:"+activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ActivityStack.getInstance().setTop(activity);
        LogUtils.i("AppActivityLifecycleCallbacks::onResumed", "currentActivity:"+activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ActivityStack.getInstance().pop(activity);
        LogUtils.i("AppActivityLifecycleCallbacks::onDestroyed", "currentActivity:"+activity);
    }
}
