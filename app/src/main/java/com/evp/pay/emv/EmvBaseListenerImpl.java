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
 * 20190108  	         linhb                   Create
 * ===========================================================================================
 */
package com.evp.pay.emv;

import android.app.Activity;
import android.content.Context;
import android.os.ConditionVariable;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.params.ParamHelper;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.DateUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.IEmvBase;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.action.ActionDcc;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.transmit.Online;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.Transmit;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.impl.GL;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.ITlv;
import com.pax.gl.pack.exception.TlvException;

import java.util.Locale;


/**
 * The type Emv base listener.
 */
public class EmvBaseListenerImpl {
    private static final String TAG = EmvBaseListenerImpl.class.getSimpleName();
    /**
     * The Context.
     */
    protected Context context;
    /**
     * The Cv.
     */
    protected ConditionVariable cv;
    /**
     * The Int result.
     */
    protected int intResult;

    /**
     * The Trans data.
     */
    protected TransData transData;
    /**
     * The E trans type.
     */
    protected ETransType eTransType;
    private IEmvBase emvBase;
    /**
     * The Trans process listener.
     */
    protected TransProcessListener transProcessListener;

    /**
     * Instantiates a new Emv base listener.
     *
     * @param context   the context
     * @param emvBase   the emv base
     * @param transData the trans data
     * @param listener  the listener
     */
    protected EmvBaseListenerImpl(Context context, IEmvBase emvBase, TransData transData, TransProcessListener listener) {
        this.context = context;
        this.transData = transData;
        this.eTransType = ConvertUtils.enumValue(ETransType.class,transData.getTransType());
        this.emvBase = emvBase;
        this.transProcessListener = listener;
    }

    /**
     * Update trans data from kernel.
     *
     * @throws PedDevException the ped dev exception
     */
    protected void updateTransDataFromKernel() throws PedDevException {
        // read ARQC
        byte[] arqc = emvBase.getTlv(0x9F26);
        if (arqc != null && arqc.length > 0) {
            transData.setArqc(ConvertHelper.getConvert().bcdToStr(arqc));
        }

        // generate field 55 data
        byte[] f55 = EmvTags.getF55(emvBase, eTransType, false);
        byte[] f55Dup = EmvTags.getF55(emvBase, eTransType, true);

        // reversal process
        new Transmit().sendReversal(transData.getAcquirer(), transProcessListener);

        transData.setSendIccData(ConvertHelper.getConvert().bcdToStr(f55));
        if (f55Dup.length > 0) {
            transData.setDupIccData(ConvertHelper.getConvert().bcdToStr(f55Dup));
        }
    }

    /**
     * Update trans data from resp.
     *
     * @param list the list
     * @throws EmvException the emv exception
     */
    protected void updateTransDataFromResp(ITlv.ITlvDataObjList list) throws EmvException {
        byte[] value91 = list.getValueByTag(0x91);
        if (value91 != null && value91.length > 0) {
            emvBase.setTlv(0x91, value91);
        }
        // set script 71
        byte[] value71 = list.getValueByTag(0x71);
        if (value71 != null && value71.length > 0) {
            emvBase.setTlv(0x71, value71);
        }

        // set script 72
        byte[] value72 = list.getValueByTag(0x72);
        if (value72 != null && value72.length > 0) {
            emvBase.setTlv(0x72, value72);
        }
    }

