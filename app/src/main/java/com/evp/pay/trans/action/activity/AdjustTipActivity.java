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
package com.evp.pay.trans.action.activity;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.eventbus.EmvCallbackEvent;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.EnterAmountTextWatcher;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.view.keyboard.CustomKeyboardEditText;
import com.pax.dal.entity.EReaderType;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

/**
 * The type Adjust tip activity.
 */
public class AdjustTipActivity extends BaseActivityWithTickForAction {

    private TextView textTipAmount;//tip amount text
    private CustomKeyboardEditText editAmount;//total amount edit text

    private String title;
    private String amount;
    private float percent;
    private String cardMode;

    private boolean isFirstStart = true;// whether the first time or not

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFirstStart) {
            editAmount.requestFocus();
            isFirstStart = false;
        }
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        amount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        percent = getIntent().getFloatExtra(EUIParamKeys.TIP_PERCENT.toString(), 0.0f);
        cardMode = getIntent().getStringExtra(EUIParamKeys.CARD_MODE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_enter_amount;
    }

    @Override
    protected String getTitleString() {
        return title;
    }

    @Override
    protected void initViews() {

        TextView textBaseAmount = (TextView) findViewById(R.id.base_amount_input_text);
        textBaseAmount.setVisibility(View.VISIBLE);
        textBaseAmount.setText(CurrencyConverter.convert(Utils.parseLongSafe(amount, 0)));

        TextView promptTip = (TextView) findViewById(R.id.prompt_tip);
        promptTip.setText(getString(R.string.prompt_tip) + "(max:" + percent + "%)");

        LinearLayout tipAmountLL = (LinearLayout) findViewById(R.id.tip_amount_ll);
        tipAmountLL.setVisibility(View.VISIBLE);
        textTipAmount = (TextView) findViewById(R.id.tip_amount_input_text);
        textTipAmount.setText(CurrencyConverter.convert(0L));

        editAmount = (CustomKeyboardEditText) findViewById(R.id.amount_edit);
        editAmount.setText(CurrencyConverter.convert(Utils.parseLongSafe(amount, 0)));
        editAmount.requestFocus();

    }

    @Override
    protected void setListeners() {
        EnterAmountTextWatcher amountWatcher = new EnterAmountTextWatcher();
        amountWatcher.setAmount(Utils.parseLongSafe(amount, 0), 0L);


        amountWatcher.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            BigDecimal hundredBd = new BigDecimal(100);
            BigDecimal percentBd = BigDecimal.valueOf(percent);

            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                textTipAmount.setText(CurrencyConverter.convert(tipAmount));
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                // AET-313
                BigDecimal baseAmountBd = new BigDecimal(baseAmount);
                BigDecimal maxTipsBd = baseAmountBd.divide(hundredBd).multiply(percentBd).divide(hundredBd);
                BigDecimal tipAmountBd = (new BigDecimal(tipAmount)).divide(hundredBd);
                return maxTipsBd.doubleValue() >= tipAmountBd.doubleValue(); //AET-33
            }
        });
        editAmount.addTextChangedListener(amountWatcher);
        editAmount.setOnEditorActionListener(new AdjustTipEditorActionListener(this));
    }

    //AET-281
    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.CARD_NUM_CONFIRM_ERROR));
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }

    private static class AdjustTipEditorActionListener extends EditorActionListener {

        private WeakReference<AdjustTipActivity> weakReference;

        /**
         * Instantiates a new Adjust tip editor action listener.
         *
         * @param activity the activity
         */
        public AdjustTipEditorActionListener(AdjustTipActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onKeyCancel() {
            AdjustTipActivity activity = weakReference.get();
            activity.textTipAmount.setText("");
            activity.editAmount.setText("");
            if ((activity.cardMode != null) && (activity.cardMode.equals(EReaderType.ICC.toString()))) {
                FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.CARD_NUM_CONFIRM_ERROR));
            } else {
                activity.finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            }
        }

        @Override
        public void onKeyOk() {
            AdjustTipActivity activity = weakReference.get();
            if ((activity.cardMode != null) && (activity.cardMode.equals(EReaderType.ICC.toString()))) {
                Intent intent = activity.getIntent();
                intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), activity.editAmount.getText().toString().trim());
                intent.putExtra(EUIParamKeys.TIP_AMOUNT.toString(), activity.textTipAmount.getText().toString().trim());
                activity.setResult(SearchCardActivity.REQ_ADJUST_TIP, intent);
                activity.finish();
            } else {
                activity.finish(new ActionResult(TransResult.SUCC, activity.editAmount.getText().toString().trim(), activity.textTipAmount.getText().toString().trim()));
            }
        }
    }

    @Override
    protected void onTimerFinish() {
        //Terminate the EMV thread if ICC/PICC
        if((cardMode!=null)&& (cardMode.equals(EReaderType.ICC.toString())
                ||cardMode.equals(EReaderType.PICC.toString()))){
            FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.TIMEOUT));
            return;
        }
        //if carmode is MAG or KEYIN(enter card num manually)
        super.onTimerFinish();
    }
}
