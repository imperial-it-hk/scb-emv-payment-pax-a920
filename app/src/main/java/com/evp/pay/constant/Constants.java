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
package com.evp.pay.constant;

import com.pax.gl.img.IRgbToMonoAlgorithm;

/**
 * constants
 */
public class Constants {
    /**
     * the period of showing dialog of successful transaction, unit: second
     */
    public static final int SUCCESS_DIALOG_SHOW_TIME = 2;
    /**
     * the period of showing dialog of failed transaction, unit: second
     */
    public static final int FAILED_DIALOG_SHOW_TIME = 3;
    /**
     * the period of showing dialog of failed transaction for longer time, unit: second
     */
    public static final int FAILED_DIALOG_SHOW_TIME_LONG = 10;
    /**
     * SSL cert for verify SCB Server Side
     */
    public static final String SCB_CACERT_PATH = "scb_ca_cert.pem";
    /**
     * date pattern of storage
     */
    public static final String TIME_PATTERN_TRANS = "yyyyMMddHHmmss";
    /**
     * The constant TIME_PATTERN_TRANS2.
     */
    public static final String TIME_PATTERN_TRANS2 = "yyMMddHHmmss";
    /**
     * The constant DATE_PATTERN.
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /**
     * The constant TIME_PATTERN.
     */
    public static final String TIME_PATTERN = "HH:mm";

    /**
     * date pattern of display
     */
    public static final String TIME_PATTERN_DISPLAY = "yyyy/MM/dd HH:mm:ss";

    /**
     * The constant TIME_PATTERN_DISPLAY2.
     */
    public static final String TIME_PATTERN_DISPLAY2 = "MMM d, yyyy HH:mm";

    public static final String TIME_PATTERN_DISPLAY3 = "MMM d, yyyy";

    /**
     * max of amount digit
     */
    public static final int AMOUNT_DIGIT = 12;

    /**
     * The constant ACQUIRER_NAME.
     */
    public static final String ACQUIRER_NAME = "acquirer_name";
    /**
     * default pattern of pan mask.
     */
    public static final String PAN_MASK_PATTERN1 = "(?<=\\d{4})\\d(?=\\d{6})";
    /**
     * The constant PAN_MASK_PATTERN2.
     */
    public static final String PAN_MASK_PATTERN2 = "[0-9]";
    /**
     * The constant PAN_MASK_PATTERN3.
     */
    public static final String PAN_MASK_PATTERN3 = "";
    /**
     * The constant DEF_PAN_MASK_PATTERN.
     */
    public static final String DEF_PAN_MASK_PATTERN = "(?<=\\d{6})\\d(?=\\d{4})";

    /**
     * The constant INTEGER.
     */
    public static final int INTEGER = 0;
    /**
     * The constant LONG.
     */
    public static final int LONG = 1;
    /**
     * Maximum System Trace Number (STAN) value
     */
    public static final int MAX_STAN_NO = 999999;
    /**
     * Maximum Trace Number / Invoice number value
     */
    public static final long MAX_TRANS_NO = 999999;
    /**
     * Maximum Batch Number value
     */
    public static final long MAX_BATCH_NO = 999999;

    public static final String OLS_VERSION = "02000100";
    public static final String LMIC_SPECIAL_PRODUCT = "L02";


    /**
     * The constant rgb2MonoAlgo.
     */
    public static final IRgbToMonoAlgorithm rgb2MonoAlgo = new IRgbToMonoAlgorithm() {
        @Override
        public int evaluate(int r, int g, int b) {
            int v = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            // set new pixel color to output bitmap
            if (v < 200) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    private Constants() {
        //do nothing
    }
}
