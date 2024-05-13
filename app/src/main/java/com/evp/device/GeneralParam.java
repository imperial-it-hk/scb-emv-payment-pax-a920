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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.device;

import com.evp.pay.app.FinancialApplication;
import com.evp.settings.SharedPref;

/**
 * general param, share preferences
 *
 * @author Steven.W
 */
public class GeneralParam {
    /**
     * PIN key
     */
    public static final String TPK = "TPK";
    /**
     * MAC key
     */
    public static final String TAK = "TAK";
    /**
     * DES key
     */
    public static final String TDK = "TDK";

    private static final String CONFIG_FILE_NAME = "generalParam";
    private final SharedPref mGnrlSp;

    /**
     * Instantiates a new General param.
     */
    public GeneralParam() {
        mGnrlSp = new SharedPref(FinancialApplication.getApp(), CONFIG_FILE_NAME);
    }

    /**
     * Get string.
     *
     * @param key the key
     * @return the string
     */
    public String get(String key) {
        return  mGnrlSp.getString(key);
    }

    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(String key, String value) {
        mGnrlSp.putString(key,value);
    }

}
