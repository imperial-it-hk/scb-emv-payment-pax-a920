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
package com.evp.settings.spupgrader;

import com.evp.commonlib.utils.LogUtils;
import com.evp.settings.SharedPref;

/**
 * SharedPreference upgrade
 */
public abstract class SpUpgrader {

    private static final String TAG = "SP Upgrader";

    /**
     * Upgrade.
     *
     * @param sp          the sp
     * @param oldVersion  the old version
     * @param newVersion  the new version
     * @param packagePath the package path
     */
//sp upgrade
    public static void upgrade(SharedPref sp, int oldVersion, int newVersion, String packagePath) {
        try {
            Class<?> c1 = Class.forName(packagePath + ".Upgrade" + oldVersion + "To" + newVersion);
            SpUpgrader upgrader = (SpUpgrader) c1.newInstance();
            LogUtils.i("SpUpgrader", "upgrading from version(" + oldVersion + ") to version(" + newVersion + ")");
            upgrader.upgrade(sp);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LogUtils.w(TAG, "", e);
            throw new IllegalArgumentException("No Upgrader for SP" +
                    " from version(" + oldVersion +
                    ") to version(" + newVersion + ")");
        }
    }

    /**
     * Upgrade.
     *
     * @param sp the sp
     */
//upgrade method
    public abstract void upgrade(SharedPref sp);
}