    /**
     * Online proc e online result.
     *
     * @return the e online result
     * @throws PedDevException the ped dev exception
     */
    protected EOnlineResult onlineProc() throws PedDevException {
        boolean reversalRequired = false;
        try {
            LogUtils.i(TAG, "Acquirer used for online operation is: " + FinancialApplication.getAcqManager().getCurAcq().getName());

            updateTransDataFromKernel();

            // online process
            boolean onlineDenial = false;
            if (transProcessListener != null) {
                transProcessListener.onUpdateProgressTitle(eTransType.getTransName());
            }
            int ret = new Online().online(transData, transProcessListener);
            LogUtils.i(TAG, "Online  ret = " + ret);
            if (ret == TransResult.SUCC) {
                if (!"00".equals(transData.getResponseCode())) {
                    onlineDenial = true;
                }
            } else {
                if ("55".equals(transData.getResponseCode())) {
                    return EOnlineResult.FAILED;
                }
                return EOnlineResult.ABORT;
            }

            byte[] rspF55 = transData.getRecvIccData();
            LogUtils.hex(TAG, "rspF55", rspF55);
            ITlv tlv = GL.getGL().getPacker().getTlv();

            if (rspF55 != null && rspF55.length > 0) {
                ITlv.ITlvDataObjList list = tlv.unpack(rspF55);
                updateTransDataFromResp(list);
            }

            if (onlineDenial) {
                GreendaoHelper.getTransDataHelper().deleteDupRecord(transData.getAcquirer());
                if ("65".equals(transData.getResponseCode())) {
                    return EOnlineResult.TRY_OTHER_INTERFACE;
                }
                Device.beepErr();
                return EOnlineResult.DENIAL;
            }
            // set auth code
            String authCode = transData.getAuthCode();
            if (authCode != null && !authCode.isEmpty()) {
                emvBase.setTlv(0x89, authCode.getBytes());
            }
            emvBase.setTlv(0x8A, "00".getBytes());
            // write transaction record
            transData.setOnlineTrans(true);
            //region OLS
            RedeemTrans.Companion.unpackReservedField(transData);
            //endregion
            GreendaoHelper.getTransDataHelper().update(transData);
            return EOnlineResult.APPROVE;

        } catch (EmvException | TlvException | NegativeArraySizeException e) {
            LogUtils.e(TAG, "", e);
            reversalRequired = true;
        } finally {
            if (transProcessListener != null) {
                transProcessListener.onHideProgress();
            }
        }

        if(reversalRequired) {
            return EOnlineResult.ABORT;
        }

        return EOnlineResult.FAILED;
    }

    /**
     * Enter pin.
     *
     * @param isOnlinePin         the is online pin
     * @param offlinePinLeftTimes the offline pin left times
     * @param pinData             the pin data
     */
    protected void enterPin(boolean isOnlinePin, int offlinePinLeftTimes,byte[] pinData) {
        ETransType transType = ConvertUtils.enumValue(ETransType.class,transData.getTransType());
        if (transType == null){
            cv.open();
            return;
        }
        final String header;
        final String subHeader = context.getString(R.string.prompt_no_pin);
        final String totalAmount = transType.isSymbolNegative() ? "-" + transData.getAmount() : transData.getAmount();
        final String tipAmount = transType.isSymbolNegative() ? null : transData.getTipAmount();
        final int pinKeyIndex = KeyUtils.getTpkIndex(transData.getAcquirer().getTleKeySetId());

        transData.setPinFree(false);
        if (isOnlinePin) { // online PIN
            header = context.getString(R.string.prompt_pin);
            doOnlineAction(header, subHeader, totalAmount, tipAmount, pinKeyIndex);

        } else {
            header = context.getString(R.string.prompt_pin) + "(" + offlinePinLeftTimes + ")";
            doOfflineAction(header, subHeader, totalAmount, tipAmount, pinData, pinKeyIndex);
        }
    }

