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
package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.pay.emv.EmvTransProcess;
import com.evp.pay.trans.action.ActionEmvProcess;
import com.evp.pay.trans.action.ActionEnterAmount;
import com.evp.pay.trans.action.ActionEnterAuthCode;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.pay.trans.action.ActionSignature;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

/**
 * The type Offline sale trans.
 */
public class OfflineSaleTrans extends BaseTrans {

    /**
     * Instantiates a new Offline sale trans.
     *
     * @param context       the context
     * @param transListener the trans listener
     */
    public OfflineSaleTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.OFFLINE_SALE, transListener);
    }

    @Override
    protected void bindStateOnAction() {

        //Enter PWD action
        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        getCurrentContext(),
                        6,
                        String.format("%s %s", ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_OFFLINE_SALE), ConfigUtils.getInstance().getString(ConfigConst.LABEL_PASSWORD)),
                        null
                )
        );
        bind(State.INPUT_PWD.toString(), inputPasswordAction, true);

        // search card action
        ActionSearchCard searchCardAction = new ActionSearchCard(action -> ((ActionSearchCard) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_OFFLINE_SALE),
                        Component.getCardReadMode(ETransType.OFFLINE_SALE),
                        transData.getAmount(),
                        null,
                        ""
                )
        );
        bind(State.CHECK_CARD.toString(), searchCardAction, true);

        // input amount and tip amount action
        ActionEnterAmount amountAction = new ActionEnterAmount(action -> ((ActionEnterAmount) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_OFFLINE_SALE),
                        false
                )
        );
        bind(State.ENTER_AMOUNT.toString(), amountAction, true);

        //enter auth code action
        ActionEnterAuthCode enterAuthCodeAction = new ActionEnterAuthCode(action -> ((ActionEnterAuthCode) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_OFFLINE_SALE),
                        getString(R.string.prompt_auth_code),
                        transData.getAmount()
                )
        );
        bind(State.ENTER_AUTH_CODE.toString(), enterAuthCodeAction, true);

        // enter pin action
        ActionEnterPin enterPinAction = new ActionEnterPin(action -> ((ActionEnterPin) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_OFFLINE_SALE),
                        transData.getPan(),
                        true,
                        getString(R.string.prompt_pin),
                        getString(R.string.prompt_no_pin),
                        transData.getAmount(),
                        transData.getTipAmount(),
                        ActionEnterPin.EEnterPinType.ONLINE_PIN,
                        KeyUtils.getTpkIndex(transData.getAcquirer().getTleKeySetId())
                )
        );
        bind(State.ENTER_PIN.toString(), enterPinAction, true);

        // signature action
        ActionSignature signatureAction = new ActionSignature(action -> ((ActionSignature) action)
                .setParam(
                        getCurrentContext(),
                        transData.getAmount()
                )
        );
        bind(State.SIGNATURE.toString(), signatureAction);

        //print preview action
        PrintTask printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(OfflineSaleTrans.this, SaleTrans.State.PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.PRINT.toString(), printTask);

        // emv process action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(action -> ((ActionEmvProcess) action)
                .setParam(
                        getCurrentContext(),
                        emv,
                        transData
                )
        );
        bind(State.EMV_PROC.toString(), emvProcessAction);

        // perform the first action
        gotoState(State.INPUT_PWD.toString());
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
         * Check card state.
         */
        CHECK_CARD,
        /**
         * Enter amount state.
         */
        ENTER_AMOUNT,
        /**
         * Enter auth code state.
         */
        ENTER_AUTH_CODE,
        /**
         * Enter pin state.
         */
        ENTER_PIN,
        /**
         * Signature state.
         */
        SIGNATURE,
        /**
         * Print state.
         */
        PRINT,
        /**
         * Emv proc state.
         */
        EMV_PROC,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);

        switch (state) {
            case INPUT_PWD:
                onInputPwd(result);
                break;
            case CHECK_CARD:
                onCheckCard(result);
                break;
            case ENTER_AMOUNT:
                onEnterAmount(result);
                break;
            case ENTER_AUTH_CODE:
                onEnterAuthCode(result);
                break;
            case ENTER_PIN:
                onEnterPin(result);
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
            case EMV_PROC:
                //get trans result
                CTransResult transResult = (CTransResult) result.getData();
                // EMV完整流程 脱机批准或联机批准都进入签名流程
                afterEMVProcess(transResult.getTransResult());
                break;
            default:
                transEnd(result);
                break;
        }
    }

    private void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData);
        transData.setTransType(ETransType.OFFLINE_SALE.name());
        byte currentMode = cardInfo.getSearchMode();
        if (currentMode == SearchMode.SWIPE || currentMode == SearchMode.KEYIN) {
            Issuer issuer = transData.getIssuer();
            if (issuer != null) {
                if (!issuer.isEnableOffline()) {
                    transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                    return;
                }
            } else {
                transEnd(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
                return;
            }
            //enter auth code
            gotoState(State.ENTER_AUTH_CODE.toString());
            return;
        } else if (currentMode == SearchMode.INSERT) {
            needRemoveCard = true;
            // EMV process
            gotoState(SaleTrans.State.EMV_PROC.toString());
            return;
        }

        transEnd(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
    }

    private void onEnterAmount(ActionResult result) {
        //set total amount
        transData.setAmount(result.getData().toString());
        gotoState(State.CHECK_CARD.toString());
    }

    private void onEnterAuthCode(ActionResult result) {
        //get auth code
        String authCode = (String) result.getData();
        //set auth code
        transData.setAuthCode(authCode);
        //enter pin
        if (transData.getIssuer().isNonEmvTranRequirePIN()) {
            gotoState(State.ENTER_PIN.toString());
        } else {
            transData.setHasPin(false);
            saveTransData();
        }
    }

    private void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && !pinBlock.isEmpty()) {
            transData.setHasPin(true);
        }
        // save trans data
        saveTransData();
    }

    private void saveTransData() {
        transData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
        transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
        //Important to increase stan no. for offline transactions because of database
        Component.incStanNo();
        GreendaoHelper.getTransDataHelper().insert(transData);
        //increase trans no.
        Component.incTransNo();
        // signature?
        if(!transData.getHasPin()) {
            gotoState(State.SIGNATURE.toString());
        } else {
            gotoState(State.PRINT.toString());
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

    private void afterEMVProcess(ETransResult transResult) {
        EmvTransProcess.emvTransResultProcess(transResult, emv, transData);
        if(transResult == ETransResult.SIMPLE_FLOW_END) {
            Issuer issuer = transData.getIssuer();
            if (issuer != null) {
                if (!issuer.isEnableOffline()) {
                    transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                    return;
                }
            } else {
                transEnd(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
                return;
            }
            //enter auth code
            gotoState(State.ENTER_AUTH_CODE.toString());
        } else {
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    private void onInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.OFFLINE_SALE_PASSWORD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        gotoState(State.ENTER_AMOUNT.toString());
    }
}
