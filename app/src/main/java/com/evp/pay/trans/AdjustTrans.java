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
 * 20190108  	         caowb                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.util.LinkedHashMap;

/*
 * according to the EDC Prolin, Tip of Sale,
 * Offline sale can be adjusted times before settlement.
 */

/**
 * The type Adjust trans.
 */
public class AdjustTrans extends BaseTrans {
    private String origTransNo;

    // the adjustment is just a state for a transaction,
    // so it cannot use the logic of base transaction which uses transData from the BaseTrans,
    // and in the db, each record has its id,
    // which means we cannot call transData.save() cuz it will create an excess record,
    // and we cannot call the transData.updateTrans() either, cuz the record with new id is not existed.
    // So we have to use the origTransData instead of transData.
    private TransData origTransData;
    /**
     * is need read transaction record
     */
    private boolean isNeedFindOrigTrans;
    /**
     * is need input transaction NO
     */
    private boolean isNeedInputTransNo;

    /**
     * Instantiates a new Adjust trans.
     *
     * @param context       the context
     * @param transListener the trans listener
     */
    public AdjustTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.ADJUST, transListener);
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = true;
    }

    /**
     * Instantiates a new Adjust trans.
     *
     * @param context       the context
     * @param origTransData the orig trans data
     * @param transListener the trans listener
     */
    public AdjustTrans(Context context, TransData origTransData, TransEndListener transListener) {
        super(context, ETransType.ADJUST, transListener); // ignore the type, cuz we are using the origTransData
        this.origTransData = origTransData;
        isNeedFindOrigTrans = false;
        isNeedInputTransNo = false;
    }

    /**
     * Instantiates a new Adjust trans.
     *
     * @param context       the context
     * @param origTransNo   the orig trans no
     * @param transListener the trans listener
     */
    public AdjustTrans(Context context, String origTransNo, TransEndListener transListener) {
        super(context, ETransType.ADJUST, transListener);
        this.origTransNo = origTransNo;
        isNeedFindOrigTrans = true;
        isNeedInputTransNo = false;
    }

    @Override
    protected void bindStateOnAction() {
        //input manager password
        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        getCurrentContext(),
                        6,
                        String.format("%s %s", ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_ADJUST), ConfigUtils.getInstance().getString(ConfigConst.LABEL_PASSWORD)),
                        null));
        bind(AdjustTrans.State.INPUT_PWD.toString(), inputPasswordAction, true);

        //input original trance no
        ActionInputTransData enterTransNoAction = new ActionInputTransData(action -> {
            Context context = getCurrentContext();
            ((ActionInputTransData) action)
                    .setParam(
                            context,
                            ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_ADJUST)
                    )
                    .setInputLine(
                            getString(R.string.prompt_input_transno),
                            ActionInputTransData.EInputType.NUM,
                            6,
                            true
                    );
        });
        bind(AdjustTrans.State.ENTER_TRANSNO.toString(), enterTransNoAction, true);

        // input new tips
        ActionInputTransData newTipsAction = new ActionInputTransData(action -> {
            String title = ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_ADJUST);
            String transAmount = origTransData.getAmount();
            String transTips = origTransData.getTipAmount();
            float adjustPercent = origTransData.getIssuer().getAdjustPercent();

            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put(getString(R.string.prompt_total_amount), transAmount);
            map.put(getString(R.string.prompt_ori_tips), transTips);
            map.put(getString(R.string.prompt_adjust_percent), Float.toString(adjustPercent));

            ((ActionInputTransData) action)
                    .setParam(
                            getCurrentContext(),
                            title,
                            map)
                    .setInputLine(
                            getString(R.string.prompt_new_tips),
                            ActionInputTransData.EInputType.AMOUNT,
                            Constants.AMOUNT_DIGIT,
                            false
                    );
        });
        bind(AdjustTrans.State.ENTER_AMOUNT.toString(), newTipsAction, true);

        // if need pwd for adjust
        if (SysParam.getInstance().getBoolean(R.string.OTHTC_VERIFY)) {
            gotoState(AdjustTrans.State.INPUT_PWD.toString());
        } else if (isNeedInputTransNo) {// need input trans NO
            gotoState(AdjustTrans.State.ENTER_TRANSNO.toString());
        } else {// not need input trans NO
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Utils.parseLongSafe(origTransNo, -1));
            } else {
                updateAcqIssuer();
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
         * Enter amount state.
         */
        ENTER_AMOUNT
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
            case ENTER_AMOUNT:
                onEnterAmount(result);
                break;
            default:
                break;
        }
    }

    private void onInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.MERCHANT_PASSWORD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        if (isNeedInputTransNo) {// need input trans NO
            gotoState(AdjustTrans.State.ENTER_TRANSNO.toString());
        } else {// not need input trans NO
            if (isNeedFindOrigTrans) {
                validateOrigTransData(Utils.parseLongSafe(origTransNo, -1));
            } else {
                updateAcqIssuer();
                gotoState(State.ENTER_AMOUNT.toString());
            }
        }
    }

    private void onEnterTraceNo(ActionResult result) {
        String content = (String) result.getData();
        long transNo;
        if (content == null) {
            TransData tempTransData = GreendaoHelper.getTransDataHelper().findLastTransData(true);
            if (tempTransData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                return;
            }
            transNo = tempTransData.getTraceNo();
        } else {
            transNo = Utils.parseLongSafe(content, -1);
        }
        validateOrigTransData(transNo);
    }

    private void onEnterAmount(ActionResult result) {
        long newTotalAmount = (long) result.getData();
        long newTipAmount = (long) result.getData1();

        //base amount and tip
        origTransData.setAmount(newTotalAmount + "");
        //set tip
        origTransData.setTipAmount(newTipAmount + "");
        // update original transaction record
        //set status as adjusted
        origTransData.setTransState(TransData.ETransStatus.ADJUSTED);
        origTransData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
        GreendaoHelper.getTransDataHelper().update(origTransData);
        transEnd(result);
    }

    // check original transaction information
    private void validateOrigTransData(long origTransNo) {
        origTransData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(origTransNo);
        if (origTransData == null) {
            // no original transaction
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        // unsale or offline sale can't adjust
        ETransType trType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());
        if (trType == null){
            transEnd(new ActionResult(TransResult.ERR_NO_TRANS,null));
            return;
        }
        if (!trType.isAdjustAllowed()) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORTED, null));
            return;
        }

        // tip not open
        if (!isAdjustSupported(origTransData)) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORTED, null));
            return;
        }

        //  has voided/adjust transaction can not adjust
        TransData.ETransStatus trStatus = origTransData.getTransState();
        if (trStatus.equals(TransData.ETransStatus.VOIDED)) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }

        gotoState(State.ENTER_AMOUNT.toString());
    }

    // set original trans data
    private void updateAcqIssuer() {
        transData.setIssuer(origTransData.getIssuer());
    }

    public static boolean isAdjustSupported(TransData origTransData) {
        if (origTransData == null || !SysParam.getInstance().getBoolean(R.string.EDC_SUPPORT_TIP)) {
            return false;
        }
        Issuer issuer = origTransData.getIssuer();
        return issuer != null && issuer.isEnableAdjust();
    }
}
