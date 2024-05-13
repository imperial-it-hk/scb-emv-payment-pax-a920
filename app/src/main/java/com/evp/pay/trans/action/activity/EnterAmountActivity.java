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

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.EnterAmountTextWatcher;
import com.evp.payment.evpscb.R;
import com.evp.view.keyboard.CustomKeyboardEditText;

/**
 * The type Enter amount activity.
 */
public class EnterAmountActivity extends BaseActivityWithTickForAction {

    private LinearLayout tipAmountLL;
    private LinearLayout baseAmountLL;
    private TextView textBaseAmount;//base amount text
    private TextView promptTip;
    private TextView textTipAmount;//tip amount text
    private CustomKeyboardEditText editAmount;//total amount edit text

    private String title;
    private boolean hasTip;
    private float percent;

    private boolean isTipMode = false;

    private EnterAmountTextWatcher amountWatcher = null;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        editAmount.requestFocus();
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        hasTip = getIntent().getBooleanExtra(EUIParamKeys.HAS_TIP.toString(), false);
        if (hasTip) {
            percent = getIntent().getFloatExtra(EUIParamKeys.TIP_PERCENT.toString(), 0.0f);
        }
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
        textBaseAmount = (TextView) findViewById(R.id.base_amount_input_text);
        baseAmountLL = (LinearLayout) findViewById(R.id.base_amount_ll);
        tipAmountLL = (LinearLayout) findViewById(R.id.tip_amount_ll);
        promptTip = (TextView) findViewById(R.id.prompt_tip);
        textTipAmount = (TextView) findViewById(R.id.tip_amount_input_text);
        LinearLayout amountLayout = (LinearLayout) findViewById(R.id.amount_layout);

        editAmount = (CustomKeyboardEditText) findViewById(R.id.amount_edit);
        editAmount.setText(CurrencyConverter.convert(0L)); //AET-64
        editAmount.requestFocus();
        if (primaryColor != -1) {
            amountLayout.setBackground(new ColorDrawable(primaryColor));
        }
        if (!isTipMode) {
            baseAmountLL.setVisibility(View.INVISIBLE);
            tipAmountLL.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setListeners() {

        amountWatcher = new EnterAmountTextWatcher();
        amountWatcher.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                textTipAmount.setText(CurrencyConverter.convert(tipAmount));
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                return !isTipMode || (baseAmount * percent / 100 >= tipAmount); //AET-33
            }
        });
        editAmount.addTextChangedListener(amountWatcher);

        editAmount.setOnEditorActionListener(new EnterAmountEditorActionListener());
    }


    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    private class EnterAmountEditorActionListener extends EditorActionListener {
        @Override
        public void onKeyCancel() {
            updateAmount(false);
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null)); // AET-64
        }

        @Override
        public void onKeyOk() {
            if (!isTipMode) {
                if ("0".equals(CurrencyConverter.parse(editAmount.getText().toString().trim()).toString())) {
                    return;
                }
                if (hasTip) {
                    updateAmount(true);
                    return;
                }
            }
            finish(new ActionResult(TransResult.SUCC,
                    CurrencyConverter.parse(editAmount.getText().toString().trim()),
                    CurrencyConverter.parse(textTipAmount.getText().toString().trim()))
            );
        }

        //update total amount
        private synchronized void updateAmount(boolean isTipMode) {
            EnterAmountActivity.this.isTipMode = isTipMode;
            editAmount.requestFocus();
            if (isTipMode) {
                textBaseAmount.setVisibility(View.VISIBLE);
                tipAmountLL.setVisibility(View.VISIBLE);
                textBaseAmount.setText(editAmount.getText());
                promptTip.setText(getString(R.string.prompt_tip) + "(max:" + percent + "%)");
                textTipAmount.setText("");
                if (amountWatcher != null)
                    amountWatcher.setAmount(CurrencyConverter.parse(editAmount.getText().toString().trim()), 0L);
            } else {
                baseAmountLL.setVisibility(View.INVISIBLE);
                textBaseAmount.setVisibility(View.INVISIBLE);
                tipAmountLL.setVisibility(View.GONE);
                textBaseAmount.setText("");
                textTipAmount.setText("");
                if (amountWatcher != null)
                    amountWatcher.setAmount(0L, 0L);
                editAmount.setText("");
            }
        }
    }
}
