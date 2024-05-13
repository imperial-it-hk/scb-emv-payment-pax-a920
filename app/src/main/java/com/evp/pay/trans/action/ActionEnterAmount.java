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
import com.evp.pay.trans.action.activity.EnterAmountActivity;

/**
 * The type Action enter amount.
 */
public class ActionEnterAmount extends AAction {
    private Context context;
    private String title;
    private boolean hasTip;
    private float percent;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionEnterAmount(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param hasTip  the has tip
     * @param percent the percent
     */
    public void setParam(Context context, String title, boolean hasTip, float percent) {
        this.context = context;
        this.title = title;
        this.hasTip = hasTip;
        this.percent = percent;
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param hasTip  the has tip
     */
    public void setParam(Context context, String title, boolean hasTip) {
        this.context = context;
        this.title = title;
        this.hasTip = hasTip;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, EnterAmountActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.HAS_TIP.toString(), hasTip);
        if (hasTip) {
            intent.putExtra(EUIParamKeys.TIP_PERCENT.toString(), percent);
        }
        context.startActivity(intent);
    }
}
