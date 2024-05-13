/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) YYYY-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date	                 Author	                Action
 * 19/8/29  	         xieYb      	        Create
 * ===========================================================================================
 */
package com.evp.commonlib.application;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.evp.commonlib.utils.LogUtils;

public class AppInfo {
    private static final String PAXSTORE_APP_KEY = "PAXSTORE_APP_KEY";
    private static final String PAXSTORE_APP_SECRET = "PAXSTORE_APP_SECRET";


    private Context context;

    private AppInfo() {
    }

    public static AppInfo getInstance() {
        return AppInfoHolder.INSTANCE;
    }

    public void init(Context app) {
        this.context = app.getApplicationContext();
    }

    /**
     * 获取paxStore APP_KEY
     */
    public String getAppKey() {
        return getMetaData(PAXSTORE_APP_KEY);
    }

    /**
     * 获取paxStore APP_SECRET
     */
    public String getAppSecret() {
        return getMetaData(PAXSTORE_APP_SECRET);
    }

    /**
     * 获取AndroidManifest文件中metadata
     *
     * @param name key
     * @return string value
     */
    public String getMetaData(String name) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        if (applicationInfo == null || applicationInfo.metaData == null) {
            return "";
        }
        return applicationInfo.metaData.getString(name);
    }

    /**
     * 获取AndroidManifest文件中metadata
     *
     * @param name key
     * @return int value
     */
    public int getIntMetaData(String name) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        if (applicationInfo == null || applicationInfo.metaData == null) {
            return 0;
        }
        return applicationInfo.metaData.getInt(name);
    }

    public Context getApplicationContext() {
        return context;
    }

    private static class AppInfoHolder {
        private static final AppInfo INSTANCE = new AppInfo();
    }

}
