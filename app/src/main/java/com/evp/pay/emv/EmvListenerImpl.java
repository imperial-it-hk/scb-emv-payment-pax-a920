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
package com.evp.pay.emv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.ConditionVariable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.dcc.DccUtils;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.tpn.TpnUtils;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.EmvImpl;
import com.evp.eemv.IEmv;
import com.evp.eemv.IEmvListener;
import com.evp.eemv.entity.Amounts;
import com.evp.eemv.entity.CandList;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eventbus.SearchCardEvent;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.pax.dal.exceptions.PedDevException;
import com.pax.jemv.clcommon.RetCode;

import java.util.List;

/**
 * The type Emv listener.
 */
public class EmvListenerImpl extends EmvBaseListenerImpl implements IEmvListener {

    private static final String TAG = EmvListenerImpl.class.getSimpleName();
    private IEmv emv;

    /**
     * Instantiates a new Emv listener.
     *
     * @param context   the context
     * @param emv       the emv
     * @param transData the trans data
     * @param listener  the listener
     */
    public EmvListenerImpl(Context context, IEmv emv, TransData transData, TransProcessListener listener) {
        super(context, emv, transData, listener);
        this.emv = emv;
    }

    @Override
    public final int onCardHolderPwd(final boolean isOnlinePin, final int offlinePinLeftTimes, byte[] pinData) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        intResult = 0;

        if (pinData != null && pinData[0] != 0) {
            if (pinData[0] == 1) {
                LogUtils.e(TAG, "enter pin timeout");
                return RetCode.EMV_TIME_OUT;
            } else {
                return pinData[0];
            }
        }
        enterPin(isOnlinePin, offlinePinLeftTimes,pinData);

