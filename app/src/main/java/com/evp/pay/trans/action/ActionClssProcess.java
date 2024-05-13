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

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.EnterMode;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.params.ParamHelper;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.device.DeviceImplNeptune;
import com.evp.eemv.IClss;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.eventbus.SearchCardEvent;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.emv.clss.ClssListenerImpl;
import com.evp.pay.emv.clss.ClssTransProcess;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.utils.ResponseCode;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.view.ClssLight;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.jemv.device.DeviceManager;

/**
 * The type Action clss process.
 */
public class ActionClssProcess extends AAction {
    private Context context;
    private IClss clss;
    private TransData transData;
    private TransProcessListener transProcessListener;
    private ClssListenerImpl clssListener;
    private boolean isInternalPicc = true;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionClssProcess(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context   the context
     * @param clss      the clss
     * @param transData the trans data
     */
    public void setParam(Context context, IClss clss, TransData transData) {
        this.context = context;
        this.clss = clss;
        this.transData = transData;
        transProcessListener = new TransProcessListenerImpl(context);
        clssListener = new ClssListenerImpl(context, clss, transData, transProcessListener);
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        DeviceImplNeptune implDevice = DeviceImplNeptune.getInstance();
        isInternalPicc = ParamHelper.isClssInternal() || (ParamHelper.isClssBothSupport() && ParamHelper.isClssInternalResult());
        implDevice.setInternalPicc(isInternalPicc);
        DeviceManager.getInstance().setIDevice(implDevice);
        FinancialApplication.getApp().runInBackground(new ProcessRunnable());
    }

    private class ProcessRunnable implements Runnable {
        private final ClssTransProcess clssTransProcess;

        /**
         * Instantiates a new Process runnable.
         */
        ProcessRunnable() {
            if (transData.getEnterMode() == EnterMode.CLSS) {
                transProcessListener.onShowProgress(context.getString(R.string.wait_process), 0);
            }
            clssTransProcess = new ClssTransProcess(clss);
        }

        @Override
        public void run() {
            try {
                FinancialApplication.getApp().doEvent(new SearchCardEvent(SearchCardEvent.Status.CLSS_LIGHT_STATUS_PROCESSING));
                CTransResult result = clssTransProcess.transProcess(transData, clssListener);
                Device.beepPrompt();
                //AET-260
                updateReversalStatus();
                setResult(new ActionResult(TransResult.SUCC, result));
                FinancialApplication.getApp().doEvent(new SearchCardEvent(SearchCardEvent.Status.CLSS_LIGHT_STATUS_COMPLETE));
            } catch (EmvException e) {
                LogUtils.e(TAG, "", e);

                if (transData.isOnlineTrans() && Component.isDemo()) {
                    updateReversalStatus();
                    setResult(new ActionResult(TransResult.SUCC, new CTransResult(ETransResult.ONLINE_APPROVED)));
                    return;
                }

                FinancialApplication.getApp().doEvent(new SearchCardEvent(SearchCardEvent.Status.CLSS_LIGHT_STATUS_ERROR));
                if (e.getErrCode() == EEmvExceptions.EMV_ERR_DENIAL.getErrCodeFromBasement()){
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
                    transProcessListener.onShowErrMessage(responseCode.getMessage(),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_CLSS_USE_CONTACT.getErrCodeFromBasement()){
                    transProcessListener.onShowNormalMessage(
                                    Utils.getString(R.string.err_host_reject)
                                    + System.getProperty("line.separator")
                                    + Utils.getString(R.string.dialog_clss_try_contact),
                            Constants.FAILED_DIALOG_SHOW_TIME_LONG,
                            true);
                } else {
                    transProcessListener.onShowErrMessage(e.getMessage(),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                setResult(new ActionResult(TransResult.ERR_ABORTED, null));
            } finally {
                try {
                    if (isInternalPicc){
                        FinancialApplication.getDal().getPicc(EPiccType.INTERNAL).close();
                    }else {
                        FinancialApplication.getDal().getPicc(EPiccType.EXTERNAL).close();
                    }
                } catch (PiccDevException e) {
                    LogUtils.e("ActionClssProcess","close picc after cls process,error:"+e);
                }
                Device.setPiccLed(-1, ClssLight.OFF);
                byte[] value95 = clss.getTlv(0x95);
                byte[] value9B = clss.getTlv(0x9B);

                LogUtils.e("TLV", "95:" + ConvertHelper.getConvert().bcdToStr(value95));
                LogUtils.e("TLV", "9b:" + ConvertHelper.getConvert().bcdToStr(value9B));

                // no memory leak
                clss.setListener(null);
                transProcessListener.onHideProgress();
            }

        }

        private void updateReversalStatus() {
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            transData.setDupReason("");
            GreendaoHelper.getTransDataHelper().update(transData);
        }
    }
}
