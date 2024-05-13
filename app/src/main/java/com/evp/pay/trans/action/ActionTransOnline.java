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
package com.evp.pay.trans.action;

import android.app.Activity;
import android.content.Context;
import android.os.ConditionVariable;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.dcc.DccUtils;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.trans.transmit.Transmit;
import com.pax.dal.exceptions.PedDevException;

import java.util.Locale;

/**
 * The type Action trans online.
 */
public class ActionTransOnline extends AAction {
    private TransProcessListener transProcessListenerImpl;
    private Context context;
    private TransData transData;
    protected ConditionVariable cv;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionTransOnline(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context   the context
     * @param transData the trans data
     */
    public void setParam(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                transProcessListenerImpl = new TransProcessListenerImpl(context);

                //DCC processing for MAG and KEYIN
                Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(AppConstants.DCC_ACQUIRER);
                if(DccUtils.isTrxDcc(acquirer, transData.getTransType())) {
                    TransData backupTransData = transData;
                    transData = new TransData(transData);
                    transData.setTransType(ETransType.DCC_GET_RATE.name());
                    transData.setProcCode(ETransType.DCC_GET_RATE.getProcCode());
                    transData.setAcquirer(acquirer);
                    transData.setNii(acquirer.getNii());

                    EOnlineResult ret = EOnlineResult.ABORT;
                    try {
                        ret = new Transmit().dccOnlineProc(transData, transProcessListenerImpl);
                    } catch (PedDevException e) { }
                    if(ret != EOnlineResult.APPROVE) {
                        backupTransData.setStanNo(Component.getStanNo());
                        transData = backupTransData;
                    } else {
                        backupTransData.setDccExchangeRate(transData.getDccExchangeRate());
                        backupTransData.setDccForeignAmount(transData.getDccForeignAmount());
                        backupTransData.setDccCurrencyCode(transData.getDccCurrencyCode());
                        backupTransData.setStanNo(Component.getStanNo());
                        transData = backupTransData;

                        cv = new ConditionVariable();
                        dccGetCardholderDecision();
                        cv.block();
                    }
                }

                //Standard processing
                int ret = 0;
                try {
                    ret = new Transmit().transmit(transData, transProcessListenerImpl);
                } catch (PedDevException e) {
                    transProcessListenerImpl.onShowErrMessage(e.getMessage(),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                transProcessListenerImpl.onHideProgress();
                setResult(new ActionResult(ret, null));
            }
        });
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

        actionDcc.setEndListener(new ActionTransOnline.DccGetCardholderDecisionEndAction());
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
            }
            if (cv != null) {
                cv.open();
            }
            ActivityStack.getInstance().popTo((Activity) context);
        }
    }
}