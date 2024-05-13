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
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.device.Device;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.payment.evpscb.R;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;

/**
 * result boolean : continue or not
 */
public class ActionRemoveCard extends AAction {
    private Context context;
    private String title;
    private TransProcessListener transProcessListener;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionRemoveCard(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context the context
     * @param title   the title
     */
    public void setParam(Context context, String title) {
        this.context = context;
        this.title = title;
        transProcessListener = new TransProcessListenerImpl(context);
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        Device.removeCard(new Device.RemoveCardListener() {
            @Override
            public void onShowMsg(PollingResult result) {
                transProcessListener.onUpdateProgressTitle(title);
                transProcessListener.onShowWarning(result.getReaderType() == EReaderType.ICC
                        ? context.getString(R.string.wait_pull_card)
                        : context.getString(R.string.wait_remove_card), -1);
            }
        });
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        setResult(new ActionResult(TransResult.SUCC, null));
    }

}
