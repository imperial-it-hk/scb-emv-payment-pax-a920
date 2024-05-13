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

package com.evp.view;

import android.content.Context;

import com.evp.pay.app.FinancialApplication;
import com.evp.settings.SharedPref;

/**
 * Created by Joshua Huang on 2018/3/26.
 */
public class UserGuideManager {

    private static UserGuideManager INSTANCE = new UserGuideManager();
    private SharedPref sp;

    private static final String USER_GUIDE_ENABLE = "USER_GUIDE_ENABLE";
    private static final String PSW_GUIDE_ENABLE = "PASSWORD_GUIDE_ENABLE";
    private static final String SETTINGS_GUIDE_ENABLE = "SETTINGS_GUIDE_ENABLE";
    private static final String SALE_GUIDE_ENABLE = "SALE_GUIDE_ENABLE";

    private UserGuideManager() {
        Context mAppContext = FinancialApplication.getApp().getApplicationContext();
        sp = new SharedPref(mAppContext, "USER_GUIDE_CONTROL");

        setPswGuideEnable(true);
        setSettingsGuideEnable(true);
        setSaleGuideEnable(true);

        setEnable(false);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static UserGuideManager getInstance() {
        return INSTANCE;
    }

    /**
     * Is enabled boolean.
     *
     * @return the boolean
     */
    public boolean isEnabled() {
        return sp.getBoolean(USER_GUIDE_ENABLE, false);
    }

    /**
     * Sets enable.
     *
     * @param isEnabled the is enabled
     */
    public void setEnable(boolean isEnabled) {
        sp.putBoolean(USER_GUIDE_ENABLE, isEnabled);
    }

    /**
     * Is psw guide enabled boolean.
     *
     * @return the boolean
     */
    public boolean isPswGuideEnabled() {
        return isEnabled() && sp.getBoolean(PSW_GUIDE_ENABLE, false);
    }

    /**
     * Sets psw guide enable.
     *
     * @param isEnabled the is enabled
     */
    public void setPswGuideEnable(boolean isEnabled) {
        sp.putBoolean(PSW_GUIDE_ENABLE, isEnabled);
    }

    /**
     * Is settings guide enabled boolean.
     *
     * @return the boolean
     */
    public boolean isSettingsGuideEnabled() {
        return isEnabled() && sp.getBoolean(SETTINGS_GUIDE_ENABLE, false);
    }

    /**
     * Sets settings guide enable.
     *
     * @param isEnabled the is enabled
     */
    public void setSettingsGuideEnable(boolean isEnabled) {
        sp.putBoolean(SETTINGS_GUIDE_ENABLE, isEnabled);
    }

    /**
     * Is sale guide enabled boolean.
     *
     * @return the boolean
     */
    public boolean isSaleGuideEnabled() {
        return isEnabled() && sp.getBoolean(SALE_GUIDE_ENABLE, false);
    }

    /**
     * Sets sale guide enable.
     *
     * @param isEnabled the is enabled
     */
    public void setSaleGuideEnable(boolean isEnabled) {
        sp.putBoolean(SALE_GUIDE_ENABLE, isEnabled);
    }

}
