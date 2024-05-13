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
package com.evp.pay.trans.action.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.params.ParamHelper;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.DeviceUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eventbus.CardDetectEvent;
import com.evp.eventbus.EmvCallbackEvent;
import com.evp.eventbus.NoticeSwipe;
import com.evp.eventbus.SearchCardEvent;
import com.evp.mvp.contract.SearchCardContract;
import com.evp.mvp.presenter.SearchCardPresenter;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.emv.EmvListenerImpl;
import com.evp.pay.trans.TransContext;
import com.evp.pay.trans.action.ActionEmvProcess;
import com.evp.pay.trans.action.ActionSearchCard;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.TickTimer;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;
import com.evp.view.ClssLight;
import com.evp.view.ClssLightsView;
import com.evp.view.dialog.DialogUtils;
import com.pax.dal.IPed;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.exceptions.PedDevException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * The type Search card activity.
 */
public class SearchCardActivity extends BaseActivityWithTickForAction implements SearchCardContract.View {
    private SearchCardPresenter presenter;
    /**
     * The constant REQ_ADJUST_TIP.
     */
    public static final int REQ_ADJUST_TIP = 0;

    private ClssLightsView llClssLight;

    private TextView tvPrompt; // 输入方式提示

    private EditText edtCardNo; // 输入框
    private EditText expDate; //卡有效期
    private TextView holderName; //显示持卡人姓名

    private Button btnConfirm; // 确认按钮

    private ImageView ivSwipe; // 刷卡图标
    private ImageView ivInsert; // 插卡图标
    private ImageView ivTap; // 非接图标
    private ImageButton ivQr; // 非接图标

    private LinearLayout llSupportedCard;

    private String navTitle;
    private String amount; // 交易金额

    private String searchCardPrompt; // 寻卡提示

    private boolean supportManual = false; // 是否支持手输
    private boolean supportQR = false;
    // 寻卡成功时，此界面还保留， 在后续界面切换时，还有机会跑到前台，此时按返回键，此activity finish，同时会有两个分支同时进行
    // 如果寻卡成功时， 此标志为true
    private boolean isSuccLeave = false;

    /**
     * 支持的寻卡类型{@link SearchMode}
     */
    private byte mode; // 寻卡模式

