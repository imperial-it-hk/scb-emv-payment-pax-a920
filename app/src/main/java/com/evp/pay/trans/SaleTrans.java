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
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.EnterMode;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.smallamt.SmallAmtUtils;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.emv.EmvTags;
import com.evp.pay.emv.EmvTransProcess;
import com.evp.pay.emv.clss.ClssTransProcess;
import com.evp.pay.trans.action.ActionAdjustTip;
import com.evp.pay.trans.action.ActionClssPreProc;
import com.evp.pay.trans.action.ActionClssProcess;
import com.evp.pay.trans.action.ActionEmvProcess;
import com.evp.pay.trans.action.ActionEnterAmount;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.pay.trans.action.ActionQRCancelCSB;
import com.evp.pay.trans.action.ActionQRInquiry;
import com.evp.pay.trans.action.ActionQRPaymentBSCMenu;
import com.evp.pay.trans.action.ActionQRPaymentCSBMenu;
import com.evp.pay.trans.action.ActionQRSaleBSC;
import com.evp.pay.trans.action.ActionQRSaleCSB;
import com.evp.pay.trans.action.ActionScanQRCode;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.pay.trans.action.ActionShowConfirmAmount;
import com.evp.pay.trans.action.ActionShowQRCodePage;
import com.evp.pay.trans.action.ActionSignature;
import com.evp.pay.trans.action.ActionTransOnline;
import com.evp.pay.trans.action.ActionUserAgreement;
import com.evp.pay.trans.action.activity.UserAgreementActivity;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.settings.SysParam;
import com.evp.view.dialog.CustomAlertDialog;

import java.util.ArrayList;
import java.util.Currency;

/**
 * The type Sale trans.
 */