        cv.block(); // for the Offline pin case, block it for make sure the PIN activity is ready, otherwise, may get the black screen.
        return intResult;
    }

    @Override
    public final boolean onChkExceptionFile() {
        byte[] track2 = emv.getTlv(TagsTable.TRACK2);
        String strTrack2 = TrackUtils.getTrack2FromTag57(track2);
        // 卡号
        String pan = TrackUtils.getPan(strTrack2);
        boolean ret = GreendaoHelper.getCardBinBlackHelper().isBlack(pan);
        if (ret) {
            transProcessListener.onShowErrMessage(context.getString(R.string.emv_card_in_black_list), Constants.FAILED_DIALOG_SHOW_TIME, false);
            return true;
        }
        return false;
    }

    @Override
    public final int onConfirmCardNo(final String pan) {
        boolean failure = false;

        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }

        //TPN and TSC logic
        byte[] aid = emv.getTlv(TagsTable.AID);
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
            ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
            if(transType == ETransType.INSTALLMENT) {
                acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.IPP_ACQUIRER);
            } else if(transType == ETransType.OLS_ENQUIRY || transType == ETransType.REDEEM) {
                acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.OLS_ACQUIRER);
            } else {
                acq = FinancialApplication.getAcqManager().findAcquirer(AppConstants.SCB_ACQUIRER);
            }

            Issuer issuer = FinancialApplication.getAcqManager().findIssuer(AppConstants.TSC_ISSUER);
            if(!TpnUtils.setTscAcqAndIssuer(acq, issuer, transData)) {
                failure = true;
            } else {
                FinancialApplication.getAcqManager().setCurAcq(acq);
            }
        }
        //Standard EMV logic
        else {
            Issuer issuer = FinancialApplication.getAcqManager().findIssuerAndSetAcquirerByPan(pan, transData);
            if (issuer == null) {
                failure = true;
            }
        }

        if(failure) {
            return EEmvExceptions.EMV_ERR_DATA.getErrCodeFromBasement();
        }

        cv = new ConditionVariable();

        byte[] holderNameBCD = emv.getTlv(0x5F20);
        if (holderNameBCD == null) {
            holderNameBCD = " ".getBytes();
        }
        byte[] expDateBCD = emv.getTlv(0x5F24);
        String expDate = ConvertHelper.getConvert().bcdToStr(expDateBCD);

        //TIP / Adjust allowed logic
        float adjustPercent = 0;
        if(eTransType.isAdjustAllowed() && transData.getIssuer().isEnableAdjust()) {
            adjustPercent = transData.getIssuer().getAdjustPercent();
        }
        FinancialApplication.getApp().doEvent(new SearchCardEvent(SearchCardEvent.Status.ICC_UPDATE_CARD_INFO, new CardInfo(pan, new String(holderNameBCD), expDate, adjustPercent)));

        //Check PAN validity and exp date
        if(!Issuer.validPan(transData.getIssuer(), pan) || !Issuer.validCardExpiry(transData.getIssuer(), expDate)) {
            return EEmvExceptions.EMV_ERR_DATA.getErrCodeFromBasement();
        }

        FinancialApplication.getApp().doEvent(new SearchCardEvent(SearchCardEvent.Status.ICC_CONFIRM_CARD_NUM));

        cv.block();

        //Check if refund is available for this PAN
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if(transType == ETransType.REFUND && transData.getIssuer() != null && !transData.getIssuer().getIsEnableRefund()) {
            return EEmvExceptions.ERR_TRANS_NOT_SUPPORTED.getErrCodeFromBasement();
        }

        return intResult;
    }

    @Override
    public final Amounts onGetAmounts() {
        Amounts amt = new Amounts();
        amt.setTransAmount(transData.getAmount());
        return amt;
    }

    @Override
    protected void updateTransDataFromKernel() throws PedDevException {
        super.updateTransDataFromKernel();
        EmvTransProcess.saveCardInfoAndCardSeq(emv, transData);
    }

    @Override
    public EOnlineResult onOnlineProc() throws PedDevException {
        return onlineProc();
    }

    @Override
    public final int onWaitAppSelect(final boolean isFirstSelect, final List<CandList> candList) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        // ignore Sonar's lambda suggestion cuz the Sonar runs JAVA8, but EDC runs JAVA7,
        // there are same cases, ignore them as well.
        FinancialApplication.getApp().runOnUiThread(new SelectAppRunnable(isFirstSelect, candList));

        cv.block();
        return intResult;
    }

    @Override
    public final int onDcc() throws PedDevException {
        Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(AppConstants.DCC_ACQUIRER);
        if(!DccUtils.isTrxDcc(acquirer, emv.getTlv(TagsTable.APP_CURRENCY_CODE), transData.getTransType())) {
            return 0;
        }

        final byte[] track2 = emv.getTlv(TagsTable.TRACK2);
        final String strTrack2 = TrackUtils.getTrack2FromTag57(track2);

        TransData backupTransData = transData;

        transData = new TransData(transData);
        transData.setTransType(ETransType.DCC_GET_RATE.name());
        transData.setProcCode(ETransType.DCC_GET_RATE.getProcCode());
        transData.setAcquirer(acquirer);
        transData.setNii(acquirer.getNii());
        transData.setTrack2(strTrack2);

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

    @Override
    public int onAdditionalProcess() {
        boolean itIsUpiAcquirer = AppConstants.UPI_ACQUIRER.equals(transData.getAcquirer().getName());
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        if(transType == ETransType.REFUND && itIsUpiAcquirer) {
            cv = new ConditionVariable();
            intResult = RetCode.EMV_OK;
            getRrn();
            cv.block();

            if(intResult != RetCode.EMV_OK) {
                return intResult;
            }

            cv = new ConditionVariable();
            intResult = RetCode.EMV_OK;
            getOrigDate();
            cv.block();

            return intResult;
        } else if(transType == ETransType.PREAUTH_CANCEL || transType == ETransType.PREAUTH_COMPLETE) {
            cv = new ConditionVariable();
            intResult = RetCode.EMV_OK;
            getRrn();
            cv.block();

            return intResult;
        }

        return RetCode.EMV_OK;
    }

    /**
     * set timeout action
     */
    public void onTimeOut() {
        intResult = EEmvExceptions.EMV_ERR_TIMEOUT.getErrCodeFromBasement();
        EmvImpl.isTimeOut = true;
        cv.open();
    }

    private class SelectAppRunnable implements Runnable {
        private final boolean isFirstSelect;
        private final List<CandList> candList;

        /**
         * Instantiates a new Select app runnable.
         *
         * @param isFirstSelect the is first select
         * @param candList      the cand list
         */
        SelectAppRunnable(final boolean isFirstSelect, final List<CandList> candList) {
            this.isFirstSelect = isFirstSelect;
            this.candList = candList;
        }

        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (isFirstSelect) {
                builder.setTitle(context.getString(R.string.emv_application_choose));
            } else {
                SpannableString sstr = new SpannableString(context.getString(R.string.emv_application_choose_again));
                sstr.setSpan(new ForegroundColorSpan(Color.RED), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setTitle(sstr);
            }
            String[] appNames = new String[candList.size()];
            for (int i = 0; i < appNames.length; i++) {
                appNames[i] = candList.get(i).getAppName();
            }
            builder.setSingleChoiceItems(appNames, -1, (dialog, which) -> {
                intResult = which;
                close(dialog);
            });

            builder.setPositiveButton(context.getString(R.string.dialog_cancel),
                    (dialog, which) -> {
                        intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                        close(dialog);
                    });
            builder.setCancelable(false);
            builder.create().show();
        }

        private void close(DialogInterface dialog) {
            dialog.dismiss();
            cv.open();
        }
    }


    /**
     * Offline pin enter ready.
     */
    public void offlinePinEnterReady() {
        cv.open();
    }

    /**
     * Card num config err.
     */
    public void cardNumConfigErr() {
        intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
        cv.open();
    }

    /**
     * Card num config succ.
     */
    public void cardNumConfigSucc() {
        intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
        cv.open();
    }

    /**
     * Card num config succ.
     *
     * @param amount the amount
     */
    public void cardNumConfigSucc(String[] amount) {
        if (amount != null && amount.length == 2) {
            transData.setAmount(String.valueOf(CurrencyConverter.parse(amount[0])));
            transData.setTipAmount(String.valueOf(CurrencyConverter.parse(amount[1])));
        }
        cardNumConfigSucc();
    }

    @Override
    public void setCvmResult(byte[] cvmResult) {
        if(cvmResult == null || cvmResult.length <= 0) {
            transData.setSignFree(false);
            return;
        }

        //Signature
        if(cvmResult[0] == 0x1E) {
            transData.setSignFree(false);
        }
        //Enciphered PIN and signature
        else if (cvmResult[0] == 0x05) {
            transData.setSignFree(false);
        }
        //Plaintext PIN and signature
        else if (cvmResult[0] == 0x03) {
            transData.setSignFree(false);
        }
        //No signature required
        else {
            transData.setSignFree(true);
        }
    }

    @Override
    public boolean isPinAllowedForTransaction() {
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if(transType == null) {
            return true;
        }
        return transType.isPinAllowed();
    }

    /**
     * The type Card info.
     */
    public static class CardInfo {
        private String cardNum;
        private String holderName;
        private String expDate;
        private float adjustPercent;

        /**
         * Instantiates a new Card info.
         *
         * @param cardNum       the card num
         * @param holderName    the holder name
         * @param expDate       the exp date
         * @param adjustPercent the adjust percent
         */
        CardInfo(String cardNum, String holderName, String expDate, float adjustPercent) {
            this.cardNum = cardNum;
            this.holderName = holderName;
            this.expDate = expDate;
            this.adjustPercent = adjustPercent;
        }

        /**
         * Gets card num.
         *
         * @return the card num
         */
        public String getCardNum() {
            return cardNum;
        }

        /**
         * Gets holder name.
         *
         * @return the holder name
         */
        public String getHolderName() {
            return holderName;
        }

        /**
         * Gets exp date.
         *
         * @return the exp date
         */
        public String getExpDate() {
            return expDate;
        }

        /**
         * Gets adjust percent.
         *
         * @return the adjust percent
         */
        public float getAdjustPercent() {
            return adjustPercent;
        }
    }
}
