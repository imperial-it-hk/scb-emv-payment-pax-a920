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

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.SignatureActivity;

/**
 * The type Action signature.
 */
public class ActionSignature extends AAction {
    private String amount;
    private String point;
    private Context context;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionSignature(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param amount  the amount
     */
    public void setParam(Context context, String amount) {
        this.context = context;
        this.amount = amount;
    }

    public void setParam(Context context, String amount, String point) {
        this.context = context;
        this.amount = amount;
        this.point = point;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, SignatureActivity.class);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        if (point != null)
            intent.putExtra(EUIParamKeys.TRANS_POINT.toString(), point);
        context.startActivity(intent);
    }
}
