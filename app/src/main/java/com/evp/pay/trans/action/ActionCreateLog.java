/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2018-1-15
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.evp.pay.trans.action;

import android.content.Context;
import android.content.DialogInterface;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.constant.Constants;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.evp.view.dialog.DialogUtils;

public class ActionCreateLog extends AAction {
    private Context context;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionCreateLog(ActionStartListener listener) {
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

    @Override
    protected void process() {
        if (SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_SAVE_LOG)) {
            int index = (int) SysParam.getInstance().get(R.string.LOG_FILE_INDEX, 1);
            index++;
            SysParam.getInstance().set(R.string.LOG_FILE_INDEX, index);
            DialogUtils.showSuccMessage(context, context.getString(R.string.create_log), new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    setResult(new ActionResult(TransResult.SUCC, null));
                }
            }, Constants.SUCCESS_DIALOG_SHOW_TIME);
        } else {
            DialogUtils.showErrMessage(context, null, context.getString(R.string.create_log), new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                }
            }, Constants.FAILED_DIALOG_SHOW_TIME);
        }

    }
}
