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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.EnterMode;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.params.ParamHelper;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.utils.LogUtils;
import com.evp.commonlib.utils.MemoryUtils;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.EmvImpl;
import com.evp.eemv.IClss;
import com.evp.eemv.IEmv;
import com.evp.eemv.clss.ClssImpl;
import com.evp.invoke.InvokeConst;
import com.evp.invoke.InvokeResponseData;
import com.evp.pay.MainActivity;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.action.ActionRemoveCard;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.pay.trans.action.ActionTransPreDeal;
import com.evp.pay.trans.action.ActionUpdateParam;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.task.BaseTask;
import com.evp.pay.utils.ToastUtils;
import com.evp.payment.evpscb.BuildConfig;
import com.evp.poslib.neptune.Sdk;
import com.pax.dal.IPed;
import com.pax.dal.IPicc;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.exceptions.PedDevException;
import com.pax.dal.exceptions.PiccDevException;

/**
 * The type Base trans.
 */
public abstract class BaseTrans extends BaseTask {
    /**
     * The Trans type.
     */
// 当前交易类型
    protected ETransType transType;
    /**
     * The Emv.
     */
    protected IEmv emv;
    /**
     * The Clss.
     */
    protected IClss clss;
    /**
     * The Trans data.
     */
    protected TransData transData;
    /**
     * The Need remove card.
     */
//AET-160
    boolean needRemoveCard = false;
    //AET-199
    private Activity old;

    private boolean backToMain = false;
    private boolean isFromThirdParty = false;

    /**
     * whether transaction is running, it's global for all transaction, if insert a transaction in one transaction, control the status itself
     */
    private static boolean isTransRunning = false;
    private static boolean isUpdating = false;

    /**
     * Instantiates a new Base trans.
     *
     * @param context       the context
     * @param transType     the trans type
     * @param transListener the trans listener
     */
    BaseTrans(Context context, ETransType transType, TransEndListener transListener) {
        super(context, transListener);
        this.transType = transType;
    }

    /**
     * set transaction type
     *
     * @param transType the trans type
     */
    public void setTransType(ETransType transType) {
        this.transType = transType;
    }

    /**
     * Sets trans listener.
     *
     * @param transListener the trans listener
     */
    protected void setTransListener(TransEndListener transListener) {
        this.transListener = transListener;
    }

    /**
     * Sets back to main.
     *
     * @param backToMain the back to main
     * @return the back to main
     */
// AET-251
    public BaseTrans setBackToMain(boolean backToMain) {
        this.backToMain = backToMain;
        return this;
    }