public class SaleTrans extends BaseTrans {
    protected byte searchCardMode = -1; // search card mode
    protected String amount;
    protected String tipAmount;
    protected float percent;
    protected String saleType;
    protected String qrCode;
    protected String fundingSource;
    protected boolean isFreePin;
    protected boolean isSupportBypass = true;
    protected boolean hasTip = false;
    protected boolean needFallBack = false;
    protected String title = ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_SALE);
    private String fundingSourceImagePath;
    protected byte currentMode;

    public SaleTrans(Context context, String saleType, String amount, String fundingSource, TransEndListener transListener) {
        super(context, ETransType.SALE, transListener);
        setParam(saleType, amount, "0", (byte) 0, false, false, fundingSource);
    }

    /**
     * Instantiates a new Sale trans.
     *
     * @param context       :context
     * @param amount        :total amount
     * @param mode          {@link SearchMode}, 如果等于-1，
     * @param isFreePin     :true free; false not
     * @param transListener the trans listener
     */
    public SaleTrans(Context context, String saleType, String amount, byte mode, boolean isFreePin,
                     TransEndListener transListener) {
        super(context, ETransType.SALE, transListener);
        setParam(saleType, amount, "0", mode, isFreePin, false, null);
    }

    /**
     * Instantiates a new Sale trans.
     *
     * @param context       :context
     * @param amount        :total amount
     * @param tipAmount     :tip amount
     * @param mode          {@link SearchMode}, 如果等于-1，
     * @param isFreePin     :true free; false not
     * @param transListener the trans listener
     */
    public SaleTrans(Context context, String saleType, String amount, String tipAmount, byte mode, boolean isFreePin,
                     TransEndListener transListener) {
        super(context, ETransType.SALE, transListener);
        setParam(saleType, amount, tipAmount, mode, isFreePin, true, null);
    }

    /**
     * Instantiates a generic Sale trans.
     */
    public SaleTrans(Context context, ETransType transType, boolean isFreePin, TransEndListener transListener) {
        super(context, transType, transListener);
        //tipAmount = "0" && hasTip = true => no prompt for tip
        setParam(AppConstants.SALE_TYPE_CARD, "0", "0", (byte) -1, isFreePin, true, null);
    }

    private void setParam(String saleType, String amount, String tipAmount, byte mode, boolean isFreePin, boolean hasTip, String fundingSource) {
        this.saleType = saleType;
        this.searchCardMode = mode;
        this.amount = amount;
        this.tipAmount = tipAmount;
        this.isFreePin = isFreePin;
        this.hasTip = hasTip;
        this.fundingSource = fundingSource;

        if (searchCardMode == -1) { // 待机银行卡消费入口
            searchCardMode = Component.getCardReadMode(super.transType);
        }
    }

    @Override
    public void bindStateOnAction() {
        if (amount != null && !amount.isEmpty()) {
            transData.setAmount(amount.replace(".", ""));
        }
        if (tipAmount != null && !tipAmount.isEmpty()) {
            transData.setTipAmount(tipAmount.replace(".", ""));
        }
        if (fundingSource != null) {
            transData.setFundingSource(fundingSource);
        }

        // enter trans amount action(This action is mainly used to handle bank card consumption and flash close paid deals)
        ActionEnterAmount amountAction = new ActionEnterAmount(action -> ((ActionEnterAmount) action)
                .setParam(
                        getCurrentContext(),
                        title,
                        false
                )
        );
        bind(State.ENTER_AMOUNT.toString(), amountAction, true);

        //Inquiry
        ActionQRInquiry qrInquiryAction = new ActionQRInquiry(action -> ((ActionQRInquiry) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.INQUIRY.toString(), qrInquiryAction, true);

        // search card action
        ActionSearchCard searchCardAction = new ActionSearchCard(action -> ((ActionSearchCard) action)
                .setParam(
                        getCurrentContext(),
                        title,
                        searchCardMode,
                        transData.getAmount(),
                        null,
                        ""
                )
        );
        bind(State.CHECK_CARD.toString(), searchCardAction, true);

        //adjust tip action
        ActionAdjustTip adjustTipAction = new ActionAdjustTip(action -> {
            amount = String.valueOf(Utils.parseLongSafe(transData.getAmount(), 0) -
                    Utils.parseLongSafe(transData.getTipAmount(), 0));

            ((ActionAdjustTip) action)
                    .setParam(
                            getCurrentContext(),
                            title,
                            amount,
                            percent
                    );
        });
        bind(State.ADJUST_TIP.toString(), adjustTipAction, true);

        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(action -> {
            // if flash pay by pwd,set isSupportBypass=false,need to enter pin
            if (!isFreePin) {
                isSupportBypass = false;
            }
            ((ActionEnterPin) action)
                    .setParam(
                            getCurrentContext(),
                            title,
                            transData.getPan(),
                            isSupportBypass,
                            getString(R.string.prompt_pin),
                            getString(R.string.prompt_no_pin),
                            transData.getAmount(),
                            transData.getTipAmount(),
                            ActionEnterPin.EEnterPinType.ONLINE_PIN,
                            KeyUtils.getTpkIndex(transData.getAcquirer().getTleKeySetId())
                    );
        });
        bind(State.ENTER_PIN.toString(), enterPinAction, true);

        // emv process action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(action -> ((ActionEmvProcess) action)
                .setParam(
                        getCurrentContext(),
                        emv,
                        transData
                )
        );
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(action -> ((ActionClssProcess) action)
                .setParam(
                        getCurrentContext(),
                        clss,
                        transData
                )
        );
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(action -> ((ActionClssPreProc) action)
                .setParam(
                        clss,
                        transData
                )
        );
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

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

        // Agreement action
        ActionUserAgreement userAgreementAction = new ActionUserAgreement(action -> ((ActionUserAgreement) action)
                .setParam(
                        getCurrentContext()
                )
        );
        bind(State.USER_AGREEMENT.toString(), userAgreementAction, true);

        //print preview action
        PrintTask printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(SaleTrans.this, State.PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.PRINT.toString(), printTask);

        // Show QR Payment CSB Menu
        ActionQRPaymentCSBMenu actionQRPaymentCSBMenu = new ActionQRPaymentCSBMenu(action -> ((ActionQRPaymentCSBMenu) action)
                .setParam(
                        getCurrentContext()
                )
        );
        bind(State.SELECT_QR_PAYMENT_CSB.toString(), actionQRPaymentCSBMenu, true);

        // Get QRCode
        ActionQRSaleCSB qrSaleCSBAction = new ActionQRSaleCSB(action -> ((ActionQRSaleCSB) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.PERFORM_SALE_CSB.toString(), qrSaleCSBAction, true);

        // Cancel QRCode
        ActionQRCancelCSB qrCancelCSBAction = new ActionQRCancelCSB(action -> ((ActionQRCancelCSB) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.PERFORM_CANCEL_CSB.toString(), qrCancelCSBAction, true);

        // Show QR Code Page
        ActionShowQRCodePage showQRCodePage = new ActionShowQRCodePage(action -> ((ActionShowQRCodePage) action)
                .setParam(
                        getCurrentContext(),
                        qrCode,
                        amount,
                        Currency.getInstance(transData.getCurrency()).getCurrencyCode(),
                        transData.getFundingSource(),
                        fundingSourceImagePath
                )
        );
        bind(State.SHOW_QR_CODE_PAGE.toString(), showQRCodePage, false);        // will handle cancel

        // Show QR Payment CSB Menu
        ActionQRPaymentBSCMenu actionQRPaymentBSCMenu = new ActionQRPaymentBSCMenu(action -> ((ActionQRPaymentBSCMenu) action)
                .setParam(
                        getCurrentContext()
                )
        );
        bind(State.SELECT_QR_PAYMENT_BSC.toString(), actionQRPaymentBSCMenu, true);

        // Scan QRCode
        ActionScanQRCode scanQrCode = new ActionScanQRCode(action -> ((ActionScanQRCode) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.SCAN_QR_CODE.toString(), scanQrCode, true);

        // Confirm Amount
        ActionShowConfirmAmount showConfirmAmountAction = new ActionShowConfirmAmount(action -> ((ActionShowConfirmAmount) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_SALE),
                        amount,
                        Currency.getInstance(transData.getCurrency()).getCurrencyCode(),
                        transData.getFundingSource()
                )
        );
        bind(State.SHOW_CONFIRM_AMOUNT.toString(), showConfirmAmountAction, true);

        // Perform BSC
        ActionQRSaleBSC qrSaleBSCAction = new ActionQRSaleBSC(action -> ((ActionQRSaleBSC) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.PERFORM_SALE_BSC.toString(), qrSaleBSCAction, true);

        if (transType == ETransType.SALE) {
            transData.setSaleType(saleType);
            switch (saleType) {
                case AppConstants.SALE_TYPE_QR:
                    if (fundingSource != null) {
                        afterQRPaymentCSBPlatform(new ActionResult(0, fundingSource));
                    } else {
                        gotoState(State.SELECT_QR_PAYMENT_CSB.toString());
                    }
                    break;
                case AppConstants.SALE_TYPE_SCAN:
                    if (fundingSource != null) {
                        afterQRPaymentBSCPlatform(new ActionResult(0, fundingSource));
                    } else {
                        gotoState(State.SELECT_QR_PAYMENT_BSC.toString());
                    }
                    break;
                case AppConstants.SALE_TYPE_CARD:
                    if(amount != null && !amount.isEmpty()) {
                        gotoState(State.CLSS_PREPROC.toString());
                    } else {
                        gotoState(State.ENTER_AMOUNT.toString());
                    }
                    break;
            }
        }
    }

    /**
     * The enum State.
     */
    enum State {
        /** CARD */
        /**
         * Enter amount state.
         */
        ENTER_AMOUNT,
        /**
         * Check card state.
         */
        CHECK_CARD,
        /**
         * Adjust tip state.
         */
        ADJUST_TIP,
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
         * Clss preproc state.
         */
        CLSS_PREPROC,
        /**
         * Clss proc state.
         */
        CLSS_PROC,
        /**
         * Signature state.
         */
        SIGNATURE,
        /**
         * User agreement state.
         */
        USER_AGREEMENT,
        INQUIRY,
        /**
         * Print state.
         */
        PRINT,

        /**
         * QR
         */
        SELECT_QR_PAYMENT_CSB,
        PERFORM_SALE_CSB,
        PERFORM_CANCEL_CSB,
        SHOW_QR_CODE_PAGE,

        /**
         * SCAN
         */
        SELECT_QR_PAYMENT_BSC,
        SCAN_QR_CODE,
        SHOW_CONFIRM_AMOUNT,
        PERFORM_SALE_BSC,
        TRANS_CONFIRM,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        int ret = result.getRet();
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(emv, transType, true);
            if (f55Dup.length > 0) {
                TransData dupTransData = GreendaoHelper.getTransDataHelper().findFirstDupRecord(transData.getAcquirer());
                if (dupTransData != null) {
                    dupTransData.setDupIccData(ConvertHelper.getConvert().bcdToStr(f55Dup));
                    GreendaoHelper.getTransDataHelper().update(dupTransData);
                }
            }
            if (ret == TransResult.NEED_FALL_BACK) {
                needFallBack = true;
                searchCardMode &= 0x01;
                gotoState(State.CHECK_CARD.toString());
                return;
            } else if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        if (state == State.CLSS_PREPROC && ret != TransResult.SUCC) {
            searchCardMode &= 0x03;
        }

        switch (state) {
            /** CARD */
            case ENTER_AMOUNT:
//                // save trans amount
                transData.setAmount(result.getData().toString());
                gotoState(State.CLSS_PREPROC.toString());
                break;
            case CHECK_CARD: // subsequent processing of check card
                onCheckCard(result);
                break;
            case INQUIRY:
                afterInquiry(result);
                break;
            case ADJUST_TIP:
                onAdjustTip(result);
                break;
            case ENTER_PIN: // subsequent processing of enter pin
                onEnterPin(result);
                break;
            case MAG_ONLINE: // subsequent processing of online
                // determine whether need electronic signature or print
                toSignOrPrint();
                break;
            case EMV_PROC: // emv后续处理
                //get trans result
                CTransResult transResult = (CTransResult) result.getData();
                // EMV完整流程 脱机批准或联机批准都进入签名流程
                afterEMVProcess(transResult.getTransResult());
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                CTransResult clssResult = (CTransResult) result.getData();
                afterClssProcess(clssResult);
                break;
            case SIGNATURE:
                // save signature data
                byte[] signData = (byte[]) result.getData();
                byte[] signPath = (byte[]) result.getData1();

                if (signData != null && signData.length > 0 &&
                        signPath != null && signPath.length > 0) {
                    transData.setSignData(signData);
                    transData.setSignPath(signPath);
                    // update trans data，save signature
                    GreendaoHelper.getTransDataHelper().update(transData);
                }
                gotoState(State.PRINT.toString());
                break;
            case USER_AGREEMENT:
                String agreement = (String) result.getData();
                if (agreement != null && agreement.equals(UserAgreementActivity.ENTER_BUTTON)) {
                    gotoState(State.CLSS_PREPROC.toString());
                } else {
                    transEnd(result);
                }
                break;
            case PRINT:
                if (result.getRet() == TransResult.SUCC || Utils.needBtPrint()) {
                    // end trans
                    transEnd(result);
                } else {
                    transEnd(new ActionResult(TransResult.SUCC, null));
                }
                break;

            /** QR */
            case SELECT_QR_PAYMENT_CSB:
                afterQRPaymentCSBPlatform(result);
                break;
            case PERFORM_SALE_CSB:
                afterSaleCSB(result);
                break;
            case PERFORM_CANCEL_CSB:
                afterCancelCSB(result);
                break;
            case SHOW_QR_CODE_PAGE:
                afterShowQrCode(result);
                break;

            /** SCAN */
            case SELECT_QR_PAYMENT_BSC:
                afterQRPaymentBSCPlatform(result);
                break;
            case SCAN_QR_CODE:
                afterScanQRCode(result);
                break;
            case SHOW_CONFIRM_AMOUNT:
                afterShowConfirmAmount(result);
                break;
            case PERFORM_SALE_BSC:
                afterSaleBSC(result);
                break;

            default:
                transEnd(result);
                break;
        }
    }

    /**
     * CARD
     */
    private void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData);
        transData.setTransType(transType.name());
        if (needFallBack) {
            transData.setEnterMode(EnterMode.FALLBACK);
        }
        // enter card number manually
        currentMode = cardInfo.getSearchMode();
        if (SearchMode.isWave(currentMode)) {
            needRemoveCard = true;
            // AET-15
            gotoState(State.CLSS_PROC.toString());
        } else if (currentMode == SearchMode.SWIPE || currentMode == SearchMode.KEYIN) {
            goTipBranch();
        } else if (currentMode == SearchMode.INSERT) {
            needRemoveCard = true;
            // EMV process
            gotoState(State.EMV_PROC.toString());
        }
    }

    public void afterInquiry(ActionResult result) {
        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(new ActionResult(ret, null));
            return;
        }

        transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
        transData.setTransType(ETransType.SALE.name());
        GreendaoHelper.getTransDataHelper().update(transData);

        gotoState(State.PRINT.toString());
    }

    private void onAdjustTip(ActionResult result) {
        //get total amount
        String totalAmountStr = String.valueOf(CurrencyConverter.parse(result.getData().toString()));
        transData.setAmount(totalAmountStr);
        //get tip amount
        String tip = String.valueOf(CurrencyConverter.parse(result.getData1().toString()));
        transData.setTipAmount(tip);
        if (currentMode == SearchMode.SWIPE || currentMode == SearchMode.KEYIN) {
            if (transData.getIssuer().isNonEmvTranRequirePIN()) {
                // enter pin
                gotoState(State.ENTER_PIN.toString());
            } else {
                if (currentMode == SearchMode.SWIPE) {
                    if (transData.getEnterMode() == EnterMode.SWIPE &&
                            transData.getIssuer().getNonEmvTranFloorLimit() > Utils.parseLongSafe(transData.getAmount(), 0)) {
                        // save trans data
                        transData.setTransType(ETransType.OFFLINE_SALE.name());
                        transData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
                        transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                        GreendaoHelper.getTransDataHelper().update(transData);
                        //increase trans no.
                        Component.incTransNo();
                        toSignOrPrint();
                        return;
                    }
                }
                // online process
                gotoState(State.MAG_ONLINE.toString());
            }
        }
    }

    private void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && !pinBlock.isEmpty()) {
            transData.setHasPin(true);
        }

        if (transData.getEnterMode() == EnterMode.SWIPE &&
                transData.getIssuer().getNonEmvTranFloorLimit() > Utils.parseLongSafe(transData.getAmount(), 0)) {
            // save trans data
            transData.setTransType(ETransType.OFFLINE_SALE.name());
            transData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            GreendaoHelper.getTransDataHelper().update(transData);
            //increase trans no.
            Component.incTransNo();
            toSignOrPrint();
            return;
        }

        //clss process
        if (transData.getEnterMode() == EnterMode.CLSS &&
                ETransResult.CLSS_OC_APPROVED.name().equals(transData.getEmvResult())) {
            if (!transData.isSignFree()) {
                gotoState(State.SIGNATURE.toString());
            } else {
                gotoState(State.PRINT.toString());
            }
            return;
        }

        // online process
        gotoState(State.MAG_ONLINE.toString());
    }

    private void goTipBranch() {
        boolean enableTip = SysParam.getInstance().getBoolean(R.string.EDC_SUPPORT_TIP);
        if(enableTip) {
            enableTip = transData.getIssuer().isEnableAdjust();
        }
        //adjust tip
        long totalAmount = Utils.parseLongSafe(transData.getAmount(), 0);
        tipAmount = transData.getTipAmount();
        long lTipAmountLong = Utils.parseLongSafe(tipAmount, 0);
        long baseAmount = totalAmount - lTipAmountLong;
        percent = transData.getIssuer().getAdjustPercent();

        if (enableTip) {
            if (!hasTip) {
                gotoState(State.ADJUST_TIP.toString());
                return;
            } else if (baseAmount * percent / 100 < lTipAmountLong) {
                showAdjustTipDialog(getCurrentContext());
            }
        }

        if (currentMode == SearchMode.SWIPE || currentMode == SearchMode.KEYIN) {
            if (transData.getIssuer().isNonEmvTranRequirePIN()) {
                // enter pin
                gotoState(State.ENTER_PIN.toString());
                return;
            }
        }

        // online process
        gotoState(State.MAG_ONLINE.toString());
    }

    // need electronic signature or send
    private void toSignOrPrint() {
        if (transType == ETransType.OLS_ENQUIRY) {
            gotoState(State.PRINT.toString());
        } else if (transType == ETransType.INSTALLMENT || transType == ETransType.REDEEM) {
            gotoState(State.TRANS_CONFIRM.toString());
        } else if (Component.isSignatureFree(transData)) {
            gotoState(State.PRINT.toString());
        } else if(SmallAmtUtils.isTrxSmallAmt(transData)) {
            gotoState(State.PRINT.toString());
        } else if(transData.getHasPin()) {
            gotoState(State.PRINT.toString());
        } else {
            gotoState(State.SIGNATURE.toString());
        }
    }

    private void afterEMVProcess(ETransResult transResult) {
        EmvTransProcess.emvTransResultProcess(transResult, emv, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准
            GreendaoHelper.getTransDataHelper().update(transData);
            toSignOrPrint();
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {//脱机批准处理
            EmvTransProcess.saveCardInfoAndCardSeq(emv, transData);
            transData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            GreendaoHelper.getTransDataHelper().update(transData);
            // increase trans no.
            Component.incTransNo();
            toSignOrPrint();
        } else if (transResult == ETransResult.ONLINE_DENIED) { // refuse online
            // end trans
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
        } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// 平台批准卡片拒绝
            transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
        } else if (transResult == ETransResult.ABORT_TERMINATED ||
                transResult == ETransResult.OFFLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void afterClssProcess(CTransResult transResult) {
        if (transResult == null) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_SEE_PHONE) {
            //
            searchCardMode &= 0xC;
            gotoState(State.CLSS_PREPROC.toString());
            return;
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_TRY_AGAIN) {
            gotoState(State.CLSS_PREPROC.toString());
            return;
        }

        // 设置交易结果
        transData.setEmvResult(transResult.getTransResult().name());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE) {
            ToastUtils.showMessage("Please use contact");
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, clss, transData);

        if (transResult.getCvmResult() == ECvmResult.SIG || !Component.isSignatureFree(transData)) { // AET-283
            //do signature after online
            transData.setSignFree(false);
        } else {
            transData.setSignFree(true);
        }

        //Check small amount capability
        if(SmallAmtUtils.isTrxSmallAmt(transData)) {
            transData.setSignFree(true);
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED || transResult.getTransResult() == ETransResult.ONLINE_APPROVED) {
            transData.setOnlineTrans(transResult.getTransResult() == ETransResult.ONLINE_APPROVED);
            if (!transData.isSignFree()) {
                gotoState(State.SIGNATURE.toString());
            } else {
                gotoState(State.PRINT.toString());
            }
        } else {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void showAdjustTipDialog(final Context context) {
        final CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
        dialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                dialog.dismiss();
                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            }
        });
        dialog.setConfirmClickListener(alertDialog -> {
            dialog.dismiss();
            gotoState(State.ADJUST_TIP.toString());
        });
        dialog.show();
        dialog.setNormalText(getString(R.string.prompt_tip_exceed));
        dialog.showCancelButton(true);
        dialog.showConfirmButton(true);
    }

    /**
     * QR
     */
    public void afterQRPaymentCSBPlatform(ActionResult result) {
        String platform = (String) result.getData();
        fundingSourceImagePath = (String) result.getData1();
        transData.setFundingSource(platform);

        Acquirer acquirerQR = FinancialApplication.getAcqManager().findAcquirer(AppConstants.QR_ACQUIRER);
        if (acquirerQR == null) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        transData.setEnterMode(TransData.EnterMode.QR);
        transData.setAcquirer(acquirerQR);

        String transactionIdQR = transData.getAcquirer().getTerminalId()
                + ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6)
                + ConvertUtils.convertCurrentTime(ConvertUtils.TIME_PATTERN_TRANS);
        transData.setRefNo(transactionIdQR);

        gotoState(State.PERFORM_SALE_CSB.toString());
    }

    public void afterQRPaymentBSCPlatform(ActionResult result) {
        String platform = (String) result.getData();
        transData.setFundingSource(platform);

        Acquirer acquirerScan = FinancialApplication.getAcqManager().findAcquirer(AppConstants.QR_ACQUIRER);
        if (acquirerScan == null) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        transData.setEnterMode(TransData.EnterMode.QR);
        transData.setAcquirer(acquirerScan);

        String transactionIdScan = transData.getAcquirer().getTerminalId()
                + ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6)
                + ConvertUtils.convertCurrentTime(ConvertUtils.TIME_PATTERN_TRANS);
        transData.setRefNo(transactionIdScan);

        gotoState(State.SCAN_QR_CODE.toString());
    }


    public void afterShowQrCode(ActionResult result) {
        int ret = result.getRet();
        switch (ret) {
            case TransResult.SUCC:
                gotoState(State.INQUIRY.toString());
                break;
            case TransResult.ERR_USER_CANCEL:
            case TransResult.ERR_TIMEOUT:
                transData.setTransState(TransData.ETransStatus.SUSPENDED);
                GreendaoHelper.getTransDataHelper().update(transData);
                transEnd(result);
                break;
            default:
                transEnd(result);
        }
    }

    public void afterSaleCSB(ActionResult result) {
        qrCode = (String) result.getData();
        transData.setTransType(ETransType.SALE.name());
        GreendaoHelper.getTransDataHelper().update(transData);
        Component.incTransNo();
        gotoState(State.SHOW_QR_CODE_PAGE.toString());
    }

    public void afterCancelCSB(ActionResult result) {
        int ret = result.getRet();
        Object data = result.getData();

        if (ret == TransResult.SUCC) {
            transData.setTransType(ETransType.SALE.name());
            transData.setReversalStatus(TransData.ReversalStatus.REVERSAL);
            GreendaoHelper.getTransDataHelper().update(transData);

            transEnd(new ActionResult(TransResult.ERR_USER_CANCEL, data));
        } else {
            transEnd(result);
        }
    }

    /**
     * SCAN
     */
    private void afterScanQRCode(ActionResult result) {
        qrCode = result.getData().toString();

        if (qrCode == null || qrCode.length() == 0) {
            transEnd(new ActionResult(TransResult.ERR_INVALID_EMV_QR, null));
            return;
        }

        transData.setQrCode(qrCode);

        gotoState(State.SHOW_CONFIRM_AMOUNT.toString());
    }

    private void afterShowConfirmAmount(ActionResult result) {
        gotoState(State.PERFORM_SALE_BSC.toString());
    }

    private void afterSaleBSC(ActionResult result) {
        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(new ActionResult(ret, null));
            return;
        }

        Object data = result.getData();
        if (data != null) {
            ArrayList<Object> dataObject = (ArrayList<Object>) result.getData();

            if (dataObject.get(0) != null) {
                transData.setBillPaymentRef1((String) dataObject.get(0));
            }

            if (dataObject.get(1) != null) {
                transData.setBillPaymentRef2((String) dataObject.get(1));
            }

            if (dataObject.get(2) != null) {
                transData.setBillPaymentRef3((String) dataObject.get(2));
            }
        }

        transData.setTransType(ETransType.SALE.name());
        transData.setTransState(TransData.ETransStatus.PENDING);
        GreendaoHelper.getTransDataHelper().insert(transData);
        Component.incTransNo();

        gotoState(State.INQUIRY.toString());
    }
}
