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
 * 20190108  	         qixw                    Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.EnterAmountTextWatcher;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.view.keyboard.CustomKeyboardEditText;

/**
 * The type Input trans data tip activity.
 */
public class InputTransDataTipActivity extends BaseActivityWithTickForAction {

    private TextView mOriTips;
    private TextView mTotalAmount;
    private CustomKeyboardEditText mEditNewTips;

    private Button confirmBtn;

    private String navTitle;
    private int tickTime;

    private long totalAmountLong = 0L;
    private long tipAmountLong = 0L;
    private long baseAmountLong = 0L;
    private float adjustPercent = 0L;

    private long orgTipAmountLong = 0L;//original tip amount


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_new_tip;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());

        String oriTransAmount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        String oriTips = getIntent().getStringExtra(EUIParamKeys.ORI_TIPS.toString());
        totalAmountLong = Utils.parseLongSafe(oriTransAmount, 0);
        tipAmountLong = Utils.parseLongSafe(oriTips, 0);
        orgTipAmountLong = tipAmountLong;
        baseAmountLong = totalAmountLong - tipAmountLong;
        adjustPercent = getIntent().getFloatExtra(EUIParamKeys.TIP_PERCENT.toString(), 0.0f);
        tickTime = getIntent().getIntExtra(EUIParamKeys.TIKE_TIME.toString(), 0);
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        TextView mBaseAmount = (TextView) findViewById(R.id.value_base_amount);
        mOriTips = (TextView) findViewById(R.id.value_ori_tips);
        mTotalAmount = (TextView) findViewById(R.id.value_total_amount);

        mBaseAmount.setText(CurrencyConverter.convert(baseAmountLong));
        mOriTips.setText(CurrencyConverter.convert(tipAmountLong));
        mTotalAmount.setText(CurrencyConverter.convert(totalAmountLong));

        mEditNewTips = (CustomKeyboardEditText) findViewById(R.id.prompt_edit_new_tips);
        mEditNewTips.setText(CurrencyConverter.convert(tipAmountLong));
        mEditNewTips.setFocusable(true);
        mEditNewTips.requestFocus();

        confirmBtn = (Button) findViewById(R.id.info_confirm);

        if (tickTime > 0) {
            tickTimer.start(tickTime);
        }
    }

    private void setEditText() {
        mEditNewTips.setHint(getString(R.string.amount_default));
        mEditNewTips.requestFocus();

        confirmBtnChange(); //AET-19

        final EnterAmountTextWatcher amountWatcher = new EnterAmountTextWatcher(0, tipAmountLong);
        amountWatcher.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                tipAmountLong = tipAmount;
                totalAmountLong = baseAmountLong + tipAmountLong;
                confirmBtnChange();
                mTotalAmount.setText(CurrencyConverter.convert(totalAmountLong));
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                //AET-205
                return baseAmountLong * adjustPercent / 100 >= tipAmount && baseAmountLong + tipAmount <= amountWatcher.getMaxValue();
            }
        });
        mEditNewTips.addTextChangedListener(amountWatcher);
    }

    private void confirmBtnChange() {
        boolean enable = !mOriTips.getText().toString().equals(mEditNewTips.getText().toString());
        confirmBtn.setEnabled(enable);
    }

    @Override
    protected void setListeners() {
        confirmBtn.setOnClickListener(this);
        mEditNewTips.setOnEditorActionListener(new TipEditorActionListener());
    }

    private class TipEditorActionListener extends EditorActionListener {
        @Override
        public void onKeyOk() {
            //do nothing
            quickClickProtection.stop();
            onClick(confirmBtn);
        }

        @Override
        public void onKeyCancel() {
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        }
    }

    @Override
    public void onClickProtected(View v) {

        if (v.getId() == R.id.info_confirm) {
            String content = process();
            if (content == null || content.isEmpty()) {
                ToastUtils.showMessage(R.string.please_input_again);
                return;
            }
            if(orgTipAmountLong == tipAmountLong){//tip not change
                ToastUtils.showMessage(R.string.prompt_tip_not_changed);
                return;
            }
            LogUtils.i(TAG, "process: tipAmountLong=" + tipAmountLong);
            finish(new ActionResult(TransResult.SUCC, totalAmountLong, tipAmountLong));
        }

    }

    /**
     * 输入数值检查
     */
    private String process() {
        String content = mEditNewTips.getText().toString().trim();

        if (content.isEmpty()) {
            return null;
        }
        LogUtils.i(TAG, "process: content=" + content);
        //tip can be 0, so don't need to check here
        return CurrencyConverter.parse(content).toString();
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
