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
import android.graphics.Bitmap;

import com.evp.abl.core.AAction;
import com.evp.bizlib.data.entity.TransData;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.PrintPreviewActivity;
import com.evp.pay.trans.model.PrintType;

/**
 * The type Action print preview.
 */
public class ActionPrintPreview extends AAction {

    private Context context;
    private TransData transData;
    private PrintType printType;
    private boolean isReprint;

    private Bitmap loadedBitmap = null;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionPrintPreview(ActionStartListener listener) {
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
        this.printType = printType;
        this.isReprint = isReprint;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, PrintPreviewActivity.class);
        intent.putExtra(EUIParamKeys.TRANSDATA.toString(), transData);
        intent.putExtra(EUIParamKeys.PRINT_TYPE.toString(), printType);
        intent.putExtra(EUIParamKeys.IS_REPRINT.toString(), isReprint);
        context.startActivity(intent);
    }
}
