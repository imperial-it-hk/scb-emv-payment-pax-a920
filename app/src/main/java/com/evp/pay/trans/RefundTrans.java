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
import com.evp.bizlib.data.entity.SettledTransData;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.smallamt.SmallAmtUtils;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.DateUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.emv.EmvTransProcess;
import com.evp.pay.trans.action.ActionDispTransDetail;
import com.evp.pay.trans.action.ActionEmvProcess;
import com.evp.pay.trans.action.ActionEnterAmount;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionQrRefund;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.pay.trans.action.ActionShowRefundSelection;
import com.evp.pay.trans.action.ActionSignature;
import com.evp.pay.trans.action.ActionTransOnline;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * The type Refund trans.
 */
public class RefundTrans extends BaseTrans {

    private String initAmount;
    private TransData origTransData;
    private SettledTransData settledTransData;
    private String selectedRefundType;

    /**
     * Instantiates a new Refund trans.
     *
     * @param context       the context
     * @param transListener the trans listener
     */
    public RefundTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.REFUND, transListener);
        initAmount = null;
    }

    /**
     * Instantiates a new Refund trans.
     *
     * @param context       the context
     * @param amount        the amount
     * @param transListener the trans listener
     */
    public RefundTrans(Context context, String amount, TransEndListener transListener) {
        super(context, ETransType.REFUND, transListener);
        initAmount = amount;
    }

    @Override
    protected void bindStateOnAction() {

        transData.setAmount(initAmount); // cannot set in the Constructor cuz the Component.transInit() is called in execute

        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        getCurrentContext(),
                        6,
                        String.format("%s %s", ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND), ConfigUtils.getInstance().getString(ConfigConst.LABEL_PASSWORD)),
                        null
                )
        );
        bind(State.INPUT_PWD.toString(), inputPasswordAction, true);

        // enter amount action
        ActionEnterAmount amountAction = new ActionEnterAmount(action -> ((ActionEnterAmount) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND),
                        false
                )
        );
        bind(State.ENTER_AMOUNT.toString(), amountAction, true);

        // Refund Selection
        ActionShowRefundSelection actionShowRefundSelection = new ActionShowRefundSelection(action -> ((ActionShowRefundSelection) action)
                .setParam(
                        getCurrentContext()
                )
        );
        bind(State.REFUND_SELECTION.toString(), actionShowRefundSelection, true);

        // Enter Trace No
        ActionInputTransData enterTransNoAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND)
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("inputTraceLabel"),
                        ActionInputTransData.EInputType.NUM,
                        6,
                        false
                )
        );
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction, true);

        // confirm information
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(action -> {
            LinkedHashMap<String, String> transInfo = new LinkedHashMap<>();
            ETransType eTransType = ConvertUtils.enumValue(ETransType.class, settledTransData.getTransType());
            String transType = eTransType != null ? eTransType.getTransName() : "";
            String amount = CurrencyConverter.convert(Utils.parseLongSafe(settledTransData.getAmount(), 0), transData.getCurrency());
            if (settledTransData != null) {
                transData.setTrack2(settledTransData.getTrack2());
                transData.setTrack3(settledTransData.getTrack3());

                // date and time
                String formattedDate = ConvertUtils.convert(settledTransData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                        Constants.TIME_PATTERN_DISPLAY);
                transInfo.put(ConfigUtils.getInstance().getString("transTypeLabel"), transType);
                if (settledTransData.getFundingSource() != null) {
                    transInfo.put(ConfigUtils.getInstance().getString("walletLabel"), ConfigUtils.getInstance().getWalletName(settledTransData.getFundingSource()));
                } else {
                    //AET-95
                    if (settledTransData.getIssuer() != null) {
                        transInfo.put(getString(R.string.history_detail_card_no), PanUtils.maskCardNo(settledTransData.getPan(), settledTransData.getIssuer().getPanMaskPattern()));
                    }
                    transInfo.put(getString(R.string.history_detail_auth_code), settledTransData.getAuthCode());
                    transInfo.put(getString(R.string.history_detail_ref_no), settledTransData.getRefNo());
                }
                transInfo.put(ConfigUtils.getInstance().getString("amountLabel"), amount);
                transInfo.put(ConfigUtils.getInstance().getString("traceNoLabel"), Component.getPaddedNumber(settledTransData.getTraceNo(), 6));
                transInfo.put(ConfigUtils.getInstance().getString("dateTimeLabel"), formattedDate);
            } else {
                transData.setEnterMode(origTransData.getEnterMode());
                transData.setTrack2(origTransData.getTrack2());
                transData.setTrack3(origTransData.getTrack3());

                // date and time
                String formattedDate = ConvertUtils.convert(origTransData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                        Constants.TIME_PATTERN_DISPLAY);
                transInfo.put(ConfigUtils.getInstance().getString("transTypeLabel"), transType);
                if (transData.getEnterMode().equals(TransData.EnterMode.QR)) {
                    transInfo.put(ConfigUtils.getInstance().getString("walletLabel"), ConfigUtils.getInstance().getWalletName(transData.getFundingSource()));
                } else {
                    //AET-95
                    if (origTransData.getIssuer() != null) {
                        transInfo.put(getString(R.string.history_detail_card_no), PanUtils.maskCardNo(origTransData.getPan(), origTransData.getIssuer().getPanMaskPattern()));
                    }
                    transInfo.put(getString(R.string.history_detail_auth_code), origTransData.getAuthCode());
                    transInfo.put(getString(R.string.history_detail_ref_no), origTransData.getRefNo());
                }
                transInfo.put(ConfigUtils.getInstance().getString("amountLabel"), amount);
                transInfo.put(ConfigUtils.getInstance().getString("traceNoLabel"), Component.getPaddedNumber(settledTransData.getTraceNo(), 6));
                transInfo.put(ConfigUtils.getInstance().getString("dateTimeLabel"), formattedDate);
            }
            ((ActionDispTransDetail) action)
                    .setParam(
                            getCurrentContext(),
                            ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND),
                            transInfo,
                            transData.getFundingSource()
                    );
        });
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction, true);

        // QR Refund
        ActionQrRefund actionQrRefund = new ActionQrRefund(action -> ((ActionQrRefund) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.QR_ONLINE.toString(), actionQrRefund, true);

        // search card
        ActionSearchCard searchCardAction = new ActionSearchCard(action -> ((ActionSearchCard) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND),
                        Component.getCardReadMode(ETransType.REFUND), transData.getAmount(),
                        null,
                        ""
                )
        );
        bind(State.CHECK_CARD.toString(), searchCardAction, true);

        // input password action
        ActionEnterPin enterPinAction = new ActionEnterPin(action -> ((ActionEnterPin) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND),
                        transData.getPan(),
                        true,
                        getString(R.string.prompt_pin),
                        getString(R.string.prompt_no_pin),
                        "-" + transData.getAmount(),
                        null,
                        ActionEnterPin.EEnterPinType.ONLINE_PIN,
                        KeyUtils.getTpkIndex(transData.getAcquirer().getTleKeySetId())
                )
        );
        bind(State.ENTER_PIN.toString(), enterPinAction, true);

        // emv deal action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(action -> ((ActionEmvProcess) action)
                .setParam(
                        getCurrentContext(),
                        emv,
                        transData
                )
        );
        bind(State.EMV_PROC.toString(), emvProcessAction, true);

        // online action
        ActionTransOnline transOnlineAction = new ActionTransOnline(action -> ((ActionTransOnline) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.MAG_ONLINE.toString(), transOnlineAction, true);

        // signature action
        ActionSignature signatureAction = new ActionSignature(action -> ((ActionSignature) action)
                .setParam(
                        getCurrentContext(),
                        transData.getAmount()
                )
        );
        bind(State.SIGNATURE.toString(), signatureAction);

        //print preview action
        PrintTask printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(RefundTrans.this, State.PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.PRINT.toString(), printTask);

        // Enter RRN for UPI
        ActionInputTransData enterRrnAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_REFUND)
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("inputRrnLabel"),
                        ActionInputTransData.EInputType.NUM,
                        12,
                        1,
                        false
                )
        );
        bind(State.ENTER_RRN.toString(), enterRrnAction, true);

        // Enter date for UPI
        ActionInputTransData enterDateAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
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
        bind(State.ENTER_DATE.toString(), enterDateAction, true);

        // whether need input management password for void and refund
        if (SysParam.getInstance().getBoolean(R.string.OTHTC_VERIFY)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            gotoState(State.REFUND_SELECTION.toString());
        }
    }

    private enum State {
        /**
         * Input pwd state.
         */
        INPUT_PWD,
        /**
         * Select refund state.
         */
        REFUND_SELECTION,
        /**
         * Input transaction no. state.
         */
        ENTER_TRANSNO,
        /**
         * Transaction detail state.
         */
        TRANS_DETAIL,
        /**
         * QR online state.
         */
        QR_ONLINE,
        /**
         * Enter amount state.
         */
        ENTER_AMOUNT,
        /**
         * Check card state.
         */
        CHECK_CARD,
        /**
         * Enter pin state.
         */
        ENTER_PIN,
        /**
         * Mag online state.
         */
        MAG_ONLINE,
        /**
         * Emv proc state.
         */
        EMV_PROC,
        /**
         * Signature state.
         */
        SIGNATURE,
        /**
         * Print state.
         */
        PRINT,
        /**
         * Enter RRN state.
         */
        ENTER_RRN,
        /**
         * Enter DATE state.
         */
        ENTER_DATE,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        switch (state) {
            case INPUT_PWD:
                onInputPwd(result);
                break;
            case ENTER_AMOUNT:
                onEnterAmount(result);
                break;
            case REFUND_SELECTION:
                afterRefundSelection(result);
                break;
            case TRANS_DETAIL:
                if (result.getRet() != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
                if (selectedRefundType != AppConstants.SALE_TYPE_CARD) {
                    gotoState(State.QR_ONLINE.toString());
                } else {
                    gotoState(State.MAG_ONLINE.toString());
                }
                break;
            case ENTER_TRANSNO:
                onEnterTraceNo(result);
                break;
            case QR_ONLINE:
                afterQrRefund(result);
                break;
            case CHECK_CARD: // check card
                onCheckCard(result);
                break;
            case ENTER_PIN: // enter pin
                onEnterPin(result);
                break;
            case EMV_PROC: // emv
                CTransResult transResult = (CTransResult) result.getData();
                onEmvProc(transResult.getTransResult());
                break;
            case MAG_ONLINE: // after online
                toSignOrPrint();
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case PRINT:
                if (result.getRet() == TransResult.SUCC || Utils.needBtPrint()) {
                    // end trans
                    transEnd(result);
                } else {
                    transEnd(new ActionResult(TransResult.SUCC, null));
                }
                break;
            case ENTER_RRN:
                onInputRrn(result);
                break;
            case ENTER_DATE:
                onInputDate(result);
                break;
            default:
                transEnd(result);
                break;
        }
    }

    private void onInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.REFUND_PASSWORD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        gotoState(State.REFUND_SELECTION.toString());
    }

    private void onEnterAmount(ActionResult result) {
        transData.setAmount(result.getData().toString());
        gotoState(State.CHECK_CARD.toString());
    }

    private void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData);
        transData.setTransType(transType.name());

        byte searchMode = cardInfo.getSearchMode();
        if (searchMode == SearchMode.INSERT) {
            needRemoveCard = true;
            // EMV process
            gotoState(SaleTrans.State.EMV_PROC.toString());
        } else if (searchMode == SearchMode.SWIPE || searchMode == SearchMode.KEYIN) {
            //Check if refund is available for this PAN
            if(transData.getIssuer() != null && !transData.getIssuer().getIsEnableRefund()) {
                transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                return;
            }

            //Special flow for UPI
            if(AppConstants.UPI_ACQUIRER.equals(transData.getAcquirer().getName())) {
                gotoState(State.ENTER_RRN.toString());
            }
            else if (transData.getIssuer().isNonEmvTranRequirePIN()) {
                // enter pin
                gotoState(State.ENTER_PIN.toString());
            } else {
                // online
                transData.setHasPin(false);
                gotoState(State.MAG_ONLINE.toString());
            }
        } else {
            gotoState(State.ENTER_PIN.toString());
        }
    }

    private void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && !pinBlock.isEmpty()) {
            transData.setHasPin(true);
        }
        // online
        gotoState(State.MAG_ONLINE.toString());
    }

    private void onEmvProc(ETransResult transResult) {
        EmvTransProcess.emvTransResultProcess(transResult, emv, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {
            GreendaoHelper.getTransDataHelper().update(transData);
            toSignOrPrint();
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {
            EmvTransProcess.saveCardInfoAndCardSeq(emv, transData);
            transData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            GreendaoHelper.getTransDataHelper().update(transData);
            // increase trans no.
            Component.incTransNo();
            toSignOrPrint();
        } else if (transResult == ETransResult.ONLINE_DENIED) { // online denied
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
        } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// platform approve card denied
            transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
        } else if (transResult == ETransResult.ABORT_TERMINATED || transResult == ETransResult.OFFLINE_DENIED) { // emv terminated
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void onSignature(ActionResult result) {
        // save signature
        byte[] signData = (byte[]) result.getData();
        byte[] signPath = (byte[]) result.getData1();

        if (signData != null && signData.length > 0 &&
                signPath != null && signPath.length > 0) {
            transData.setSignData(signData);
            transData.setSignPath(signPath);
            // update trans data，save signature
            GreendaoHelper.getTransDataHelper().update(transData);
        }

        // if terminal not support electronic signature, user do not make signature or signature time out, print preview
        gotoState(State.PRINT.toString());
    }

    private void afterRefundSelection(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        selectedRefundType = result.getData().toString();
        if (selectedRefundType == AppConstants.SALE_TYPE_CARD) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            gotoState(State.ENTER_TRANSNO.toString());
        }
    }

    private void onEnterTraceNo(ActionResult result) {
        if (Component.isDemo()) {
            long transNo = (long) 1;
            validateOrigTransData(transNo);
        } else {
            String content = (String) result.getData();
            long transNo;
            transNo = Utils.parseLongSafe(content, -1);
            settledTransData = GreendaoHelper.getSettledTransDataDbHelper().findSettledTransDataByTraceNo(transNo);
            if (settledTransData == null) {
                // trans not exist
                transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
                return;
            }
            if (settledTransData.getFundingSource() == null) {
                transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                return;
            }
            if (settledTransData.getTransState() == SettledTransData.ETransStatus.REFUNDED) {
                transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
                return;
            }
            validateOrigTransData(transNo);
        }
    }

    private void validateOrigTransData(long origTransNo) {
        if (selectedRefundType == AppConstants.SALE_TYPE_CARD) {
            origTransData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(origTransNo);
            if (origTransData == null) {
                // trans not exist
                transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
                return;
            }
            ETransType trType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());

            // only sale and refund trans can be revoked
            // AET-101 AET-139
            boolean isOfflineSent = trType == ETransType.OFFLINE_SALE &&
                    origTransData.getOfflineSendState() != null &&
                    origTransData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_SENT;
            boolean isAdjustedNotSent = origTransData.getTransState() == TransData.ETransStatus.ADJUSTED &&
                    origTransData.getOfflineSendState() != null &&
                    origTransData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_NOT_SENT;
            if ((!Objects.requireNonNull(trType).isVoidAllowed() && !isOfflineSent) || isAdjustedNotSent) {
                transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORTED, null));
                return;
            }

            TransData.ETransStatus trStatus = origTransData.getTransState();
            // void trans can not be revoked again
            if (trStatus.equals(TransData.ETransStatus.VOIDED)) {
                transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
                return;
            }
        }

        copyOrigTransData();
        gotoState(State.TRANS_DETAIL.toString());
    }

    // set original trans data
    private void copyOrigTransData() {
        if (settledTransData != null) {
            if(AppConstants.QR_ACQUIRER.equals(settledTransData.getAcquirer().getName())) {
                transData.setTransType(ETransType.REFUND.name());
                transData.setTransType(settledTransData.getTransType());
                transData.setTraceNo(settledTransData.getTraceNo());
                transData.setStanNo(settledTransData.getStanNo());
                transData.setAmount(settledTransData.getAmount());
                transData.setAmountCNY(settledTransData.getAmountCNY());
                transData.setExchangeRate(settledTransData.getExchangeRate());
                transData.setCurrencyCode(settledTransData.getCurrencyCode());
                transData.setOrigBatchNo(settledTransData.getBatchNo());
                transData.setBatchNo(settledTransData.getBatchNo());
                transData.setOrigAuthCode(settledTransData.getAuthCode());
                transData.setAuthCode(settledTransData.getAuthCode());
                transData.setOrigRefNo(settledTransData.getRefNo());
                transData.setRefNo(settledTransData.getRefNo());
                transData.setOrigTransNo(settledTransData.getTraceNo());
                transData.setPan(settledTransData.getPan());
                transData.setExpDate(settledTransData.getExpDate());
                transData.setAcquirer(settledTransData.getAcquirer());
                transData.setPaymentId(settledTransData.getPaymentId());
                transData.setFundingSource(settledTransData.getFundingSource());
                transData.setOrigTransType(settledTransData.getTransType());
                transData.setOrigDateTime(settledTransData.getDateTime());
                transData.setSendingBankCode(settledTransData.getSendingBankCode());
                transData.setMerchantPan(settledTransData.getMerchantPan());
                transData.setConsumerPan(settledTransData.getConsumerPan());
                transData.setPaymentChannel(settledTransData.getPaymentChannel());
                transData.setQrCodeId(settledTransData.getQrCodeId());
                transData.setTransactionId(settledTransData.getTransactionId());
                transData.setBillPaymentRef1(settledTransData.getBillPaymentRef1());
                transData.setBillPaymentRef2(settledTransData.getBillPaymentRef2());
                transData.setBillPaymentRef3(settledTransData.getBillPaymentRef3());
                transData.setPayeeProxyId(settledTransData.getPayeeProxyId());
                transData.setPayeeProxyType(settledTransData.getPayeeProxyType());
                transData.setPayeeAccountNumber(settledTransData.getPayeeAccountNumber());
                transData.setPayerProxyId(settledTransData.getPayerProxyId());
                transData.setPayerProxyType(settledTransData.getPayerProxyType());
                transData.setPayerAccountNumber(settledTransData.getPayerAccountNumber());
                transData.setReceivingBankCode(settledTransData.getReceivingBankCode());
                transData.setThaiQRTag(settledTransData.getThaiQRTag());
                transData.setIsPullSlip(settledTransData.getIsPullSlip());
                transData.setQrcsTraceNo(settledTransData.getQrcsTraceNo());
                transData.setIsBSC(settledTransData.getIsBSC());
                transData.setSaleType(settledTransData.getSaleType());
            } else {
                transData.setTransType(ETransType.REFUND.name());
                transData.setAmount(settledTransData.getAmount());
                transData.setOrigBatchNo(settledTransData.getBatchNo());
                transData.setOrigAuthCode(settledTransData.getAuthCode());
                transData.setOrigRefNo(settledTransData.getRefNo());
                transData.setOrigTransNo(settledTransData.getTraceNo());
                transData.setPan(settledTransData.getPan());
                transData.setExpDate(settledTransData.getExpDate());
                transData.setAcquirer(settledTransData.getAcquirer());
                transData.setIssuer(settledTransData.getIssuer());
                transData.setOrigTransType(settledTransData.getTransType());
                transData.setOrigDateTime(settledTransData.getDateTime());
                transData.setEmvAppName(settledTransData.getEmvAppName());
                transData.setAid(settledTransData.getAid());
                transData.setEmvAppLabel(settledTransData.getEmvAppLabel());
                transData.setTrack2(settledTransData.getTrack2());
                transData.setTrack3(settledTransData.getTrack3());
                transData.setSaleType(settledTransData.getSaleType());
            }
        } else {
            if(AppConstants.QR_ACQUIRER.equals(origTransData.getAcquirer().getName())) {
                transData.setEnterMode(origTransData.getEnterMode());
                transData.setTraceNo(origTransData.getTraceNo());
                transData.setTransType(origTransData.getTransType());
                transData.setAmount(origTransData.getAmount());
                transData.setAmountCNY(origTransData.getAmountCNY());
                transData.setExchangeRate(origTransData.getExchangeRate());
                transData.setCurrencyCode(origTransData.getCurrencyCode());
                transData.setOrigBatchNo(origTransData.getBatchNo());
                transData.setBatchNo(origTransData.getBatchNo());
                transData.setOrigAuthCode(origTransData.getAuthCode());
                transData.setAuthCode(origTransData.getAuthCode());
                transData.setOrigRefNo(origTransData.getRefNo());
                transData.setRefNo(origTransData.getRefNo());
                transData.setOrigTransNo(origTransData.getTraceNo());
                transData.setPan(origTransData.getPan());
                transData.setExpDate(origTransData.getExpDate());
                transData.setAcquirer(origTransData.getAcquirer());
                transData.setPaymentId(origTransData.getPaymentId());
                transData.setTransState(origTransData.getTransState());
                transData.setFundingSource(origTransData.getFundingSource());
                transData.setOrigTransType(origTransData.getTransType());
                transData.setOrigDateTime(origTransData.getDateTime());
                transData.setSendingBankCode(origTransData.getSendingBankCode());
                transData.setMerchantPan(origTransData.getMerchantPan());
                transData.setConsumerPan(origTransData.getConsumerPan());
                transData.setPaymentChannel(origTransData.getPaymentChannel());
                transData.setQrCodeId(origTransData.getQrCodeId());
                transData.setTransactionId(origTransData.getTransactionId());
                transData.setBillPaymentRef1(origTransData.getBillPaymentRef1());
                transData.setBillPaymentRef2(origTransData.getBillPaymentRef2());
                transData.setBillPaymentRef3(origTransData.getBillPaymentRef3());
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
                transData.setEmvAppName(origTransData.getEmvAppName());
                transData.setAid(origTransData.getAid());
                transData.setEmvAppLabel(origTransData.getEmvAppLabel());
                transData.setTrack2(origTransData.getTrack2());
                transData.setTrack3(origTransData.getTrack3());
                transData.setSaleType(origTransData.getSaleType());
            }
        }

        FinancialApplication.getAcqManager().setCurAcq(transData.getAcquirer());
    }

    private void afterQrRefund(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        settledTransData.setTransState(SettledTransData.ETransStatus.REFUNDED);
        GreendaoHelper.getSettledTransDataDbHelper().update(settledTransData);
        transData.setTransState(TransData.ETransStatus.NORMAL);
        transData.setTransType(ETransType.REFUND.name());
        GreendaoHelper.getTransDataHelper().insert(transData);

        gotoState(State.PRINT.toString());
    }

    private void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {
            gotoState(State.PRINT.toString());
        } else if(SmallAmtUtils.isTrxSmallAmt(transData)) {
            gotoState(State.PRINT.toString());
        } else if(transData.getHasPin()) {
            gotoState(State.PRINT.toString());
        } else {
            gotoState(State.SIGNATURE.toString());
        }
    }

    private void onInputRrn(ActionResult result) {
        transData.setRefNo((String)result.getData());
        gotoState(State.ENTER_DATE.toString());
    }

    private void onInputDate(ActionResult result) {
        String data = (String) result.getData();
        if(data == null || data.length() != 8 || !DateUtils.isDateValid(data, DateUtils.UPI_REFUND_DATE_FORMAT)) {
            transEnd(new ActionResult(TransResult.ERR_ENTERED_DATE, null));
            return;
        }
        transData.setDateTime(String.format("%s%s%s", data.substring(4), data.substring(2, 4), data.substring(0, 2)));
        if (transData.getIssuer().isNonEmvTranRequirePIN()) {
            // enter pin
            gotoState(State.ENTER_PIN.toString());
        } else {
            // online
            transData.setHasPin(false);
            gotoState(State.MAG_ONLINE.toString());
        }
    }
}
