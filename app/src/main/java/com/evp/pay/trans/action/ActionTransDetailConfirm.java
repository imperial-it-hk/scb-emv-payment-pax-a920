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
import com.evp.pay.trans.action.activity.TransDetailConfirmActivity;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * The type Action disp trans detail.
 */
public class ActionTransDetailConfirm extends AAction {
    private Context context;
    private Map<String, String> map;
    private String title;
    private String cardPAN;
    private String cardHolderName;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionTransDetailConfirm(ActionStartListener listener) {
        super(listener);
    }

    /**
     * 参数设置
     *
     * @param context ：应用上下文
     * @param title   ：抬头
     * @param map     ：确认信息
     */
    public void setParam(Context context, String title, Map<String, String> map, String cardPAN, String cardHolderName) {
        this.context = context;
        this.title = title;
        this.map = map;
        this.cardPAN = cardPAN;
        this.cardHolderName = cardHolderName;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runOnUiThread(new ProcessRunnable(map));
    }

    private class ProcessRunnable implements Runnable {
        private ArrayList<String> leftColumns = new ArrayList<>();
        private ArrayList<String> rightColumns = new ArrayList<>();

        /**
         * Instantiates a new Process runnable.
         *
         * @param promptValue the prompt value
         */
        ProcessRunnable(Map<String, String> promptValue) {
            updateColumns(promptValue);
        }


        @Override
        public void run() {
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);
            if (cardPAN != null) {
                bundle.putString("cardPAN", cardPAN);
            }
            if (cardHolderName != null && cardHolderName.length() > 1) {
                bundle.putString("cardHolderName", cardHolderName);
            }

            Intent intent = new Intent(context, TransDetailConfirmActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }

        private void updateColumns(Map<String, String> promptValue) {
            Set<Map.Entry<String, String>> entries = promptValue.entrySet();
            for (Map.Entry<String, String> next : entries) {
                leftColumns.add(next.getKey());
                String value = next.getValue();
                rightColumns.add(value == null ? "" : value);
            }
        }
    }
}