    /**
     * Sets is from third party.
     *
     * @param isFromThirdParty the is from third party
     * @return the is from third party
     */
    public BaseTrans setIsFromThirdParty(boolean isFromThirdParty) {
        this.isFromThirdParty = isFromThirdParty;
        return this;
    }
    /**
     * transaction result prompt
     */
    @Override
    protected void transEnd(final ActionResult result) {
        InvokeResponseData.createResponseData(transType, result, transData);

        LogUtils.i(TAG, transType.toString() + " TRANS--END--");
        clear(); // no memory leak
        dispResult(transType.getTransName(), result, arg0 -> FinancialApplication.getApp().runInBackground(new DismissRunnable(result)));
        setTransRunning(false);
        //dispatch To the end of MessageQueue,you must use runOnUiThread.
        FinancialApplication.getApp().runOnUiThread(() -> FinancialApplication.setCurrentETransType(null));
        if (!ParamHelper.isClssInternal() && ParamHelper.isExternalTypeAPed()){
            try {
                IPed ped = PedHelper.getPed();
                ped.clearScreen();
                ped.showStr((byte) 0x68, (byte) 0x01,"WELCOME!");
                ped.showStr((byte) 0x54, (byte) 0x04,"PAX TECHNOLOGY");
            } catch (PedDevException e) {
                LogUtils.e(e);
            }
        }
        if (!BuildConfig.RELEASE){
            int memory = MemoryUtils.getMemory();
            LogUtils.fd("BaseTrans",String.format("memory = %s", memory));
        }

        if (transType == ETransType.VOID && result.getRet() == TransResult.SUCC) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(InvokeConst.VOID_END_EVENT));
        }
    }

    public static void setUpdating(boolean updating) {
        LogUtils.i(TAG, "SetUpdating: " + updating);
        isUpdating = updating;
    }

    private class DismissRunnable implements Runnable {
        private final ActionResult result;

        /**
         * Instantiates a new Dismiss runnable.
         *
         * @param result the result
         */
        DismissRunnable(ActionResult result) {
            this.result = result;
        }

        @Override
        public void run() {
            if (BuildConfig.needRemoveCard){
                removeCard();
            }
            try {
                IPicc picc = FinancialApplication.getDal().getPicc(EPiccType.INTERNAL);
                picc.close();
            } catch (PiccDevException e) {
                LogUtils.e(TAG, "", e);
            }

            if (backToMain) {
                ActivityStack.getInstance().popTo(MainActivity.class);
            } else if (old == null) {
                ActivityStack.getInstance().pop();
            }else if (isFromThirdParty){
                //do nothing,第三方调用时，解决RESULT_CANCELED导致response==null
                LogUtils.d(TAG,"FromThirdParty");
            }
            else {
                ActivityStack.getInstance().popTo(old);
            }

            TransContext.getInstance().setCurrentAction(null);
            Device.enableStatusBar(true);
            Device.enableHomeRecentKey(true);
            if (transListener != null) {
                transListener.onEnd(result);
            }
        }

        /**
         * remove card check, need start thread when call this function
         */
        private void removeCard() {
            // avoid prompting warning message for some no card transaction, like settlement
            //AET-160
            if (!needRemoveCard)
                return;

            new ActionRemoveCard(action -> ((ActionRemoveCard) action)
                    .setParam(
                            getCurrentContext(),
                            transType.getTransName()
                    )
            ).execute();
        }
    }

    /**
     * override execute， add function to judge whether transaction check is running and add transaction pre-deal
     */
    @Override
    public void execute() {
        LogUtils.i(TAG, transType.toString() + " TRANS--START--");
        FinancialApplication.setCurrentETransType(transType);
        if (isTransRunning()) {
            setTransRunning(false);
            return;
        }
        if (isUpdating) {
            ToastUtils.showMessage(ConfigUtils.getInstance().getString("errOperationMessage"));
            return;
        }
        setTransRunning(true);
        old = ActivityStack.getInstance().top();
        if (Sdk.isPaxDevice()) {
            emv = new EmvImpl().getEmv();
            clss = new ClssImpl().getClss();
        }

        // transData initial
        transData = Component.transInit();
        ActionTransPreDeal preDealAction = new ActionTransPreDeal(action -> ((ActionTransPreDeal) action)
                .setParam(
                        getCurrentContext(), transType
                )
        );

        preDealAction.setEndListener((action, result) -> {
            if (result.getRet() != TransResult.SUCC) {
                transEnd(result);
                return;
            }
            transData.setTransType(transType.name());
            transData.setProcCode(transType.getProcCode());
            Device.enableStatusBar(false);
            Device.enableHomeRecentKey(false);

            if (transType != ETransType.SETTLE) {
                ActionUpdateParam actionUpdateParam = new ActionUpdateParam(action1 -> ((ActionUpdateParam) action1)
                        .setParam(
                                getCurrentContext(),
                                false
                        )
                );

                actionUpdateParam.setEndListener((action12, result1) -> {
                    if (result1.getRet() != TransResult.SUCC) {
                        transEnd(result1);
                        return;
                    }
                    exe();
                });

                actionUpdateParam.execute();
            } else {
                exe();
            }
        });

        preDealAction.execute();
    }

    /**
     * execute father execute()
     */
    private void exe() {
        super.execute();
    }

    /**
     * get transaction running status
     *
     * @return boolean
     */
    public static boolean isTransRunning() {
        return isTransRunning;
    }

    /**
     * set transaction running status
     *
     * @param isTransRunning the is trans running
     */
    public static void setTransRunning(boolean isTransRunning) {
        BaseTrans.isTransRunning = isTransRunning;
    }

    /**
     * save card information and input type after search card
     *
     * @param cardInfo  the card info
     * @param transData the trans data
     */
    void saveCardInfo(ActionSearchCard.CardInformation cardInfo, TransData transData) {
        // manual input card number
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.INSERT || SearchMode.isWave(mode)) {
            transData.setEnterMode(mode == SearchMode.INSERT ? EnterMode.INSERT : EnterMode.CLSS);
        }else if (mode == SearchMode.KEYIN) {
            FinancialApplication.getAcqManager().findIssuerAndSetAcquirerByPan(cardInfo.getPan(), transData);
            transData.setPan(cardInfo.getPan());
            transData.setExpDate(cardInfo.getExpDate());
            transData.setEnterMode(EnterMode.MANUAL);
            transData.setIssuer(cardInfo.getIssuer());
        } else if (mode == SearchMode.SWIPE) {
            FinancialApplication.getAcqManager().findIssuerAndSetAcquirerByPan(cardInfo.getPan(), transData);
            transData.setTrack1(cardInfo.getTrack1());
            transData.setTrack2(cardInfo.getTrack2());
            transData.setTrack3(cardInfo.getTrack3());
            transData.setPan(cardInfo.getPan());
            transData.setExpDate(TrackUtils.getExpDate(cardInfo.getTrack2()));
            if(cardInfo.getIsThisFallback()) {
                transData.setEnterMode(EnterMode.FALLBACK);
            } else {
                transData.setEnterMode(EnterMode.SWIPE);
            }
            transData.setIssuer(cardInfo.getIssuer());
        } else if (mode == SearchMode.QR){
            transData.setEnterMode(EnterMode.QR);
        }
    }

    @Override
    public void gotoState(String state) {
        LogUtils.i(TAG, transType.toString() + " ACTION--" + state + "--start");
        super.gotoState(state);
    }
}
