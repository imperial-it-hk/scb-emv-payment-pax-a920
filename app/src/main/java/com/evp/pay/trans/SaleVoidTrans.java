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
package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.ETransStatus;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.action.ActionDispTransDetail;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionScanQRCode;
import com.evp.pay.trans.action.ActionSignature;
import com.evp.pay.trans.action.ActionTransOnline;
import com.evp.pay.trans.action.ActionVoid;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * The type Sale void trans.
 */
public class SaleVoidTrans extends BaseTrans {

    private TransData origTransData;
    private String origTransNo;

    /**
     * whether need to read the original trans data or not
     */
    private boolean isNeedFindOrigTrans = true;
    /**
     * whether need to input trans no. or not
     */
    private boolean isNeedInputTransNo = true;

    private boolean isThisOfflineVoid;

    /**
     * Instantiates a new Sale void trans.
     *
     * @param context       the context
     * @param transListener the trans listener
     */
    public SaleVoidTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.VOID, transListener);
        origTransData = new TransData();
        Component.transInit(origTransData);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    /**
     * Instantiates a new Sale void trans.
     *
     * @param context       the context
     * @param origTransData the orig trans data
     * @param transListener the trans listener
     */
    public SaleVoidTrans(Context context, TransData origTransData, TransEndListener transListener) {
        super(context, ETransType.VOID, transListener);
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    /**
     * Instantiates a new Sale void trans.
     *
     * @param context       the context
     * @param origTransNo   the orig trans no
     * @param transListener the trans listener
     */
    public SaleVoidTrans(Context context, String origTransNo, TransEndListener transListener) {
        super(context, ETransType.VOID, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @Override
    protected void bindStateOnAction() {

        Acquirer acquirer = transData.getAcquirer();

        //Enter PWD action
        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        getCurrentContext(),
                        6,
                        String.format("%s %s", ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_VOID), ConfigUtils.getInstance().getString(ConfigConst.LABEL_PASSWORD)),
                        null
                )
        );
        bind(State.INPUT_PWD.toString(), inputPasswordAction, true);

        //Enter trace NO. action
        ActionInputTransData enterTransNoAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_VOID)
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("inputTraceLabel"),
                        ActionInputTransData.EInputType.NUM,
                        6,
                        0,
                        true,
                        true
                )
        );
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction, true);

        //Scan QR code action
        ActionScanQRCode actionScanQRCode = new ActionScanQRCode(action -> ((ActionScanQRCode) action)
                .setParam(
                        getCurrentContext()
                )
        );
        bind(State.SCAN_CODE.toString(), actionScanQRCode, false);

        // confirm information
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(action -> {
            ETransType eTransType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());
            String transType = eTransType != null ? eTransType.getTransName() : "";
            String amount = CurrencyConverter.convert(Utils.parseLongSafe(origTransData.getAmount(), 0), transData.getCurrency());

            if (eTransType == ETransType.REDEEM) {
                transType = RedeemTrans.Companion.getPlanName(origTransData.getPaymentPlan());
                amount = origTransData.getFormattedNetSaleAmt();
            }

            transData.setEnterMode(origTransData.getEnterMode());
            transData.setTrack2(origTransData.getTrack2());
            transData.setTrack3(origTransData.getTrack3());

            LinkedHashMap<String, String> transInfo = new LinkedHashMap<>();
            transInfo.put(ConfigUtils.getInstance().getString("transTypeLabel"), transType);

            if (transData.getFundingSource() != null) {
                transInfo.put(ConfigUtils.getInstance().getString("walletLabel"), ConfigUtils.getInstance().getWalletName(transData.getFundingSource()));
            } else {
                if (origTransData.getIssuer() != null) {
                    transInfo.put(getString(R.string.history_detail_card_no), PanUtils.maskCardNo(origTransData.getPan(), origTransData.getIssuer().getPanMaskPattern()));
                }
                transInfo.put(getString(R.string.history_detail_auth_code), origTransData.getAuthCode());
                transInfo.put(getString(R.string.history_detail_ref_no), origTransData.getRefNo());
            }
            transInfo.put(ConfigUtils.getInstance().getString("amountLabel"), amount);

            if (eTransType == ETransType.REDEEM) {
                transInfo.put(getString(R.string.history_detail_point), origTransData.getFormattedRedeemPts());
            }

            transInfo.put(ConfigUtils.getInstance().getString("traceNoLabel"), Component.getPaddedNumber(origTransData.getTraceNo(), 6));
            transInfo.put(ConfigUtils.getInstance().getString("dateTimeLabel"), ConvertUtils.convert(origTransData.getDateTime(), Constants.TIME_PATTERN_TRANS, Constants.TIME_PATTERN_DISPLAY));

            ((ActionDispTransDetail) action)
                    .setParam(
                            getCurrentContext(),
                            ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_VOID),
                            transInfo,
                            transData.getFundingSource()
                    );
        });
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction, true);

        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(action -> ((ActionTransOnline) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.MAG_ONLINE.toString(), transOnlineAction, true);

        // signature action
        ActionSignature signatureAction = new ActionSignature(action -> {
            ETransType transType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());
            if(transType == ETransType.REDEEM) {
                ((ActionSignature) action)
                        .setParam(
                                getCurrentContext(),
                                transData.getFormattedNetSaleAmt(),
                                transData.getFormattedRedeemPts()
                        );
            } else {
                ((ActionSignature) action)
                        .setParam(
                                getCurrentContext(),
                                transData.getAmount()
                        );
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // Void Online for QR
        ActionVoid actionVoid = new ActionVoid(action -> ((ActionVoid) action)
                .setParam(
                        getCurrentContext(),
                        transData,
                        origTransData
                )
        );
        bind(State.QR_ONLINE.toString(), actionVoid, true);

        //print preview action
        PrintTask printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(SaleVoidTrans.this, State.PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.PRINT.toString(), printTask);

        //Determine first action
        if (SysParam.getInstance().getBoolean(R.string.OTHTC_VERIFY)) {
            gotoState(State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {
            gotoState(State.ENTER_TRANSNO.toString());
        } else if (isNeedFindOrigTrans) {
            validateOrigTransData(Utils.parseLongSafe(origTransNo, -1));
        } else {
            copyOrigTransData();
            if(acquirer.getName().equals(AppConstants.QR_ACQUIRER)) {
                gotoState(State.TRANS_DETAIL.toString());
            } else {
                checkPin();
            }
        }
    }

    /**
     * The enum State.
     */
    enum State {
        /**
         * Input pwd state.
         */
        INPUT_PWD,
        /**
         * Enter transno state.
         */
        ENTER_TRANSNO,
        /**
         * Scan QR code state.
         */
        SCAN_CODE,
        /**
         * Trans detail state.
         */
        TRANS_DETAIL,
        /**
         * Mag online state.
         */
        MAG_ONLINE,
        /**
         * Signature state.
         */
        SIGNATURE,
        /**
         * Print state.
         */
        PRINT,
        /**
         * Online for QR state.
         */
        QR_ONLINE,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);

        switch (state) {
            case INPUT_PWD:
                onInputPwd(result);
                break;
            case ENTER_TRANSNO:
                onEnterTraceNo(result);
                break;
            case TRANS_DETAIL:
                if (result.getRet() != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
                if (transData.getFundingSource() != null) {
                    gotoState(State.QR_ONLINE.toString());
                } else {
                    checkOfflineTrans();
                }
                break;
            case MAG_ONLINE: //  subsequent processing of online
                if (ETransType.REDEEM.name().equals(transData.getOrigTransType())
                        && transData.getField63() != null
                        && transData.getField63().length > 0)
                {
                    RedeemTrans.Companion.unpackReservedField(transData);
                }
                saveTransData();
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case SCAN_CODE:
                afterScanCode(result);
                break;
            case QR_ONLINE:
                afterQrOnline(result);
                break;
            case PRINT:
                if (result.getRet() == TransResult.SUCC || Utils.needBtPrint()) {
                    // end trans
                    transEnd(result);
                } else {
                    transEnd(new ActionResult(TransResult.SUCC, null));
                }
                break;
            default:
                transEnd(result);
                break;
        }

    }

    private void onInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.VOID_PASSWORD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        if (isNeedInputTransNo) {
            gotoState(State.ENTER_TRANSNO.toString());
        } else if (isNeedFindOrigTrans) {
            validateOrigTransData(Utils.parseLongSafe(origTransNo, -1));
        } else {
            copyOrigTransData();
            if(transData.getAcquirer().getName().equals(AppConstants.QR_ACQUIRER)) {
                gotoState(State.TRANS_DETAIL.toString());
            } else {
                checkPin();
            }
        }
    }

    private void onEnterTraceNo(ActionResult result) {
        if (result.getData() != null && result.getData().toString() == AppConstants.SALE_TYPE_SCAN) {
            gotoState(State.SCAN_CODE.toString());
            return;
        }

        String content = (String) result.getData();
        long transNo;
        if (content == null) {
            TransData transData = GreendaoHelper.getTransDataHelper().findLastTransData();
            if (transData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                return;
            }
            transNo = transData.getTraceNo();
        } else {
            transNo = Utils.parseLongSafe(content, -1);
        }
        validateOrigTransData(transNo);
    }

    private void onSignature(ActionResult result) {
        // save signature data
        byte[] signData = (byte[]) result.getData();
        byte[] signPath = (byte[]) result.getData1();

        if (signData != null && signData.length > 0
                && signPath != null && signPath.length > 0)
        {
            transData.setSignData(signData);
            transData.setSignPath(signPath);
            // update trans data，save signature
            GreendaoHelper.getTransDataHelper().update(transData);
        }

        // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
        gotoState(State.PRINT.toString());
    }

    // check original trans data
    private void validateOrigTransData(long origTransNo) {
        origTransData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(origTransNo);
        if (origTransData == null) {
            // trans not exist
            LogUtils.e(TAG, "ERROR - Transaction not found!");
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        ETransType trType = ConvertUtils.enumValue(ETransType.class,origTransData.getTransType());
        // void trans can not be revoked again
        if (trType == ETransType.VOID) {
            LogUtils.e(TAG, "Transaction already voided!");
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }

        boolean isAdjustedNotSent = origTransData.getTransState() == ETransStatus.ADJUSTED &&
                origTransData.getOfflineSendState() != null &&
                origTransData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_NOT_SENT;
        if (!Objects.requireNonNull(trType).isVoidAllowed() || isAdjustedNotSent) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORTED, null));
            return;
        }

        copyOrigTransData();
        gotoState(State.TRANS_DETAIL.toString());
    }

    // set original trans data
    private void copyOrigTransData() {
        if (origTransData == null) {
            LogUtils.e(TAG, "ERROR - origTransData is NULL!!!");
            return;
        }

        ETransType trType = ConvertUtils.enumValue(ETransType.class,origTransData.getTransType());
        isThisOfflineVoid = trType == ETransType.OFFLINE_SALE &&
                origTransData.getOfflineSendState() != null &&
                origTransData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_NOT_SENT;

        FinancialApplication.getAcqManager().setCurAcq(origTransData.getAcquirer());

        if(origTransData.getAcquirer().getName().equals(AppConstants.QR_ACQUIRER)) {
            transData.setAmount(origTransData.getAmount());
            transData.setOrigBatchNo(origTransData.getBatchNo());
            transData.setOrigAuthCode(origTransData.getAuthCode());
            transData.setOrigRefNo(origTransData.getRefNo());
            transData.setOrigTransNo(origTransData.getTraceNo());
            transData.setPan(origTransData.getPan());
            transData.setExpDate(origTransData.getExpDate());
            transData.setAcquirer(origTransData.getAcquirer());
            transData.setOrigTransType(origTransData.getTransType());
            transData.setOrigDateTime(origTransData.getDateTime());
            transData.setEnterMode(origTransData.getEnterMode());
            transData.setEmvAppName(origTransData.getEmvAppName());
            transData.setAid(origTransData.getAid());
            transData.setEmvAppLabel(origTransData.getEmvAppLabel());
            transData.setTrack2(origTransData.getTrack2());
            transData.setTrack3(origTransData.getTrack3());
            transData.setTransType(origTransData.getTransType());
            transData.setStanNo(origTransData.getStanNo());
            transData.setAmountCNY(origTransData.getAmountCNY());
            transData.setExchangeRate(origTransData.getExchangeRate());
            transData.setCurrencyCode(origTransData.getCurrencyCode());
            transData.setBatchNo(origTransData.getBatchNo());
            transData.setAuthCode(origTransData.getAuthCode());
            transData.setRefNo(origTransData.getRefNo());
            transData.setPaymentId(origTransData.getPaymentId());
            transData.setTransState(origTransData.getTransState());
            transData.setFundingSource(origTransData.getFundingSource());
            transData.setDateTime(origTransData.getDateTime());
            transData.setSendingBankCode(origTransData.getSendingBankCode());
            transData.setMerchantPan(origTransData.getMerchantPan());
            transData.setConsumerPan(origTransData.getConsumerPan());
            transData.setPaymentChannel(origTransData.getPaymentChannel());
            transData.setQrCodeId(origTransData.getQrCodeId());
            transData.setTransactionId(origTransData.getTransactionId());
            transData.setBillPaymentRef1(origTransData.getBillPaymentRef1());
            transData.setBillPaymentRef2(origTransData.getBillPaymentRef2());
            transData.setBillPaymentRef3(origTransData.getBillPaymentRef3());
            transData.setNii(origTransData.getNii());
            transData.setPaymentPlan(origTransData.getPaymentPlan());
            transData.setPaymentTerm(origTransData.getPaymentTerm());
            transData.setProductSN(origTransData.getProductSN());
            transData.setProductCode(origTransData.getProductCode());
            transData.setRedeemQty(origTransData.getRedeemQty());
            transData.setOrigField63(origTransData.getOrigField63());
            transData.setField63(origTransData.getField63());
            transData.setTc(origTransData.getTc());
            transData.setArqc(origTransData.getArqc());
            transData.setTvr(origTransData.getTvr());
            transData.setTsi(origTransData.getTsi());
            transData.setPayeeProxyId(origTransData.getPayeeProxyId());
            transData.setPayeeProxyType(origTransData.getPayeeProxyType());
            transData.setPayeeAccountNumber(origTransData.getPayeeAccountNumber());
            transData.setPayerProxyId(origTransData.getPayerProxyId());
            transData.setPayerProxyType(origTransData.getPayerProxyType());
            transData.setPayerAccountNumber(origTransData.getPayerAccountNumber());
            transData.setReceivingBankCode(origTransData.getReceivingBankCode());
            transData.setThaiQRTag(origTransData.getThaiQRTag());
            transData.setIsPullSlip(origTransData.getIsPullSlip());
            transData.setQrcsTraceNo(origTransData.getQrcsTraceNo());
            transData.setIsBSC(origTransData.getIsBSC());
            transData.setSaleType(origTransData.getSaleType());
        } else {
            transData.setAmount(origTransData.getAmount());
            transData.setOrigBatchNo(origTransData.getBatchNo());
            transData.setOrigAuthCode(origTransData.getAuthCode());
            transData.setOrigRefNo(origTransData.getRefNo());
            transData.setOrigTransNo(origTransData.getTraceNo());
            transData.setPan(origTransData.getPan());
            transData.setExpDate(origTransData.getExpDate());
            transData.setAcquirer(origTransData.getAcquirer());
            transData.setIssuer(origTransData.getIssuer());
            transData.setOrigTransType(origTransData.getTransType());
            transData.setOrigDateTime(origTransData.getDateTime());
            transData.setEnterMode(origTransData.getEnterMode());
            transData.setEmvAppName(origTransData.getEmvAppName());
            transData.setAid(origTransData.getAid());
            transData.setEmvAppLabel(origTransData.getEmvAppLabel());
            transData.setTrack2(origTransData.getTrack2());
            transData.setTrack3(origTransData.getTrack3());
            transData.setNii(origTransData.getNii());
            transData.setPaymentPlan(origTransData.getPaymentPlan());
            transData.setPaymentTerm(origTransData.getPaymentTerm());
            transData.setProductSN(origTransData.getProductSN());
            transData.setProductCode(origTransData.getProductCode());
            transData.setRedeemQty(origTransData.getRedeemQty());
            transData.setOrigField63(origTransData.getOrigField63());
            transData.setSaleType(origTransData.getSaleType());
            transData.setHasPin(origTransData.getHasPin());
            transData.setPin(origTransData.getPin());
        }
    }

    // check whether void trans need to enter pin or not
    private void checkPin() {
        // not need to enter pin
        transData.setPin("");
        transData.setHasPin(false);
        checkOfflineTrans();
    }

    private void afterQrOnline(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }
        origTransData.setTransState(ETransStatus.VOIDED);
        if (!Component.isDemo()) {
            GreendaoHelper.getTransDataHelper().update(origTransData);
            transData.setTransType(ETransType.VOID.name());
        }

        gotoState(State.PRINT.toString());
    }

    private void afterScanCode(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }

        long transNo;
        if (Component.isDemo()) {
            transNo = (long) 1;
            validateOrigTransData(transNo);
        } else {
            if (result.getData() != null) {
                transNo = Utils.parseLongSafe(result.getData().toString(), -1);
                if (transNo == -1 || result.getData().toString().length() > 6) {
                    transEnd(new ActionResult(TransResult.ERR_INVALID_QR, null));
                    return;
                }
                validateOrigTransData(transNo);
            } else {
                transEnd(new ActionResult(TransResult.ERR_INVALID_QR, null));
                return;
            }
        }
    }

    private void checkOfflineTrans() {
        if(isThisOfflineVoid) {
            LogUtils.i(TAG, "This is OFFLINE VOID!");
            //Get rid of transaction as it's offline
            //This is actual requirement for offline sale void. In nutshell just delete offline sale and print receipt - done.
            transData.setId(origTransData.getId());
            transData.setTraceNo(origTransData.getTraceNo());
            //No signature or PIN for VOID
            transData.setSignFree(true);
            transData.setPinFree(true);
            GreendaoHelper.getTransDataHelper().delete(transData);
            gotoState(State.PRINT.toString());
        } else {
            LogUtils.i(TAG, "This is NORMAL VOID!");
            gotoState(SaleTrans.State.MAG_ONLINE.toString());
        }
    }

    private void saveTransData() {
        //No signature or PIN for VOID
        transData.setSignFree(true);
        transData.setPinFree(true);
        //This is trick how to maintain same trace no. for sale and it's void
        GreendaoHelper.getTransDataHelper().delete(transData);
        transData.setId(origTransData.getId());
        transData.setTraceNo(origTransData.getTraceNo());
        GreendaoHelper.getTransDataHelper().update(transData);
        gotoState(State.PRINT.toString());
    }
}
