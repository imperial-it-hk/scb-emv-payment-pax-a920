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
 * 20190108  	         xieYb                   Modify
 * ===========================================================================================
 */

package com.evp.mvp.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eventbus.CardDetectEvent;
import com.evp.eventbus.EmvCallbackEvent;
import com.evp.mvp.contract.SearchCardContract;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.TransContext;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * The type Search card presenter.
 */
public class SearchCardPresenter extends SearchCardContract.Presenter {
    private static final String TAG = "SearchCardPresenter";
    private boolean isManualMode = false;
    /**
     * The Is qr mode.
     */
    public boolean isQRMode = false;
    private ActionInputPassword inputPasswordAction = null;
    private final AAction searchCardAction = TransContext.getInstance().getCurrentAction();
    private int retryTime = 3;
    private String cardNo = null; // 卡号
    private byte mode = 0; // 寻卡模式
    /**
     * The Icc adjust percent.
     */
    public float iccAdjustPercent = 0;
    private boolean iccEnable = true;
    private CardReaderHelper readerHelper = new CardReaderHelper();
    private CardDetectEvent event;
    private byte readTypeResult;

    /**
     * Instantiates a new Search card presenter.
     *
     * @param context the context
     */
    public SearchCardPresenter(Context context) {
        super(context);
    }

    @Override
    public void initParam(Byte mode) {
        this.readTypeResult = 0;//reset readTypeResult
        this.mode = mode;
        this.iccEnable = SearchMode.isSupportIcc(mode);
        LogUtils.d(TAG, "mode:"+mode);
    }

    @Override
    public void runSearchCard() {
        runSearchCardThread();
    }

    @Override
    public void stopDetectCard() {
        readerHelper.stopPolling();
    }

    private void runSearchCardThread() {
        stopDetectCard();
        isManualMode = false;
        isQRMode = false;
        readerHelper.polling(mode);
    }

    private void onReadCardCancel() {
        if (!isManualMode && !isQRMode) { // AET-179
            if (proxyView != null) {
                proxyView.finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            }
        }
    }

