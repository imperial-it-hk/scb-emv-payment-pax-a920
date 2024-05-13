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
 * 20190108  	         lixc                    Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.EnterAuthCodeActivity;

/**
 * The type Action enter auth code.
 */
public class ActionEnterAuthCode extends AAction {
    private Context context;
    private String title;
    private String header;
    private String amount;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionEnterAuthCode(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param header  the header
     * @param amount  the amount
     */
    public void setParam(Context context, String title, String header, String amount) {
        this.context = context;
        this.title = title;
        this.header = header;
        this.amount = amount;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, EnterAuthCodeActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.PROMPT_1.toString(), header);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        context.startActivity(intent);
    }
}
