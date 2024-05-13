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
 * 20190108  	         Steven.S                Create
 * ===========================================================================================
 */

package com.evp.pay.utils.lightscanner.tool;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.evp.pay.utils.lightscanner.LightScannerActivity;

/**
 * Created by Steven.S on 2018/5/24/0024.
 */
public class FinishListener implements DialogInterface.OnClickListener, OnCancelListener, Runnable {
    private final Activity activityToFinish;

    /**
     * Instantiates a new Finish listener.
     *
     * @param activityToFinish the activity to finish
     */
    public FinishListener(Activity activityToFinish){
        this.activityToFinish = activityToFinish;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        run();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        run();
    }

    @Override
    public void run() {
        ((LightScannerActivity) activityToFinish).sendScanTimeoutBroadcast();
    }

}
