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

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.model.ETransType;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.component.Component;

/**
 * 下载参数action
 *
 * @author Steven.W
 */
public class ActionTransPreDeal extends AAction {
    private Context context;
    private ETransType transType;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionTransPreDeal(ActionStartListener listener) {
        super(listener);
    }

    /**
     * 设置action运行时参数
     *
     * @param context   context
     * @param transType transType
     */
    public void setParam(Context context, ETransType transType) {
        this.context = context;
        this.transType = transType;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(() -> {
            int ret = Component.transPreDeal(context, transType);
            setResult(new ActionResult(ret, null));
        });
    }

}
