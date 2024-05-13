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
import android.text.TextUtils;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.TickTimer;
import com.evp.pay.utils.lightscanner.tool.LightScanner;

/**
 * Action for scan code
 */
public class ActionScanQRCode extends AAction {
    private Context context;
    private TransData transData;

    /**
     * set context
     *
     * @param context context
     */
    public void setParam(Context context) {
        this.context = context;
    }

    /**
     * set default params
     *  @param context context
     * @param transData
     */
    public void setParam(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;
    }

    /**
     * set action start listener
     *
     * @param listener action start listener
     */
    public ActionScanQRCode(ActionStartListener listener) {
        super(listener);
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                final LightScanner lightScanner = new LightScanner(context, TickTimer.DEFAULT_TIMEOUT); //1分钟超时时间
                lightScanner.open();
                lightScanner.start(new LightScanner.LightScannerListener() {
                    @Override
                    public void onReadSuccess(final String result) {
                        lightScanner.close();

                        FinancialApplication.getApp().runOnUiThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    setResult(new ActionResult(TransResult.SUCC, result));
                                } else {
                                    setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                                }
                            }
                        }, 100);

                    }

                    @Override
                    public void onReadError() {
                        lightScanner.close();
                        FinancialApplication.getApp().runOnUiThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                            }
                        }, 100);
                    }

                    @Override
                    public void onCancel() {
                        lightScanner.close();
                        FinancialApplication.getApp().runOnUiThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                setResult(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                            }
                        }, 100);

                    }

                    /**
                     * on timeOut
                     */
                    @Override
                    public void onTimeOut() {
                        lightScanner.close();
                        FinancialApplication.getApp().runOnUiThreadDelay(new Runnable() {
                            @Override
                            public void run() {
                                setResult(new ActionResult(TransResult.ERR_TIMEOUT, null));
                            }
                        }, 100);
                    }
                });
            }
        });
    }

}
