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
package com.evp.pay.trans.model;

import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.Utils;
import com.evp.settings.SharedPref;

import java.util.Date;

/**
 * The type Controller.
 */
public class Controller {
    private final SharedPref mCtrlSp;

    /**
     * The type Constant.
     */
    public static class Constant {
        /**
         * The constant YES.
         */
        public static final int YES = 1;
        /**
         * The constant NO.
         */
        public static final int NO = 0;
        /**
         * batch upload and settlement status
         */
        public static final int WORKED = 0;
        /**
         * The constant BATCH_UP.
         */
        public static final int BATCH_UP = 1;
        /**
         * The constant SETTLE.
         */
        public static final int SETTLE = 2;

        private Constant() {
            //do nothing
        }
    }

    /**
     * The constant IS_FIRST_RUN.
     */
    public static final String IS_FIRST_RUN = "IS_FIRST_RUN";
    /**
     * The constant NEED_SET_WIZARD.
     */
//add by xiawh if need setting wizard of not
    public static final String NEED_SET_WIZARD = "need_set_wizard";
    /**
     * is need download capk  FALSE: not need TRUE: need
     */
    public static final String NEED_DOWN_CAPK = "need_down_capk";
    /**
     * is need download aid FALSE: not need TRUE: need
     */
    public static final String NEED_DOWN_AID = "need_down_aid";
    /**
     * is need download emv terminal FALSE: not need TRUE: need
     */
    public static final String NEED_DOWN_EMV_TERMINAL = "need_down_emv_terminal";
    /**
     * batch upload status {@link Constant#WORKED}not in batch upload , {@link Constant#BATCH_UP}:in batch upload
     */
    public static final String BATCH_UP_STATUS = "batch_up_status";

    public static final String SETTLE_STATUS = "settle_status";

    public static final String LAST_SETTLE_DATE = "last_settle_date";

    /**
     * check result
     */
    public static final String RESULT = "result";
    /**
     * batch upload number
     */
    public static final String BATCH_NUM = "batch_num";
    /**
     * whether need to clear transaction record: FALSE: not clear, TRUE: clear
     */
    public static final String CLEAR_LOG = "clearLog";

    private static final String FILE_NAME = "control";

    /**
     * Instantiates a new Controller.
     */
    public Controller() {

        mCtrlSp = new SharedPref(FinancialApplication.getApp(), FILE_NAME);

        if (!isFirstRun()) {
            return;
        }
        mCtrlSp.putInt(NEED_DOWN_CAPK, Constant.YES);
        mCtrlSp.putInt(NEED_DOWN_AID, Constant.YES);
        mCtrlSp.putInt(NEED_DOWN_EMV_TERMINAL, Constant.YES);
        mCtrlSp.putInt(BATCH_UP_STATUS, Constant.NO);
        mCtrlSp.putInt(SETTLE_STATUS, Constant.NO);
        mCtrlSp.putLong(Controller.LAST_SETTLE_DATE, Utils.addDays(Utils.getStartOfDay(new Date()), -1).getTime());
    }

    /**
     * Get int.
     *
     * @param key the key
     * @return the int
     */
    public int get(String key) {
        return mCtrlSp.getInt(key, Constant.NO);
    }

    /**
     * Get int.
     *
     * @param key the key
     * @return the int
     */
    public long getLong(String key) {
        return mCtrlSp.getLong(key, Constant.NO);
    }

    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(String key, int value) {
        mCtrlSp.putInt(key, value);
    }

    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(String key, boolean value) {
        mCtrlSp.putBoolean(key, value);
    }

    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(String key, long value) {
        mCtrlSp.putLong(key, value);
    }

    /**
     * Is first run boolean.
     *
     * @return the boolean
     */
    public boolean isFirstRun() {
        return mCtrlSp.getBoolean(IS_FIRST_RUN, true);
    }

}
