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

import android.app.Activity;
import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.model.PrintType;

/**
 * The type Action print trans receipt.
 */
public class ActionPrintTransReceipt extends AAction {
    private Context context;
    private TransData transData;
    private boolean isReprint;
    private PrintType printType;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionPrintTransReceipt(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context   the context
     * @param transData the trans data
     */
    public void setParam(Context context, TransData transData, PrintType printType, boolean isReprint) {
        this.context = context;
        this.transData = transData;
        this.isReprint = isReprint;
        this.printType = printType;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {

            @Override
            public void run() {
                PrinterUtils.printTransDetail((Activity) context, transData, printType == PrintType.RECEIPT_CUSTOMER? 1: 0, isReprint);
                setResult(new ActionResult(TransResult.SUCC, transData));
            }
        });
    }

}
