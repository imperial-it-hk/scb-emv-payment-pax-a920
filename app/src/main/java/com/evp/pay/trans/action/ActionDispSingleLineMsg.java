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
import com.evp.pay.trans.action.activity.DispVersionActivity;

/**
 * The type Action disp single line msg.
 */
public class ActionDispSingleLineMsg extends AAction {
    private Context context;
    private String title;
    private String prompt;
    private String content;
    private int tiketime;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionDispSingleLineMsg(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context  the context
     * @param title    the title
     * @param prompt   the prompt
     * @param content  the content
     * @param tiketime the tiketime
     */
    public void setParam(Context context, String title, String prompt, String content, int tiketime) {
        this.context = context;
        this.title = title;
        this.prompt = prompt;
        this.content = content;
        this.tiketime = tiketime;
    }
    /**
     * action process
     */
    @Override
    protected void process() {

        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Intent intent = new Intent(context, DispVersionActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt);
                bundle.putString(EUIParamKeys.CONTENT.toString(), content);
                bundle.putInt(EUIParamKeys.TIKE_TIME.toString(), tiketime);
                intent.putExtras(bundle);
                context.startActivity(intent);

            }
        });

    }

}