    private boolean isTimeOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new SearchCardPresenter(this);
        presenter.attachView(this);
        super.onCreate(savedInstanceState);
        FinancialApplication.getApp().register(this);
        presenter.runSearchCard();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bankcard_pay;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();

        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        // 显示金额
        try {
            amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
            if (amount != null && !amount.isEmpty()) {
                amount = CurrencyConverter.convert(Utils.parseLongSafe(amount, 0));
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
            amount = null;
        }

        // 寻卡方式
        try {
            mode = bundle.getByte(EUIParamKeys.CARD_SEARCH_MODE.toString(), SearchMode.SWIPE);
            // 是否支持手输卡号
            supportManual = (mode & SearchMode.KEYIN) == SearchMode.KEYIN;
            supportQR = (mode & SearchMode.QR) == SearchMode.QR;
            presenter.initParam(mode);

        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
        }

        // 获取寻卡提醒
        searchCardPrompt = getString(R.string.prompt_insert_swipe_wave_card);
        if ((mode & SearchMode.WAVE) == 0) {
            searchCardPrompt = getString(R.string.prompt_insert_swipe_card);
        }
        try {
            String prompt = bundle.getString(EUIParamKeys.SEARCH_CARD_PROMPT.toString());
            if (prompt != null && !prompt.isEmpty()) {
                searchCardPrompt = prompt;
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
        }
    }

    @Override
    protected void initViews() {
        if (!ParamHelper.isClssInternal() && ParamHelper.isExternalTypeAPed()){
            IPed ped = PedHelper.getPed();
            try {
                ped.clearScreen();
                if (!TextUtils.isEmpty(amount)){
                    ped.showStr((byte) 0x60, (byte) 0x01,"Amount");
                    ped.showStr((byte) 0x60, (byte) 0x03,amount);
                    ped.showStr((byte) 0x60, (byte) 0x05,"PLS TAP Card");
                }
            } catch (PedDevException e) {
                LogUtils.e(e);
            }
        }
        llClssLight = (ClssLightsView) findViewById(R.id.clssLight);
        llClssLight.setVisibility(View.GONE);

        if (SearchMode.isWave(mode)) {
            llClssLight.setVisibility(View.VISIBLE);
            llClssLight.setLights(0, ClssLight.BLINK);
        }

        LinearLayout llAmount = (LinearLayout) findViewById(R.id.amount_layout);
        if (amount == null || amount.isEmpty()) { // 余额查询不显示金额
            llAmount.setVisibility(View.INVISIBLE);
        } else {
            TextView tvAmount = (TextView) findViewById(R.id.amount_txt); // 只显示交易金额
            tvAmount.setText(amount);
        }

        edtCardNo = (EditText) findViewById(R.id.bank_card_number);// 初始为卡号输入框
        ViewUtils.configInput(edtCardNo);
        expDate = (EditText) findViewById(R.id.bank_card_expdate);//卡有效期输入框
        ViewUtils.configInput(expDate);
        holderName = (TextView) findViewById(R.id.bank_card_holder_name);
        holderName.setVisibility(View.GONE);

        btnConfirm = (Button) findViewById(R.id.ok_btn);
        btnConfirm.setEnabled(false);

        tvPrompt = (TextView) findViewById(R.id.tv_prompt_readcard);

        ivSwipe = (ImageView) findViewById(R.id.iv_swipe);
        ivInsert = (ImageView) findViewById(R.id.iv_insert);
        ivTap = (ImageView) findViewById(R.id.iv_tap);
        ivQr = (ImageButton) findViewById(R.id.qr_scanner);

        if (supportManual) {
            edtCardNo.setHint(R.string.prompt_card_num_manual);
            edtCardNo.setHintTextColor(getResources().getColor(R.color.secondary_text_light));
            edtCardNo.setClickable(true);// 支持手输卡号
            edtCardNo.addTextChangedListener(new CardNoWatcher());
            edtCardNo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19 + 4)});// 4为卡号分隔符个数
            edtCardNo.setCursorVisible(true);

