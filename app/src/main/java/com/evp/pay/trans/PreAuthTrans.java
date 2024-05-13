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
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.EnterMode;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.smallamt.SmallAmtUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.pay.emv.EmvTags;
import com.evp.pay.emv.EmvTransProcess;
import com.evp.pay.emv.clss.ClssTransProcess;
import com.evp.pay.trans.action.ActionClssPreProc;
import com.evp.pay.trans.action.ActionClssProcess;
import com.evp.pay.trans.action.ActionEmvProcess;
import com.evp.pay.trans.action.ActionEnterAmount;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.pay.trans.action.ActionSignature;
import com.evp.pay.trans.action.ActionTransOnline;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;

/**
 * The type Pre auth trans.
 */
public class PreAuthTrans extends BaseTrans {
    private String amount;
    private boolean isNeedInputAmount = true; // is need input amount
    private boolean isFreePin = true;
    private boolean isSupportBypass = true;

    private byte searchCardMode = -1;
    private boolean needFallBack = false;

    /**
     * Instantiates a new Pre auth trans.
     *
     * @param context       the context
     * @param isFreePin     the is free pin
     * @param transListener the trans listener
     */
    public PreAuthTrans(Context context, boolean isFreePin, TransEndListener transListener) {
        super(context, ETransType.PREAUTH, transListener);
        this.isFreePin = isFreePin;
        isNeedInputAmount = true;
        searchCardMode = Component.getCardReadMode(ETransType.PREAUTH);
    }

    /**
     * Instantiates a new Pre auth trans.
     *
     * @param context       the context
     * @param amount        the amount
     * @param transListener the trans listener
     */
    public PreAuthTrans(Context context, String amount, TransEndListener transListener) {
        super(context, ETransType.PREAUTH, transListener);
        this.amount = amount;
        isNeedInputAmount = false;
        searchCardMode = Component.getCardReadMode(ETransType.PREAUTH);
    }

    @Override
    protected void bindStateOnAction() {
        // enter amount action
        ActionEnterAmount amountAction = new ActionEnterAmount(action -> ((ActionEnterAmount) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_PRE_AUTH),
                        false
                )
        );
        bind(State.ENTER_AMOUNT.toString(), amountAction, true);

        // read card
        ActionSearchCard searchCardAction = new ActionSearchCard(action -> ((ActionSearchCard) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_PRE_AUTH),
                        searchCardMode,
                        transData.getAmount(),
                        null,
                        ""
                )
        );
        bind(State.CHECK_CARD.toString(), searchCardAction, true);

        // input password action
        ActionEnterPin enterPinAction = new ActionEnterPin(action -> {
            // if quick pass by pin, set isSupportBypass as false,input password
            if (!isFreePin) {
                isSupportBypass = false;
            }
            ((ActionEnterPin) action)
                    .setParam(
                            getCurrentContext(),
                            ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_PRE_AUTH),
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

        // emv action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(action -> ((ActionEmvProcess) action)
                .setParam(
                        getCurrentContext(),
                        emv,
                        transData
                )
        );
        bind(State.EMV_PROC.toString(), emvProcessAction);

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
        PrintTask printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(PreAuthTrans.this, SaleTrans.State.PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.PRINT.toString(), printTask);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(action -> ((ActionClssPreProc) action)
                .setParam(
                        clss,
                        transData
                )
        );
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(action -> ((ActionClssProcess) action)
                .setParam(
                        getCurrentContext(),
                        clss,
                        transData
                )
        );
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        // execute the first action
        if (isNeedInputAmount) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            transData.setAmount(amount);
            gotoState(State.CLSS_PREPROC.toString());
        }
    }

    /**
     * The enum State.
     */
    enum State {
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
         * Emv proc state.
         */
        EMV_PROC,
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
         * Clss preproc state.
         */
        CLSS_PREPROC,
        /**
         * Clss proc state.
         */
        CLSS_PROC,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        int ret = result.getRet();
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(emv, transType, true);
            if (f55Dup.length > 0) {
                TransData dupTransData = GreendaoHelper.getTransDataHelper().findFirstDupRecord();
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

        switch (state) {
            case ENTER_AMOUNT:// 输入交易金额后续处理
                // save amount
                transData.setAmount(result.getData().toString());
                gotoState(State.CLSS_PREPROC.toString());
                break;
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case MAG_ONLINE: // after online
                // judge whether need signature or print
                toSignOrPrint();
                break;
            case EMV_PROC: // emv后续处理
                onEmvProc(result);
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
            case CLSS_PREPROC:
                gotoState(SaleTrans.State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                CTransResult clssResult = (CTransResult) result.getData();
                afterClssProcess(clssResult);
                break;
            default:
                transEnd(result);
                break;
        }
    }

    private void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData);
        transData.setTransType(ETransType.PREAUTH.name());
        if (needFallBack) {
            transData.setEnterMode(EnterMode.FALLBACK);
        }
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (SearchMode.isWave(mode)) {
            needRemoveCard = true;
            gotoState(State.CLSS_PROC.toString());
        } else if (mode == SearchMode.KEYIN || mode == SearchMode.SWIPE) {
            // input password
            if (transData.getIssuer().isNonEmvTranRequirePIN()) {
                gotoState(State.ENTER_PIN.toString());
            } else {
                transData.setHasPin(false);
                gotoState(State.MAG_ONLINE.toString());
            }
        } else if (mode == SearchMode.INSERT) {
            needRemoveCard = true;
            // EMV处理
            gotoState(State.EMV_PROC.toString());
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

    private void onEmvProc(ActionResult result) {
        ETransResult transResult = ((CTransResult) result.getData()).getTransResult();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        EmvTransProcess.emvTransResultProcess(transResult, emv, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            // judge whether need signature or print
            toSignOrPrint();

        } else if (transResult == ETransResult.ONLINE_DENIED) { // online denied
            // transaction end
            transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
        } else if (transResult == ETransResult.ONLINE_CARD_DENIED) {// platform approve card denied
            transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
        } else if (transResult == ETransResult.ABORT_TERMINATED) { // emv terminated
            // transaction end
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else if (transResult == ETransResult.OFFLINE_DENIED) {
            // transaction end
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    // 判断是否需要电子签名或打印
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

    private void onSignature(ActionResult result) {
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

        // if terminal does not support signature ,card holder does not sign or time out，print preview directly.
        gotoState(State.PRINT.toString());
    }

    private void afterClssProcess(CTransResult transResult) {
        if (transResult == null) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_SEE_PHONE) {
            searchCardMode &= 0xC;
            gotoState(SaleTrans.State.CLSS_PREPROC.toString());
            return;
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_TRY_AGAIN) {
            gotoState(SaleTrans.State.CLSS_PREPROC.toString());
            return;
        }

        transData.setEmvResult(transResult.getTransResult().name());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED
                || transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED)
        {
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

        if (transResult.getCvmResult() == ECvmResult.SIG || !Component.isSignatureFree(transData)) {
            transData.setSignFree(false);
        } else {
            transData.setSignFree(true);
        }

        //Check small amount capability
        if(SmallAmtUtils.isTrxSmallAmt(transData)) {
            transData.setSignFree(true);
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED
                || transResult.getTransResult() == ETransResult.ONLINE_APPROVED)
        {
            transData.setOnlineTrans(transResult.getTransResult() == ETransResult.ONLINE_APPROVED);
            if (!transData.isSignFree()) {
                gotoState(SaleTrans.State.SIGNATURE.toString());
            } else {
                gotoState(SaleTrans.State.PRINT.toString());
            }
        } else {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }
}