    private void doOnlineAction(final String header, final String subHeader,
                                final String totalAmount, final String tipAmount, final int pinKeyIndex) {
        ActionEnterPin actionEnterPin = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                byte[] track2 = emvBase.getTlv(TagsTable.TRACK2);
                String strTrack2 = TrackUtils.getTrack2FromTag57(track2);
                String pan = TrackUtils.getPan(strTrack2);

                ((ActionEnterPin) action).setParam(context, eTransType
                                .getTransName(), pan, true, header, subHeader,
                        totalAmount, tipAmount, ActionEnterPin.EEnterPinType.ONLINE_PIN, pinKeyIndex);
            }
        });

        actionEnterPin.setEndListener(new OnlineEndAction());
        actionEnterPin.execute();
    }

    private class OnlineEndAction implements AAction.ActionEndListener {
        @Override
        public void onEnd(AAction action, ActionResult result) {
            int ret = result.getRet();
            if (ret == TransResult.SUCC) {
                String data = (String) result.getData();
                transData.setPin(data);
                if (data != null && !data.isEmpty()) {
                    transData.setHasPin(true);
                    intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
                } else {
                    intResult = EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement(); // bypass
                }
            } else {
                intResult = EEmvExceptions.EMV_ERR_RSP.getErrCodeFromBasement();
            }
            ActivityStack.getInstance().popTo((Activity) context);
            if (cv != null) {
                cv.open();
            }
        }
    }

    private void doOfflineAction(final String header, final String subHeader,
                                 final String totalAmount, final String tipAmount,
                                 final byte[] pinData, final int pinKeyIndex) {
        ActionEnterPin actionEnterPin = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                byte[] track2 = emvBase.getTlv(TagsTable.TRACK2);
                String strTrack2 = TrackUtils.getTrack2FromTag57(track2);
                String pan = TrackUtils.getPan(strTrack2);
                ActionEnterPin.EEnterPinType enterPinType = ActionEnterPin.EEnterPinType.OFFLINE_PCI_MODE;
                if (!ParamHelper.isInternalPed()){
                    enterPinType = ActionEnterPin.EEnterPinType.ONLINE_PIN;
                }
                ((ActionEnterPin) action).setParam(context, eTransType
                                .getTransName(), pan, true, header, subHeader,
                        totalAmount, tipAmount, enterPinType, pinKeyIndex);
            }
        });

        actionEnterPin.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                String data = (String)result.getData();
                if (!ParamHelper.isInternalPed() && pinData != null && data != null){
                    byte[] pinBlock = ConvertHelper.getConvert().strToBcdPaddingLeft(data);
                    //plaintext PIN
                    byte[] plaintextPIN = PedHelper.calcDes(pinKeyIndex, pinBlock);
                    //convert pDataIn to BCD format(64个byte)
                    byte[] track2 = emvBase.getTlv(TagsTable.TRACK2);
                    String strTrack2 = TrackUtils.getTrack2FromTag57(track2);
                    String pan = TrackUtils.getPan(strTrack2);
                    String panBlock = PanUtils.getPanBlock(pan, PanUtils.X9_8_WITH_PAN);
                    byte[] bcdDataIn = ConvertHelper.getConvert().strToBcdPaddingLeft(panBlock);
                    //xor with pDataIn
                    byte[] xorData = new byte[8];
                    System.arraycopy(plaintextPIN,0,xorData,0,plaintextPIN.length);
                    for (int i = 0;i<8;i++){
                        xorData[i] ^= bcdDataIn[i];
                    }
                    //get PIN
                    byte[] ascPin = ConvertHelper.getConvert().bcdToStr(xorData).getBytes();
                    int pinLen = xorData[0];
                    //set to pinData,ended with '\x00'"
                    System.arraycopy(ascPin,2,pinData,0,pinLen);
                    pinData[pinLen] = 0x00;
                }
                if (cv != null){
                    cv.open();
                }
                ActivityStack.getInstance().popTo((Activity) context);
            }
        });
        actionEnterPin.execute();
    }

    protected void dccGetCardholderDecision() {
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (transType == null){
            cv.open();
            return;
        }

        String tmp = transData.getDccForeignAmount();
        if(tmp == null || tmp.length() <= 0) {
            LogUtils.e(TAG, "DCC foreign amount empty!");
            cv.open();
            return;
        }
        tmp = transData.getDccCurrencyCode();
        if(tmp == null || tmp.length() <= 0) {
            LogUtils.e(TAG, "DCC currency code empty!");
            cv.open();
            return;
        }
        final Locale foreignLocale = CurrencyConverter.getLocaleFromCountryCode(transData.getDccCurrencyCode());
        final String foreignAmount = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getDccForeignAmount(), 0), foreignLocale);
        final String domesticAmount = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
        LogUtils.i(TAG, "DCC foreign amount: " + foreignAmount + " domestic amount: " + domesticAmount);

        ActionDcc actionDcc = new ActionDcc(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDcc) action).setParam(context, transType.getTransName(),
                        domesticAmount, foreignAmount);
            }
        });

        PrinterUtils.printDccRate((Activity) context, transData);

        actionDcc.setEndListener(new DccGetCardholderDecisionEndAction());
        actionDcc.execute();
    }

    private class DccGetCardholderDecisionEndAction implements AAction.ActionEndListener {
        @Override
        public void onEnd(AAction action, ActionResult result) {
            int ret = result.getRet();
            if (ret == TransResult.SUCC) {
                boolean data = (boolean) result.getData();
                if(data) {
                    FinancialApplication.getAcqManager().switchToAcquirer(AppConstants.DCC_ACQUIRER, transData);
                }
                intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
            } else {
                intResult = EEmvExceptions.EMV_ERR_RSP.getErrCodeFromBasement();
            }
            if (cv != null) {
                cv.open();
            }
            ActivityStack.getInstance().popTo((Activity) context);
        }
    }

    protected EOnlineResult dccOnlineProc() throws PedDevException {
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        if (transProcessListener != null) {
            transProcessListener.onUpdateProgressTitle(transType.getTransName());
        }

        int ret = new Online().online(transData, transProcessListener);
        final String respCode = transData.getResponseCode();
        LogUtils.i(TAG, "DCC Online  ret = " + ret + ", response code: " + respCode);
        if (ret == TransResult.SUCC && "00".equals(respCode)) {
            return EOnlineResult.APPROVE;
        }

        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }

        return EOnlineResult.ABORT;
    }

    protected void getRrn() {
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (transType == null){
            cv.open();
            return;
        }

        String transName;
        switch(transType) {
            case REFUND:
                transName = ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND);
                break;
            case PREAUTH_COMPLETE:
                transName = ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_PRE_AUTH_COMPLETE);
                break;
            case PREAUTH_CANCEL:
                transName = ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_PRE_AUTH_CANCEL);
                break;
            default:
                transName = transType.getTransName();
                break;
        }

        // Enter RRN for UPI
        ActionInputTransData enterRrnAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        context,
                        transName
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("inputRrnLabel"),
                        ActionInputTransData.EInputType.NUM,
                        12,
                        1,
                        false
                )
        );

        enterRrnAction.setEndListener(new EnterRrnEndAction());
        enterRrnAction.execute();
    }

    private class EnterRrnEndAction implements AAction.ActionEndListener {
        @Override
        public void onEnd(AAction action, ActionResult result) {
            int ret = result.getRet();
            if (ret == TransResult.SUCC) {
                transData.setRefNo((String)result.getData());
                intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
            } else {
                intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
            }
            if (cv != null) {
                cv.open();
            }
            ActivityStack.getInstance().popTo((Activity) context);
        }
    }

    protected void getOrigDate() {
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (transType == null){
            cv.open();
            return;
        }

        // Enter date for UPI
        ActionInputTransData enterDateAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        context,
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND)
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("inputOrigDateLabel"),
                        ActionInputTransData.EInputType.NUM,
                        8,
                        1,
                        false
                )
        );

        enterDateAction.setEndListener(new EnterOrigDateEndAction());
        enterDateAction.execute();
    }

    private class EnterOrigDateEndAction implements AAction.ActionEndListener {
        @Override
        public void onEnd(AAction action, ActionResult result) {
            int ret = result.getRet();
            if (ret == TransResult.SUCC) {
                String data = (String) result.getData();
                if(data == null || data.length() != 8 || !DateUtils.isDateValid(data, DateUtils.UPI_REFUND_DATE_FORMAT)) {
                    intResult = EEmvExceptions.ERR_ENTERED_DATE.getErrCodeFromBasement();
                } else {
                    transData.setDateTime(String.format("%s%s%s", data.substring(4), data.substring(2, 4), data.substring(0, 2)));
                    intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
                }
            } else {
                intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
            }
            if (cv != null) {
                cv.open();
            }
            ActivityStack.getInstance().popTo((Activity) context);
        }
    }
}
