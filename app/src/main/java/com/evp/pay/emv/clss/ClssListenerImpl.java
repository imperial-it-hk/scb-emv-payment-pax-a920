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
package com.evp.pay.emv.clss;

import android.content.Context;
import android.os.ConditionVariable;

import androidx.annotation.StringRes;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.dcc.DccUtils;
import com.evp.bizlib.params.ParamHelper;
import com.evp.bizlib.tpn.TpnUtils;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.device.DeviceImplNeptune;
import com.evp.eemv.IClss;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Tools;
import com.evp.eventbus.SearchCardEvent;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.emv.EmvBaseListenerImpl;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.Transmit;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.BuildConfig;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.exceptions.PedDevException;
import com.pax.jemv.clcommon.Clss_ProgramID_II;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.device.model.ApduRespL2;
import com.pax.jemv.device.model.ApduSendL2;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Clss listener.
 */
public class ClssListenerImpl extends EmvBaseListenerImpl implements IClssListener {
    private CTransResult result;
    private static final String TAG = "ClssListenerImpl";
    private IClss clss;
    private boolean detect2ndTap = false;

    /**
     * Instantiates a new Clss listener.
     *
     * @param context   the context
     * @param clss      the clss
     * @param transData the trans data
     * @param listener  the listener
     */
    public ClssListenerImpl(Context context, IClss clss, TransData transData, TransProcessListener listener) {
        super(context, clss, transData, listener);
        this.clss = clss;
    }

    @Override
    public int onCvmResult(ECvmResult result) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        intResult = 0;

        if (result == ECvmResult.ONLINE_PIN || result == ECvmResult.ONLINE_PIN_SIG) {
            cv = new ConditionVariable();
            enterPin(true, 0, null);
            cv.block(); // for the Offline pin case, block it for make sure the PIN activity is ready, otherwise, may get the black screen.
        }

        if(result != ECvmResult.SIG
                && result != ECvmResult.ONLINE_PIN_SIG
                && result != ECvmResult.REQ_SIG) {
            transData.setSignFree(true);
        }

