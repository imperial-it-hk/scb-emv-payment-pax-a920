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
package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.InputTransData1Activity;
import com.evp.pay.trans.action.activity.InputTransDataTipActivity;
import com.evp.pay.trans.action.activity.PaperlessActivity;
import com.evp.payment.evpscb.R;

import java.util.Map;

/**
 * The type Action input trans data.
 */
public class ActionInputTransData extends AAction {
    private Context context;
    private String title;
    private String prompt;
    private EInputType inputType;
    private int maxLen;
    private int minLen;
    private boolean isGetLastTrans;
    private boolean isAuthZero;
    private boolean isQrScan = false;
    private int tickTime;
    private Map<String, String> map;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionInputTransData(ActionStartListener listener) {
        super(listener);
    }

    /**
     * 输入数据类型定义
     *
     * @author Steven.W
     */
    public enum EInputType {
        /**
         * Amount e input type.
         */
        AMOUNT,
        /**
         * Num e input type.
         */
        NUM, // 数字
        /**
         * Text e input type.
         */
        TEXT, // 所有类型
        /**
         * Phone e input type.
         */
        PHONE,
        /**
         * Email e input type.
         */
        EMAIL,
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @return the param
     */
    public ActionInputTransData setParam(Context context, String title) {
        this.context = context;
        this.title = title;
        return this;
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param map     the map
     * @return the param
     */
    public ActionInputTransData setParam(Context context, String title, Map<String, String> map) {
        this.context = context;
        this.title = title;
        this.map = map;
        return this;
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param map     the map
     * @return the param
     */
    public ActionInputTransData setParam(Context context, String title, Map<String, String> map, int tickTime) {
        this.context = context;
        this.title = title;
        this.map = map;
        this.tickTime = tickTime;
        return this;
    }

    /**
     * Sets input line.
     *
     * @param prompt         the prompt
     * @param inputType      the input type
     * @param maxLen         the max len
     * @param isGetLastTrans the is get last trans
     * @return the input line
     */
    public ActionInputTransData setInputLine(String prompt, EInputType inputType, int maxLen, boolean isGetLastTrans) {
        return setInputLine(prompt, inputType, maxLen, 0, isGetLastTrans);
    }

    /**
     * Sets input line.
     *
     * @param prompt         the prompt
     * @param inputType      the input type
     * @param maxLen         the max len
     * @param minLen         the min len
     * @param isGetLastTrans the is get last trans
     * @return the input line
     */
    public ActionInputTransData setInputLine(String prompt, EInputType inputType, int maxLen, int minLen,
                                             boolean isGetLastTrans) {
        this.prompt = prompt;
        this.inputType = inputType;
        this.maxLen = maxLen;
        this.minLen = minLen;
        this.isGetLastTrans = isGetLastTrans;
        return this;
    }

    public ActionInputTransData setInputLine(String prompt, EInputType inputType, int maxLen, int minLen,
                                             boolean isGetLastTrans, boolean isQrScan) {
        this.prompt = prompt;
        this.inputType = inputType;
        this.maxLen = maxLen;
        this.minLen = minLen;
        this.isGetLastTrans = isGetLastTrans;
        this.isQrScan = isQrScan;
        return this;
    }

    /**
     * Sets input line 1.
     *
     * @param prompt         the prompt
     * @param inputType      the input type
     * @param maxLen         the max len
     * @param minLen         the min len
     * @param isGetLastTrans the is get last trans
     * @param isAuthZero     the is auth zero
     * @return the input line 1
     */
    public ActionInputTransData setInputLine1(String prompt, EInputType inputType, int maxLen, int minLen,
                                              boolean isGetLastTrans, boolean isAuthZero) {
        this.prompt = prompt;
        this.inputType = inputType;
        this.maxLen = maxLen;
        this.minLen = minLen;
        this.isGetLastTrans = isGetLastTrans;
        this.isAuthZero = isAuthZero;
        return this;
    }

    /**
     * action process
     */
    @Override
    protected void process() {

        FinancialApplication.getApp().runOnUiThread(new ProcessRunnable());
    }

    private class ProcessRunnable implements Runnable {

        @Override
        public void run() {
            if (inputType == EInputType.PHONE || inputType == EInputType.EMAIL) {
                runPaperless();
            } else if (inputType == EInputType.AMOUNT) {
                runTipStyle();
            } else {
                runStyle1();
            }
        }

        private void runPaperless() {
            Intent intent = new Intent(context, PaperlessActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt);
            bundle.putSerializable(EUIParamKeys.INPUT_TYPE.toString(), inputType);
            if (tickTime > 0) {
                bundle.putInt(EUIParamKeys.TIKE_TIME.toString(), tickTime);
            }
            intent.putExtras(bundle);
            context.startActivity(intent);
        }

        private void runStyle1() {
            Intent intent = new Intent(context, InputTransData1Activity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt);
            bundle.putInt(EUIParamKeys.INPUT_MAX_LEN.toString(), maxLen);
            bundle.putInt(EUIParamKeys.INPUT_MIN_LEN.toString(), minLen);
            bundle.putSerializable(EUIParamKeys.INPUT_TYPE.toString(), inputType);
            bundle.putBoolean(EUIParamKeys.GET_LAST_TRANS_UI.toString(), isGetLastTrans);
            bundle.putBoolean(EUIParamKeys.INPUT_PADDING_ZERO.toString(), isAuthZero);
            bundle.putBoolean(EUIParamKeys.IS_QR_SCAN.toString(), isQrScan);
            if (tickTime > 0) {
                bundle.putInt(EUIParamKeys.TIKE_TIME.toString(), tickTime);
            }
            intent.putExtras(bundle);
            context.startActivity(intent);
        }

        private void runTipStyle() {
            Intent intent = new Intent(context, InputTransDataTipActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            if (map != null) {
                String totalAmount = map.get(context.getString(R.string.prompt_total_amount));
                String oriTips = map.get(context.getString(R.string.prompt_ori_tips));
                String adjustPercent = map.get(context.getString(R.string.prompt_adjust_percent));
                bundle.putString(EUIParamKeys.TRANS_AMOUNT.toString(), totalAmount);
                bundle.putString(EUIParamKeys.ORI_TIPS.toString(), oriTips);
                bundle.putFloat(EUIParamKeys.TIP_PERCENT.toString(), Float.valueOf(adjustPercent));
            }
            if (tickTime > 0) {
                bundle.putInt(EUIParamKeys.TIKE_TIME.toString(), tickTime);
            }
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}