    @Override
    public void runInputMerchantPwdAction() {
        inputPasswordAction = new ActionInputPassword(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(getContext(), 6,
                        getContext().getString(R.string.prompt_merchant_pwd), null, false);
                ((ActionInputPassword) action).setParam(TransResult.ERR_USER_CANCEL);
            }
        });

        inputPasswordAction.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                //reset to searchCardAction
                TransContext.getInstance().setCurrentAction(searchCardAction);

                if (result.getRet() != TransResult.SUCC) {
                    //AET-156
                    proxyView.finish(new ActionResult(result.getRet(), null));
                    return;
                }

                String data = (String) result.getData();
                if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.MERCHANT_PASSWORD))) {
                    //retry three times
                    retryTime--;
                    if (retryTime > 0) {
                        // AET-110, AET-157
                        proxyView.showManualPwdErr();
                    } else {
                        proxyView.finish(new ActionResult(TransResult.ERR_PASSWORD, null));
                    }
                    return;
                }

                FinancialApplication.getApp().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onVerifyManualPan();
                    }
                });
            }
        });

        inputPasswordAction.execute();
    }

    //AET-158

    /**
     * Process manual card no.
     *
     */
    public void processManualCardNo() {
        cardNo = proxyView.getCardNo().replace(" ", "");
        String expDate = proxyView.getExpDate().replace(" ", "");
        if (cardNo.length() < 13) {
            proxyView.onEditCardNoError();
            return;
        }
        if (!dateProcess(expDate)) {
            proxyView.onEditDateError();
            return;
        }
        isManualMode = true;
        stopDetectCard();
        proxyView.onEditCardNo();
        runInputMerchantPwdAction();
    }

    @Override
    public void onTimerFinish() {
        //current maybe actionInputPassword or other action,reset to searchCardAction
        TransContext.getInstance().setCurrentAction(searchCardAction);
        searchCardAction.setFinished(false); //AET-253
        if (inputPasswordAction != null) {
            inputPasswordAction.setResult(new ActionResult(TransResult.ERR_TIMEOUT, null));
        }
    }

    @Override
    public void onVerifyManualPan() {
        String date = proxyView.getExpDate().replace("/", "");
        if (!date.isEmpty()) {
            date = date.substring(2) + date.substring(0, 2);// 将MMyy转换成yyMM
        }

        Issuer matchedIssuer = FinancialApplication.getAcqManager().findIssuerByPan(cardNo);
        if (matchedIssuer == null) {
            proxyView.finish(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
            return;
        }

        if (!matchedIssuer.isAllowManualPan()) {
            proxyView.finish(new ActionResult(TransResult.ERR_UNSUPPORTED_FUNC, null));
            return;
        }

        if (!Issuer.validPan(matchedIssuer, cardNo)) {
            proxyView.finish(new ActionResult(TransResult.ERR_CARD_INVALID, null));
            return;
        }

        if (!Issuer.validCardExpiry(matchedIssuer, date)) {
            proxyView.finish(new ActionResult(TransResult.ERR_CARD_EXPIRED, null));
            return;
        }

        ActionSearchCard.CardInformation cardInfo = new ActionSearchCard.CardInformation(SearchMode.KEYIN, cardNo, date, matchedIssuer);
        proxyView.finish(new ActionResult(TransResult.SUCC, cardInfo));
    }

    @Override
    public void processManualExpDate() {
        final String content = proxyView.getExpDate().replace(" ", "");
        if (content.isEmpty()) {
            runInputMerchantPwdAction();
        } else {
            if (dateProcess(content)) {
                runInputMerchantPwdAction();
            } else {
                proxyView.onEditDateError();
            }
        }
    }

    private boolean dateProcess(String content) {
        final String mmYY = "MM/yy";
        if (content.length() != mmYY.length()) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(mmYY, Locale.US);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(content);
        } catch (ParseException e) {
            LogUtils.w(TAG, "", e);
            return false;
        }

        return true;
    }

    @Override
    public void onOkClicked() {
        byte resultType = event.type;
        if (resultType == SearchMode.INSERT) {
            boolean enableTip = SysParam.getInstance().getBoolean(R.string.EDC_SUPPORT_TIP);
            if (enableTip && iccAdjustPercent > 0) {
                proxyView.goToAdjustTip();
            } else {
                FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.CARD_NUM_CONFIRM_SUCCESS));
            }
        } else if (resultType == SearchMode.SWIPE) {
            processMag();
        }
    }

    @Override
    public void onHeaderBackClicked() {
        if (readTypeResult == SearchMode.INSERT) {
            FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.CARD_NUM_CONFIRM_ERROR));
        } else {
            FinancialApplication.getApp().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onReadCardCancel();
                }
            });
        }
    }

    @Override
    public void onKeyBackDown() {
        if (readTypeResult == SearchMode.INSERT) {
            FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.CARD_NUM_CONFIRM_ERROR));
        } else {
            if (isManualMode) {
                proxyView.resetEditText();
                if (cardNo != null) {
                    cardNo = null;
                    runSearchCardThread();
                    proxyView.onClickBack();
                }
            } else {
                FinancialApplication.getApp().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onReadCardCancel();
                    }
                });
            }
        }
    }

    @Override
    public void onReadCardOk(CardDetectEvent event) {
        this.event = event;
        readTypeResult = event.type;
        if (readTypeResult == SearchMode.INTERNAL_WAVE) {
            proxyView.onPiccDetectOk();
        } else if (readTypeResult == SearchMode.INSERT) {
            proxyView.onIccDetectOk();
        } else if (readTypeResult == SearchMode.SWIPE) {
            if (SearchMode.isSupportIcc(mode) && TrackUtils.isIcCard(event.trackData2) && !event.isThisFallback) {
                Device.beepErr();
                LogUtils.d(TAG, "Card swipe detected but ICC card used.");
                if (iccEnable) {
                    proxyView.iccCardMagReadOk(mode);
                } else {
                    proxyView.onReadCardError();
                }
                runSearchCardThread();
                return;
            }
            Device.beepPrompt();
            String pan = TrackUtils.getPan(event.trackData2);
            String exp = TrackUtils.getExpDate(event.trackData2);

            if (exp == null || exp.length() != 4) {
                Device.beepErr();
                proxyView.magCardMagExpDateErr(mode);
                runSearchCardThread();
                return;
            }
            if (TextUtils.isEmpty(pan)) {
                proxyView.finish(new ActionResult(TransResult.ERR_CARD_NO, null));
                return;
            }
            proxyView.magCardReadOk(pan);
        }
    }

    // 填写信息校验
    private void processMag() {
        byte resultType = event.type;
        String content = proxyView.getCardNo().replace(" ", "");
        if (content.isEmpty()) {
            FinancialApplication.getApp().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    proxyView.onReadCardError();
                }
            });
            runSearchCardThread();
            return;
        }

        String pan = TrackUtils.getPan(event.trackData2);
        Issuer matchedIssuer = FinancialApplication.getAcqManager().findIssuerByPan(pan);
        if (resultType == SearchMode.SWIPE) {
            if (matchedIssuer == null) {
                proxyView.finish(new ActionResult(TransResult.ERR_CARD_UNSUPPORTED, null));
                return;
            }

            if (!Issuer.validPan(matchedIssuer, pan)) {
                proxyView.finish(new ActionResult(TransResult.ERR_CARD_INVALID, null));
                return;
            }

            if (!Issuer.validCardExpiry(matchedIssuer, TrackUtils.getExpDate(event.trackData2))) {
                proxyView.finish(new ActionResult(TransResult.ERR_CARD_EXPIRED, null));
                return;
            }
        }
        ActionSearchCard.CardInformation cardInfo = new ActionSearchCard.CardInformation(SearchMode.SWIPE, event.trackData1, event.trackData2,
                event.trackData3, pan, matchedIssuer, event.isThisFallback);
        proxyView.finish(new ActionResult(TransResult.SUCC, cardInfo));

    }
}
