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
 * 20190108  	         laiyi                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.DeviceImplNeptune;
import com.evp.eemv.IClss;
import com.evp.eemv.exception.EmvException;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.emv.clss.ClssTransProcess;
import com.evp.pay.trans.component.Component;
import com.evp.poslib.neptune.Sdk;
import com.pax.jemv.device.DeviceManager;

/**
 * clss pre processing action
 */
public class ActionClssPreProc extends AAction {


    private TransData transData;
    private IClss clss;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionClssPreProc(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param clss      the clss
     * @param transData the trans data
     */
    public void setParam(IClss clss, TransData transData) {
        this.clss = clss;
        this.transData = transData;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        if (Sdk.isPaxDevice())
            FinancialApplication.getApp().runInBackground(new ProcessRunnable());
        else
            setResult(new ActionResult(TransResult.SUCC, null));
    }

    private class ProcessRunnable implements Runnable {

        /**
         * Instantiates a new Process runnable.
         */
        ProcessRunnable() {
            DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());
        }

        @Override
        public void run() {
            try {
                clss.init();
                clss.setConfig(ClssTransProcess.genClssConfig());
                clss.setAidParamList(FinancialApplication.getAidParamList());
                clss.setCapkList(FinancialApplication.getCapkList());
                clss.preTransaction(Component.toClssInputParam(transData));
            } catch (EmvException e) {
                LogUtils.e(TAG, "", e);
                setResult(new ActionResult(TransResult.ERR_CLSS_PRE_PROC, null));
            }
            setResult(new ActionResult(TransResult.SUCC, null));
        }
    }

}
