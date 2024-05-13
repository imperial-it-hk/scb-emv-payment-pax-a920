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
import com.evp.bizlib.data.entity.TransData.EnterMode;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.device.DeviceImplNeptune;
import com.evp.eemv.EmvImpl;
import com.evp.eemv.IEmv;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.eventbus.EmvCallbackEvent;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.emv.EmvListenerImpl;
import com.evp.pay.emv.EmvTransProcess;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.utils.ResponseCode;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.pax.dal.exceptions.IccDevException;
import com.pax.jemv.device.DeviceManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * The type Action emv process.
 */
public class ActionEmvProcess extends AAction {
    private Context context;
    private IEmv emv;
    private TransData transData;
    private TransProcessListener transProcessListener;
    private EmvListenerImpl emvListener;
    private EmvCallbackEvent.Status status;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionEmvProcess(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context   the context
     * @param emv       the emv
     * @param transData the trans data
     */
    public void setParam(Context context, IEmv emv, TransData transData) {
        this.context = context;
        this.emv = emv;
        this.transData = transData;
        transProcessListener = new TransProcessListenerImpl(context);
        emvListener = new EmvListenerImpl(context, emv, transData, transProcessListener);
    }

    /**
     * On card num confirm event.
     *
     * @param event the event
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onCardNumConfirmEvent(EmvCallbackEvent event) {
        status = (EmvCallbackEvent.Status) event.getStatus();
        switch (status) {
            case OFFLINE_PIN_ENTER_READY:
                emvListener.offlinePinEnterReady();
                break;
            case CARD_NUM_CONFIRM_SUCCESS:
                emvListener.cardNumConfigSucc((String[]) event.getData());
                break;
            case TIMEOUT:
                emvListener.onTimeOut();
                break;
            case CARD_NUM_CONFIRM_ERROR:
            default:
                emvListener.cardNumConfigErr();
                break;
        }
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());
        FinancialApplication.getApp().runInBackground(new ProcessRunnable());
    }

    private class ProcessRunnable implements Runnable {
        private EmvTransProcess emvTransProcess;

        /**
         * Instantiates a new Process runnable.
         */
        ProcessRunnable() {
            if (transData.getEnterMode() == EnterMode.INSERT) {
                transProcessListener.onShowProgress(context.getString(R.string.wait_process), 0);
            }
            emvTransProcess = new EmvTransProcess(emv);
            emvTransProcess.init();
        }

        @Override
        public void run() {
            try {
                FinancialApplication.getApp().register(ActionEmvProcess.this);
                CTransResult result = emvTransProcess.transProcess(transData, emvListener);
                transProcessListener.onHideProgress();

                if (result.getTransResult() != ETransResult.ABORT_TERMINATED) {
                    //AET-260
                    updateReversalStatus();
                    setResult(new ActionResult(TransResult.SUCC, result));
                }
                //reset the isTimeout state
                EmvImpl.isTimeOut = false;

            } catch (EmvException e) {
                LogUtils.e(TAG, "", e);
                handleException(e);
            } finally {
                try {
                    FinancialApplication.getDal().getIcc().close((byte) 0);
                } catch (IccDevException e) {
                    LogUtils.e("ActionEmvProcess","close icc after emv process,error:"+e);
                }
                byte[] value95 = emv.getTlv(0x95);
                byte[] value9B = emv.getTlv(0x9B);

                LogUtils.e("TLV", "95:" + ConvertHelper.getConvert().bcdToStr(value95));
                LogUtils.e("TLV", "9b:" + ConvertHelper.getConvert().bcdToStr(value9B));

                // no memory leak
                emv.setListener(null);
                FinancialApplication.getApp().unregister(ActionEmvProcess.this);
            }
        }

        private void handleException(EmvException e) {
            EmvImpl.isTimeOut = false;
            ActivityStack.getInstance().popTo((Activity) context);
            if (Component.isDemo() &&
                    e.getErrCode() == EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement()) {
                transProcessListener.onHideProgress();
                updateReversalStatus();
                // end the EMV process, and continue a mag process
                setResult(new ActionResult(TransResult.SUCC, ETransResult.ARQC));
                return;
            }

            Device.beepErr();
            if (status == EmvCallbackEvent.Status.TIMEOUT) {
                // AET-312
                transProcessListener.onShowErrMessage(context.getString(R.string.trans_sale), context.getString(R.string.err_timeout), Constants.FAILED_DIALOG_SHOW_TIME, false);
            } else if (e.getErrCode() != EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement()) {
                if (e.getErrCode() == EEmvExceptions.EMV_ERR_FALL_BACK.getErrCodeFromBasement()) {
                    transProcessListener.onShowErrMessage(context.getString(R.string.prompt_fall_back), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    transProcessListener.onHideProgress();
                    setResult(new ActionResult(TransResult.NEED_FALL_BACK, null));
                    return;
                } else if (e.getErrCode() == EEmvExceptions.EMV_ERR_DENIAL.getErrCodeFromBasement()){
                    ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
                    if (responseCode != null) {
                        transProcessListener.onShowErrMessage(responseCode.getMessage(),
                                Constants.FAILED_DIALOG_SHOW_TIME, false);
                    } else {
                        transProcessListener.onShowErrMessage(e.getErrMsg(),
                                Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                }else {
                    transProcessListener.onShowErrMessage(e.getErrMsg(),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
            }

            transProcessListener.onHideProgress();
            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
        }

        private void updateReversalStatus() {
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            transData.setDupReason("");
            GreendaoHelper.getTransDataHelper().update(transData);
        }
    }
}

