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
import com.evp.pay.trans.action.activity.AdjustTipActivity;

/**
 * adjust tip action
 */
public class ActionAdjustTip extends AAction {
    private Context context;
    private String title;
    private String amount;
    //adjust tip percent
    private float percent;

    /**
     * Instantiates a new Action adjust tip.
     *
     * @param listener the listener
     */
    public ActionAdjustTip(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param amount  the amount
     * @param percent the percent
     */
    public void setParam(Context context, String title, String amount, float percent) {
        this.context = context;
        this.title = title;
        this.amount = amount;
        this.percent = percent;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, AdjustTipActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        intent.putExtra(EUIParamKeys.TIP_PERCENT.toString(), percent);
        context.startActivity(intent);
    }
}
