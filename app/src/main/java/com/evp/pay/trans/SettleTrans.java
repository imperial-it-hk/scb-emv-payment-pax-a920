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
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionSelectAcquirer;
import com.evp.pay.trans.action.ActionSettle;
import com.evp.pay.trans.action.ActionUpdateParam;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.util.ArrayList;

/**
 * The type Settle trans.
 */
public class SettleTrans extends BaseTrans {

    private ArrayList<String> selectAcqs;
    private boolean thisIsAutoSettle;

    /**
     * Instantiates a new Settle trans.
     *
     * @param context  the context
     * @param listener the listener
     */
    public SettleTrans(Context context, TransEndListener listener) {
        super(context, ETransType.SETTLE, listener);
    }

    public SettleTrans(Context context, TransEndListener listener, ArrayList<String> acquirers, boolean thisIsAutoSettle) {
        super(context, ETransType.SETTLE, listener);
        this.selectAcqs = acquirers;
        this.thisIsAutoSettle = thisIsAutoSettle;
    }

    @Override
    protected void bindStateOnAction() {

        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        getCurrentContext(),
                        6,
                        String.format("%s %s", ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_SETTLE), ConfigUtils.getInstance().getString(ConfigConst.LABEL_PASSWORD)),
                        null
                )
        );
        bind(State.INPUT_PWD.toString(), inputPasswordAction, true);

        ActionSelectAcquirer actionSelectAcquirer = new ActionSelectAcquirer(action -> ((ActionSelectAcquirer) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString("selectAcquirer")
                )
        );
        bind(State.SELECT_ACQ.toString(), actionSelectAcquirer, true);

        ActionSettle settleAction = new ActionSettle(action -> ((ActionSettle) action)
                .setParam(
                        getCurrentContext(),
                        thisIsAutoSettle
                                ? ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_AUTO_SETTLE)
                                : ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_SETTLE),
                        selectAcqs,
                        thisIsAutoSettle
                )
        );
        bind(State.SETTLE.toString(), settleAction);

        ActionUpdateParam actionUpdateParam = new ActionUpdateParam(action -> ((ActionUpdateParam) action)
                .setParam(
                        ActivityStack.getInstance().top(),
                        false
                )
        );
        bind(State.UPDATE_PARAM.toString(), actionUpdateParam);

        if(thisIsAutoSettle) {
            gotoState(State.SETTLE.toString());
            return;
        }

        if (SysParam.getInstance().getBoolean(R.string.OTHTC_VERIFY)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            gotoState(State.SETTLE.toString());
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
         * Select acq state.
         */
        SELECT_ACQ,
        /**
         * Settle state.
         */
        SETTLE,
        /**
         * Update param state.
         */
        UPDATE_PARAM
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        switch (state) {
            case INPUT_PWD:
                String data = (String) result.getData();
                if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SETTLE_PASSWORD))) {
                    if (selectAcqs != null) {
                        selectAcqs.clear();
                    }
                    transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }
                gotoState(State.SELECT_ACQ.toString());
                break;
            case SELECT_ACQ:
                //noinspection unchecked
                selectAcqs = (ArrayList<String>) result.getData();
                gotoState(State.SETTLE.toString());
                break;
            case SETTLE:
                if (result.getRet() == TransResult.ERR_USER_CANCEL) {
                    gotoState(State.SELECT_ACQ.toString());
                } else if (result.getRet() == TransResult.SUCC){
                    gotoState(State.UPDATE_PARAM.toString());
                }else {
                    transEnd(result);
                }
                break;
            case UPDATE_PARAM:
                if (selectAcqs != null){
                    selectAcqs.clear();
                }
                transEnd(result);
                break;
            default:
                break;
        }
    }

}
