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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.receipt.PrintListenerImpl;
import com.evp.pay.trans.receipt.paperless.ReceiptEmailTrans;
import com.evp.pay.utils.EmailInfo;

/**
 * The type Action send email.
 */
public class ActionSendEmail extends AAction {

    private Context context;
    private TransData transData;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionSendEmail(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context   the context
     * @param transData the trans data
     * @return the param
     */
    public ActionSendEmail setParam(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;
        return this;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {

            @Override
            public void run() {
                EmailInfo emailInfo = EmailInfo.generateSmtpInfo();
                ReceiptEmailTrans receiptEmailTrans = new ReceiptEmailTrans();
                PrintListenerImpl listener = new PrintListenerImpl(context);
                int ret = receiptEmailTrans.send(transData, emailInfo, false, listener);
                if (ret == 0) {
                    setResult(new ActionResult(TransResult.SUCC, transData));
                } else {
                    setResult(new ActionResult(TransResult.ERR_SEND, null));
                }
            }
        });
    }
}