            expDate.setVisibility(View.VISIBLE);
            expDate.setClickable(true);// 支持手输卡号
            expDate.addTextChangedListener(new ExpDateWatcher());
            expDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4 + 1)});// 4为卡号分隔符个数
            expDate.setCursorVisible(true);

            edtCardNo.setOnEditorActionListener(new EditorActionListener() {
                @Override
                protected void onKeyOk() {
                    presenter.processManualCardNo();
                }

                @Override
                protected void onKeyCancel() {
                    //do nothing
                }
            });
            expDate.setOnEditorActionListener(new EditorActionListener() {
                @Override
                protected void onKeyOk() {
                    presenter.processManualCardNo();
                }

                @Override
                protected void onKeyCancel() {
                    //do nothing
                }
            });
        } else {

            edtCardNo.setEnabled(false);// 不支持手输入卡号
            expDate.setVisibility(View.GONE);//temporary change
            expDate.setEnabled(false);// 不支持手输入卡号
        }
        if (supportQR) {
            ivQr.setOnClickListener(this);
            ivQr.setFocusable(false);
        } else {
            ivQr.setVisibility(View.GONE);
        }
        btnConfirm.setVisibility(View.INVISIBLE);
        llSupportedCard = (LinearLayout) findViewById(R.id.supported_card_prompt);

        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                (mode & SearchMode.INSERT) == SearchMode.INSERT, SearchMode.isWave(mode));
        tvPrompt.setText(searchCardPrompt);
    }

    @Override
    protected void setListeners() {
        btnConfirm.setOnClickListener(this);
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void onTimerFinish() {
        isTimeOut = true;
        AAction currentAction = TransContext.getInstance().getCurrentAction();
        if (currentAction instanceof ActionEmvProcess){
            FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.TIMEOUT));
            return;//when received EmvCallbackEvent,it will end the transaction,must not call super.onTimerFinish();
        }else{
            presenter.onTimerFinish();
        }
        super.onTimerFinish();
    }

    @Override
    public void onEditCardNoError() {
        ToastUtils.showMessage(R.string.prompt_card_num_err);
        edtCardNo.setText("");
        edtCardNo.requestFocus();
    }

    @Override
    public void onEditCardNo() {
        llClssLight.setLights(-1, ClssLight.OFF);
        quickClickProtection.stop();
    }

    @Override
    public void showManualPwdErr() {
        DialogUtils.showErrMessage(SearchCardActivity.this, getString(R.string.trans_password),
                getString(R.string.err_password), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        FinancialApplication.getApp().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //AET-158
                                if (!isTimeOut) {
                                    presenter.runInputMerchantPwdAction();
                                }
                            }
                        });
                    }
                }, Constants.FAILED_DIALOG_SHOW_TIME);
    }

    @Override
    public void onEditDateError() {
        ToastUtils.showMessage(R.string.prompt_card_date_err);
        expDate.setText("");
        expDate.requestFocus();
    }

    @Override
    public String getExpDate() {
        return expDate.getText().toString();
    }

    @Override
    public String getCardNo() {
        return edtCardNo.getText().toString();
    }


    @Override
    public void iccCardMagReadOk(Byte mode) {
        ToastUtils.showMessage(R.string.prompt_ic_card_input);
        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                (mode & SearchMode.INSERT) == SearchMode.INSERT,
                SearchMode.isWave(mode));
        tvPrompt.setText(searchCardPrompt);
    }

    @Override
    public void magCardMagExpDateErr(Byte mode) {
        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                (mode & SearchMode.INSERT) == SearchMode.INSERT,
                SearchMode.isWave(mode));
        tvPrompt.setText(searchCardPrompt);
    }

    @Override
    public void magCardReadOk(String pan) {
        edtCardNo.setEnabled(false);
        edtCardNo.setText(PanUtils.maskCardNo(pan));

        llSupportedCard.setVisibility(View.INVISIBLE);
        ivQr.setEnabled(false);
        ivQr.setVisibility(View.GONE);
        llClssLight.setLights(-1, ClssLight.OFF);
        //AET-136
        tickTimer.start();
        Utils.wakeupScreen(TickTimer.DEFAULT_TIMEOUT);
        confirmBtnChange();
    }

    @Override
    public void onIccDetectOk() {
        disableNonCardView();
        llClssLight.setLights(-1, ClssLight.OFF);
        finish(new ActionResult(TransResult.SUCC, new ActionSearchCard.CardInformation(SearchMode.INSERT)));
    }

    @Override
    public void onPiccDetectOk() {
        LogUtils.i("CTLS_SPEED_TEST", "CTLS CARD DETECTED");
        disableNonCardView();
        llClssLight.setVisibility(View.VISIBLE);
        finish(new ActionResult(TransResult.SUCC, new ActionSearchCard.CardInformation(SearchMode.WAVE)));
    }

    @Override
    public void onReadCardError() {
        ToastUtils.showMessage(R.string.prompt_please_retry);
    }

    @Override
    public void goToAdjustTip() {
        long baseAmountLong = CurrencyConverter.parse(amount);
        Intent intent = new Intent(SearchCardActivity.this, AdjustTipActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), navTitle);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), String.valueOf(baseAmountLong));
        intent.putExtra(EUIParamKeys.TIP_PERCENT.toString(), presenter.iccAdjustPercent);
        intent.putExtra(EUIParamKeys.CARD_MODE.toString(), EReaderType.ICC.toString());
        startActivityForResult(intent, REQ_ADJUST_TIP);
    }

    @Override
    public void resetEditText() {
        edtCardNo.setText("");
        edtCardNo.clearFocus();
    }

    @Override
    public void onClickBack() {
        expDate.setText("");
        expDate.setVisibility(View.GONE);
        expDate.clearFocus();
        //AET-155
        llClssLight.setLights(0, ClssLight.BLINK);
        tickTimer.stop();
        tickTimer.start();
    }

    private void confirmBtnChange() {
        String content = edtCardNo.getText().toString();
        tvPrompt.setText(R.string.prompt_confirm_card_info);
        if (!content.isEmpty()) {
            expDate.setVisibility(View.GONE);
            btnConfirm.setEnabled(true);
            btnConfirm.setVisibility(View.VISIBLE);
            btnConfirm.setText(R.string.btn_confirm);
            btnConfirm.requestFocus();
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setVisibility(View.INVISIBLE);
            btnConfirm.clearFocus();
        }
    }

    private void disableNonCardView() {
        llSupportedCard.setVisibility(View.INVISIBLE);
        edtCardNo.setEnabled(false);
        expDate.setEnabled(false);
        ivQr.setEnabled(true);
        ivQr.setVisibility(View.GONE);
    }

    /**
     * 设置图标显示
     *
     * @param mag  enable mag
     * @param icc  enable icc
     * @param picc enable picc
     */
    private void setSearchCardImage(boolean mag, boolean icc, boolean picc) {
        ivSwipe.setImageDrawable(mag ? FinancialApplication.getApp().getResources().getDrawable(R.drawable.swipe_card) : FinancialApplication.getApp().getResources().getDrawable(R.drawable.no_swipe_card));
        ivInsert.setImageDrawable(icc ? FinancialApplication.getApp().getResources().getDrawable(R.drawable.insert_card) : FinancialApplication.getApp().getResources().getDrawable(R.drawable.no_insert_card));
        ivTap.setImageDrawable(picc ? FinancialApplication.getApp().getResources().getDrawable(R.drawable.tap_card) : FinancialApplication.getApp().getResources().getDrawable(R.drawable.no_tap_card));
    }

    private void onCardNumConfirm() {
        isSuccLeave = false;
        edtCardNo.setClickable(false);
        //AET-120
        tickTimer.start();
        //AET-135
        Utils.wakeupScreen(TickTimer.DEFAULT_TIMEOUT);
        confirmBtnChange();
    }

    @Override
    protected void onClickProtected(View v) {
        // manual input case: get click event from IME_ACTION_DONE, the button is always hidden.
        if (v.getId() == R.id.ok_btn) {
            onOkClicked();
        }else if (v.getId() == R.id.qr_scanner){
            presenter.isQRMode = true;
            llClssLight.setLights(-1, ClssLight.OFF);
            finish(new ActionResult(TransResult.SUCC, new ActionSearchCard.CardInformation(SearchMode.QR)));
        }
    }

    private void onOkClicked() {
        tickTimer.stop();
        btnConfirm.setEnabled(false);
        presenter.onOkClicked();
    }

    private void onHeaderBackClicked() {
        if (isSuccLeave) {
            return;
        }
        presenter.onHeaderBackClicked();
    }

    private void updateAmount(@NonNull Intent data) {
        amount = data.getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        String tipAmount = data.getStringExtra(EUIParamKeys.TIP_AMOUNT.toString());
        TextView tvAmount = (TextView) findViewById(R.id.amount_txt);
        tvAmount.setText(amount);
        FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.CARD_NUM_CONFIRM_SUCCESS,
                new String[]{amount, tipAmount}));
        isSuccLeave = true; //AET-106
    }

    @Override
    protected boolean onKeyBackDown() {
        presenter.onKeyBackDown();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return isSuccLeave || super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish(ActionResult result) {
        presenter.stopDetectCard();
        if (result.getRet() == TransResult.SUCC) {
            isSuccLeave = true;
        }
        super.finish(result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADJUST_TIP && data != null) { //AET-82
            updateAmount(data);
        }
    }

    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onHeaderBackClicked();
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        boolean interactive = DeviceUtils.isScreenOn(getApplicationContext());
        LogUtils.d("CardReaderHelper","SearchCardActivity onStop interactive = " + interactive);
        if (interactive){
            presenter.detachView();
            FinancialApplication.getApp().unregister(this);
            presenter.stopDetectCard();
            llClssLight.setLights(-1, ClssLight.OFF);
        }
    }

    /**
     * handle Card Detected event
     * @param event Card Detected event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveCardDetect(CardDetectEvent event) {
        presenter.onReadCardOk(event);
    }

    /**
     * On search card event.
     *
     * @param event the event
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchCardEvent(SearchCardEvent event) {
        switch ((SearchCardEvent.Status) event.getStatus()) {
            case ICC_UPDATE_CARD_INFO:
                onUpdateCardInfo((EmvListenerImpl.CardInfo) event.getData());
                break;
            case ICC_CONFIRM_CARD_NUM:
                onCardNumConfirm();
                break;
            case CLSS_LIGHT_STATUS_NOT_READY:
                llClssLight.setLights(-1, ClssLight.OFF);
                break;
            case CLSS_LIGHT_STATUS_IDLE:
            case CLSS_LIGHT_STATUS_READY_FOR_TXN:
                llClssLight.setLights(0, ClssLight.BLINK);
                break;
            case CLSS_LIGHT_STATUS_PROCESSING:
                llClssLight.setLights(1, ClssLight.ON);
                break;
            case CLSS_LIGHT_STATUS_REMOVE_CARD:
                llClssLight.setLights(2, ClssLight.ON);
                break;
            case CLSS_LIGHT_STATUS_COMPLETE:
                llClssLight.setLights(2, ClssLight.BLINK);
                break;
            case CLSS_LIGHT_STATUS_ERROR:
                llClssLight.setLights(3, ClssLight.BLINK);
                break;
            default:
                break;
        }
    }

    /**
     * handle event of below conditions
     * 1.Detect Card Module Disabled by PAXSTORE
     * 2.show error toast
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNoticeSwipeCard(NoticeSwipe event) {
        switch (event.noticeMsg) {
            case "PICC_Disabled":
                refreshReadType(SearchMode.INTERNAL_WAVE);
                refreshReadType(SearchMode.EXTERNAL_WAVE);
                break;
            case "ICC_Disabled":
                refreshReadType(SearchMode.INSERT);
                break;
            case "MAG_Disabled":
                refreshReadType(SearchMode.SWIPE);
                break;
            default:
                ToastUtils.showMessage(event.noticeMsg);
                break;
        }

    }

    private void refreshReadType(Byte exceptReadType) {
        if ((mode & exceptReadType) == exceptReadType) {
            mode = (byte) (mode ^ exceptReadType);
            LogUtils.d(TAG, "mode:"+mode);
            presenter.initParam(mode);
            setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                    (mode & SearchMode.INSERT) == SearchMode.INSERT,
                    SearchMode.isWave(mode));
        }
    }

    private void onUpdateCardInfo(EmvListenerImpl.CardInfo cardInfo) {
        edtCardNo.setText(PanUtils.maskCardNo(cardInfo.getCardNum()));
        presenter.iccAdjustPercent = cardInfo.getAdjustPercent();
    }

    // 卡号分割及输入长度检查
    private class CardNoWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0)
                return;

            if (before < count) {
                String card = s.toString().replace(" ", "");
                card = card.replaceAll("(\\d{4}(?!$))", "$1 ");
                if (!card.equals(s.toString())) {
                    edtCardNo.setText(card);
                    edtCardNo.setSelection(card.length());
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            //do nothing
        }
    }

    private class ExpDateWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0)
                return;
            String exp = s.toString().replace("/", "");
            exp = exp.replaceAll("(\\d{2}(?!$))", "$1/");
            if (!exp.equals(s.toString())) {
                expDate.setText(exp);
                expDate.setSelection(exp.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            //do nothing
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isSuccLeave = false;
        boolean hasExtra = intent.hasExtra(EUIParamKeys.CARD_SEARCH_MODE.toString());
        if (hasExtra){
            mode = intent.getByteExtra(EUIParamKeys.CARD_SEARCH_MODE.toString(), SearchMode.SWIPE);
        }
        llSupportedCard.setVisibility(View.VISIBLE);
        boolean isWave = SearchMode.isWave(mode);
        if (isWave){
            llClssLight.setVisibility(View.VISIBLE);
        }else {
            llClssLight.setVisibility(View.GONE);
        }
        setSearchCardImage((mode & SearchMode.SWIPE) == SearchMode.SWIPE,
                (mode & SearchMode.INSERT) == SearchMode.INSERT, SearchMode.isWave(mode));
        presenter.initParam(mode);
        presenter.runSearchCard();
    }
}
