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
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.SettleActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Action settle.
 */
public class ActionSettle extends AAction {
    private Context context;
    private String title;
    private List<String> list;
    private boolean thisIsAutoSettle = false;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionSettle(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     * @param list    the list
     */
    public void setParam(Context context, String title, List<String> list, boolean thisIsAutoSettle) {
        this.context = context;
        this.title = title;
        this.list = list;
        this.thisIsAutoSettle = thisIsAutoSettle;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runOnUiThread(() -> {
            if(!thisIsAutoSettle) {
                if (GreendaoHelper.getTransDataHelper().countOf() == 0) {
                    setResult(new ActionResult(TransResult.ERR_NO_TRANS, null));
                    return;
                }
            }

            Intent intent = new Intent(context, SettleActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            if(thisIsAutoSettle) {
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), false);
            } else {
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            }
            bundle.putBoolean(EUIParamKeys.AUTO_SETTLE.toString(), thisIsAutoSettle);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), new ArrayList<>(list));

            intent.putExtras(bundle);
            context.startActivity(intent);
        });

    }
}
