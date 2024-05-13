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
package com.evp.pay.utils;

import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Response code.
 */
public class ResponseCode implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private HashMap<String, ResponseCode> map;
    private static ResponseCode rcCode;

    private static Map<String, Integer> responses = new HashMap<>();

    static {
        //SCB host response codes
        responses.put("00", R.string.response_00);
        responses.put("01", R.string.response_01);
        responses.put("02", R.string.response_02);
        responses.put("03", R.string.response_03);
        responses.put("04", R.string.response_04);
        responses.put("05", R.string.response_05);
        responses.put("08", R.string.response_08);
        responses.put("09", R.string.response_09);
        responses.put("10", R.string.response_10);
        responses.put("11", R.string.response_11);
        responses.put("12", R.string.response_12);
        responses.put("13", R.string.response_13);
        responses.put("14", R.string.response_14);
        responses.put("19", R.string.response_19);
        responses.put("21", R.string.response_21);
        responses.put("25", R.string.response_25);
        responses.put("30", R.string.response_30);
        responses.put("31", R.string.response_31);
        responses.put("41", R.string.response_41);
        responses.put("43", R.string.response_43);
        responses.put("51", R.string.response_51);
        responses.put("54", R.string.response_54);
        responses.put("55", R.string.response_55);
        responses.put("58", R.string.response_58);
        responses.put("60", R.string.response_60);
        responses.put("76", R.string.response_76);
        responses.put("77", R.string.response_77);
        responses.put("78", R.string.response_78);
        responses.put("79", R.string.response_79);
        responses.put("80", R.string.response_80);
        responses.put("82", R.string.response_82);
        responses.put("83", R.string.response_83);
        responses.put("85", R.string.response_85);
        responses.put("88", R.string.response_88);
        responses.put("89", R.string.response_89);
        responses.put("91", R.string.response_91);
        responses.put("94", R.string.response_94);
        responses.put("95", R.string.response_95);
        responses.put("96", R.string.response_96);
        responses.put("97", R.string.response_97);
        //No idea what's this response codes...
        responses.put("N1", R.string.response_N1);
        responses.put("Q1", R.string.response_Q1);
        responses.put("Y1", R.string.response_Y1);
        responses.put("Z1", R.string.response_Z1);
        responses.put("Y2", R.string.response_Y2);
        responses.put("Z2", R.string.response_Z2);
        responses.put("Y3", R.string.response_Y3);
        responses.put("Z3", R.string.response_Z3);
        responses.put("NA", R.string.response_NA);
        responses.put("P0", R.string.response_P0);
        responses.put("XY", R.string.response_XY);
        responses.put("XX", R.string.response_XX);
        //TLE response codes
        responses.put("A0", R.string.response_A0);
        responses.put("A1", R.string.response_A1);
        responses.put("A2", R.string.response_A2);
        responses.put("A3", R.string.response_A3);
        responses.put("A4", R.string.response_A4);
        responses.put("A5", R.string.response_A5);
        responses.put("A6", R.string.response_A6);
        responses.put("A7", R.string.response_A7);
        responses.put("A8", R.string.response_A8);
        responses.put("A9", R.string.response_A9);
        responses.put("B0", R.string.response_B0);
        responses.put("B1", R.string.response_B1);
        responses.put("B2", R.string.response_B2);
        responses.put("B3", R.string.response_B3);
        responses.put("B4", R.string.response_B4);
        responses.put("B5", R.string.response_B5);
        responses.put("B6", R.string.response_B6);
        responses.put("B7", R.string.response_B7);
        responses.put("B8", R.string.response_B8);
        responses.put("B9", R.string.response_B9);
        responses.put("C0", R.string.response_C0);
        responses.put("C1", R.string.response_C1);
        responses.put("C2", R.string.response_C2);
        responses.put("C3", R.string.response_C3);
        responses.put("C4", R.string.response_C4);
        responses.put("C5", R.string.response_C5);
        responses.put("C6", R.string.response_C6);
        responses.put("C7", R.string.response_C7);
        responses.put("C8", R.string.response_C8);
        responses.put("C9", R.string.response_C9);
        responses.put("D0", R.string.response_D0);
        responses.put("D1", R.string.response_D1);
        responses.put("D2", R.string.response_D2);
        responses.put("D3", R.string.response_D3);
        responses.put("D4", R.string.response_D4);
        responses.put("D5", R.string.response_D5);
        responses.put("D9", R.string.response_D9);
        responses.put("E0", R.string.response_E0);
        responses.put("K0", R.string.response_K0);
        responses.put("KA", R.string.response_KA);
        responses.put("KB", R.string.response_KB);
        responses.put("KC", R.string.response_KC);
        responses.put("KD", R.string.response_KD);
        responses.put("KE", R.string.response_KE);
    }

    private ResponseCode() {
        if (map == null)
            map = new HashMap<>();
    }

    private ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ResponseCode getInstance() {
        if (rcCode == null) {
            rcCode = new ResponseCode();
        }
        return rcCode;
    }

    /**
     * init方法必须调用， 一般放在应用启动的时候
     */
    public void init() {
        for (String i : responses.keySet()) {
            String msg = findResponse(i);
            ResponseCode rspCode = new ResponseCode(i, msg);
            map.put(i, rspCode);
        }
    }

    /**
     * Parse response code.
     *
     * @param code the code
     * @return the response code
     */
    public ResponseCode parse(String code) {
        ResponseCode rc = map.get(code);
        if (rc == null)
            return new ResponseCode(code, Utils.getString(R.string.dialog_transaction_declined));
        return rc;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    private static String findResponse(final String code) {
        Integer id = responses.get(code);
        if (id == null) {
            id = R.string.response_unknown;
        }
        return FinancialApplication.getApp().getString(id);
    }

    @Override
    public String toString() {
        return this.getCode() + "\n" + this.getMessage();
    }
}