        return intResult;
    }

    @Override
    public void onComfirmCardInfo(String track1, String track2, String track3) throws EmvException {
        transData.setTrack1(track1);
        transData.setTrack2(track2);
        transData.setTrack3(track3);

        String pan = TrackUtils.getPan(track2);
        transData.setPan(pan);

        //TPN and TSC logic
        boolean failure = false;
        byte[] aid = clss.getTlv(TagsTable.AID);
        if(TpnUtils.isThisTpnCard(aid, pan)) {
            Acquirer acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.UPI_ACQUIRER);
            Issuer issuer = FinancialApplication.getAcqManager().findIssuer(AppConstants.TPN_ISSUER);
            if(!TpnUtils.setTpnAcqAndIssuer(acq, issuer, transData)) {
                failure = true;
            } else {
                FinancialApplication.getAcqManager().setCurAcq(acq);
            }
        } else if(TpnUtils.isThisTscCard(aid, pan)) {
            Acquirer acq;
            Issuer issuer = FinancialApplication.getAcqManager().findIssuer(AppConstants.TSC_ISSUER);
            switch (ConvertUtils.enumValue(ETransType.class, transData.getTransType())) {
                case INSTALLMENT:
                    acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.IPP_ACQUIRER);
                    break;
                case OLS_ENQUIRY:
                case REDEEM:
                    acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.OLS_ACQUIRER);
                    break;
                default:
                    acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.SCB_ACQUIRER);
                    break;
            }
            if(!TpnUtils.setTscAcqAndIssuer(acq, issuer, transData)) {
                failure = true;
            } else {
                FinancialApplication.getAcqManager().setCurAcq(acq);
            }
        } else {
            if(FinancialApplication.getAcqManager().findIssuerAndSetAcquirerByPan(pan, transData) == null) {
                failure = true;
            }
        }
        if (failure) {
            throw new EmvException(EEmvExceptions.EMV_ERR_DATA);
        }

        String expDate = TrackUtils.getExpDate(transData.getTrack2());
        transData.setExpDate(expDate);
        if (!Component.isDemo() &&
                (!Issuer.validPan(transData.getIssuer(), pan) ||
                        !Issuer.validCardExpiry(transData.getIssuer(), expDate))) {
            throw new EmvException(EEmvExceptions.EMV_ERR_CLSS_CARD_EXPIRED);
        }

        //PanSeqNo
        byte[] value = clss.getTlv(TagsTable.PAN_SEQ_NO);
        if (value != null) {
            String cardSerialNo = ConvertHelper.getConvert().bcdToStr(value);
            transData.setCardSerialNo(cardSerialNo.substring(0, value.length * 2));
        }
    }

    @Override
    protected void updateTransDataFromKernel() throws PedDevException {
        new Transmit().sendReversal(transData.getAcquirer(), transProcessListener);
        ClssTransProcess.clssTransResultProcess(result, clss, transData);
    }

    @Override
    public EOnlineResult onOnlineProc(CTransResult result) throws PedDevException {
        this.result = result;
        return onlineProc();
    }

    @Override
    public boolean onDetect2ndTap() {
        final ConditionVariable cv = new ConditionVariable();
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                if (transData.getEnterMode() == TransData.EnterMode.CLSS && transProcessListener != null) {
                    transProcessListener.onShowProgress(context.getString(R.string.prompt_wave_card), 30);
                }
                try {
                    //tap card
                    ICardReaderHelper helper = FinancialApplication.getDal().getCardReaderHelper();
                    helper.polling(ParamHelper.isClssInternalResult() ? EReaderType.PICC : EReaderType.PICCEXTERNAL, 30 * 1000);
                    helper.stopPolling();
                    detect2ndTap = true;
                } catch (Exception e) {
                    LogUtils.e(TAG, "", e);
                } finally {
                    if (transProcessListener != null) {
                        transProcessListener.onHideProgress();
                    }
                    cv.open();
                }
            }
        });
        cv.block();
        return detect2ndTap;
    }

    @Override
    public int onIssScrCon() {
        ApduSendL2 apduSendL2 = new ApduSendL2();
        ApduRespL2 apduRespL2 = new ApduRespL2();
        byte[] sendCommand = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00};
        System.arraycopy(sendCommand, 0, apduSendL2.command, 0, sendCommand.length);
        apduSendL2.lc = 14;
        String sendDataIn = "1PAY.SYS.DDF01";
        System.arraycopy(sendDataIn.getBytes(), 0, apduSendL2.dataIn, 0, sendDataIn.getBytes().length);
        apduSendL2.le = 256;
        int ret = (int) DeviceImplNeptune.getInstance().iccCommand(apduSendL2, apduRespL2);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        if (apduRespL2.swa != (byte) 0x90 || apduRespL2.swb != 0x00)
            return RetCode.EMV_RSP_ERR;

        apduSendL2 = new ApduSendL2();
        apduRespL2 = new ApduRespL2();
        System.arraycopy(sendCommand, 0, apduSendL2.command, 0, sendCommand.length);
        apduSendL2.lc = 14;
        System.arraycopy(transData.getAid().getBytes(), 0, apduSendL2.dataIn, 0, transData.getAid().getBytes().length);
        apduSendL2.le = 256;
        ret = (int) DeviceImplNeptune.getInstance().iccCommand(apduSendL2, apduRespL2);
        if (ret != RetCode.EMV_OK)
            return ret;

        if (apduRespL2.swa != (byte) 0x90 || apduRespL2.swb != 0x00)
            return RetCode.EMV_RSP_ERR;

        return RetCode.EMV_OK;
    }

    @Override
    public void onPromptRemoveCard() {
        if (BuildConfig.needRemoveCard){
            onPrompt(R.string.wait_remove_card);
        }
    }

    @Override
    public void onPromptRetry() {
        onPrompt(R.string.prompt_please_retry);
    }

    private void onPrompt(@StringRes final int resId) {
        final ConditionVariable cv = new ConditionVariable();
        FinancialApplication.getApp().doEvent(new SearchCardEvent(SearchCardEvent.Status.CLSS_LIGHT_STATUS_REMOVE_CARD));
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                Device.removeCard(new Device.RemoveCardListener() {
                    @Override
                    public void onShowMsg(PollingResult result) {
                        if (transProcessListener != null) {
                            transProcessListener.onHideProgress();
                            if (FinancialApplication.getCurrentETransType() != null) {
                                transProcessListener.onUpdateProgressTitle(FinancialApplication.getCurrentETransType().getTransName());
                            }
                            transProcessListener.onShowWarning(context.getString(resId), -1);
                        }
                    }
                });
                if (transProcessListener != null) {
                    transProcessListener.onHideMessage();
                }
                cv.open();
            }
        });
        cv.block();
    }

    @Override
    public int onDisplaySeePhone() {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showMessage("Please See Phone");
            }
        });
        return 0;
    }

    @Override
    public List<Clss_ProgramID_II> onGetProgramId() {
        List<ClssProgramId> clssProgramIdList = Utils.readObjFromJSON("clssProgramId.json", ClssProgramId.class);
        if (clssProgramIdList.isEmpty()) {
            return null;
        }
        List<Clss_ProgramID_II> list = new ArrayList<>();
        for (ClssProgramId i : clssProgramIdList) {
            list.add(clssExpressGetProgramInfo(i));
        }
        return list;
    }

    private Clss_ProgramID_II clssExpressGetProgramInfo(ClssProgramId clssProgramId) {
        Clss_ProgramID_II value = new Clss_ProgramID_II();
        value.aucRdClssTxnLmt = Tools.str2Bcd(clssProgramId.getAucRdClssTxnLmt());
        value.aucRdCVMLmt = Tools.str2Bcd(clssProgramId.getAucRdCVMLmt());
        value.aucRdClssFLmt = Tools.str2Bcd(clssProgramId.getAucRdClssFLmt());
        value.aucTermFLmt = Tools.str2Bcd(clssProgramId.getAucTermFLmt());
        value.aucProgramId = Tools.str2Bcd(clssProgramId.getAucProgramId());
        value.ucPrgramIdLen = (byte) clssProgramId.getUcPrgramIdLen();
        value.ucRdClssFLmtFlg = (byte) clssProgramId.getUcRdClssFLmtFlg();
        value.ucRdClssTxnLmtFlg = (byte) clssProgramId.getUcRdClssTxnLmtFlg();
        value.ucRdCVMLmtFlg = (byte) clssProgramId.getUcRdCVMLmtFlg();
        value.ucTermFLmtFlg = (byte) clssProgramId.getUcTermFLmtFlg();
        value.ucStatusCheckFlg = (byte) clssProgramId.getUcStatusCheckFlg();
        value.ucAmtZeroNoAllowed = (byte) clssProgramId.getUcAmtZeroNoAllowed();
        value.ucDynamicLimitSet = (byte) clssProgramId.getUcDynamicLimitSet();
        value.ucRFU = (byte) clssProgramId.getUcRFU();
        return value;
    }

    @Override
    public boolean onCheckDemoMode() {
        return Component.isDemo();
    }

    @Override
    public final int onDcc() throws PedDevException {
        Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(AppConstants.DCC_ACQUIRER);
        if(!DccUtils.isTrxDcc(acquirer, transData.getTransType())) {
            return 0;
        }

        TransData backupTransData = transData;

        transData = new TransData(transData);
        transData.setTransType(ETransType.DCC_GET_RATE.name());
        transData.setProcCode(ETransType.DCC_GET_RATE.getProcCode());
        transData.setAcquirer(acquirer);
        transData.setNii(acquirer.getNii());

        EOnlineResult ret = dccOnlineProc();
        if(ret != EOnlineResult.APPROVE) {
            backupTransData.setStanNo(Component.getStanNo());
            transData = backupTransData;
            return 0;
        }

        backupTransData.setDccExchangeRate(transData.getDccExchangeRate());
        backupTransData.setDccForeignAmount(transData.getDccForeignAmount());
        backupTransData.setDccCurrencyCode(transData.getDccCurrencyCode());
        backupTransData.setStanNo(Component.getStanNo());
        transData = backupTransData;

        cv = new ConditionVariable();
        intResult = 0;

        dccGetCardholderDecision();

        cv.block();
        return intResult;
    }
}
