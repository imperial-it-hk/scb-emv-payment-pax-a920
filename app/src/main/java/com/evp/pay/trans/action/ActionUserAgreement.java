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
 * 20190108  	         huangwp                 Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.trans.action.activity.UserAgreementActivity;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

/**
 * The type Action user agreement.
 */
public class ActionUserAgreement extends AAction {
    private Context context;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionUserAgreement(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     */
    public void setParam(Context context) {
        this.context = context;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        //return immediately if it does not support user agreement
        if (!SysParam.getInstance().getBoolean(R.string.SUPPORT_USER_AGREEMENT)) {
            setResult(new ActionResult(TransResult.SUCC, null));
            return;
        }

        Intent intent = new Intent(context, UserAgreementActivity.class);
        context.startActivity(intent);
    }
}
