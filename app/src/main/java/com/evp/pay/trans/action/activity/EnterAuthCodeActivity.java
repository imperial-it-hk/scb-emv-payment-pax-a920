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

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

/**
 * The type Enter auth code activity.
 */
public class EnterAuthCodeActivity extends BaseActivityWithTickForAction {

    private EditText authCodeTv;

    private String title;
    private String prompt1;
    private String amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        authCodeTv.setFocusable(true);
        authCodeTv.setFocusableInTouchMode(true);
        authCodeTv.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        amount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_enter_auth_code;
    }

    @Override
    protected String getTitleString() {
        return title;
    }

    @Override
    protected void initViews() {
        TextView amountTv = (TextView) findViewById(R.id.amount_txt);
        LinearLayout amountLayout = (LinearLayout) findViewById(R.id.trans_amount_layout);
        if (amount != null && !amount.isEmpty()) {
            amount = CurrencyConverter.convert(Utils.parseLongSafe(amount, 0));
            amountTv.setText(amount);
        } else {
            amountLayout.setVisibility(View.INVISIBLE);
        }

        TextView promptTv1 = (TextView) findViewById(R.id.prompt_title);
        promptTv1.setText(prompt1);

        authCodeTv = (EditText) findViewById(R.id.auth_code_input_text);
    }

    @Override
    protected void setListeners() {
        authCodeTv.setOnEditorActionListener(new EditorActionListener() {
            @Override
            protected void onKeyOk() {
                if (!authCodeTv.getText().toString().isEmpty()) {
                    finish(new ActionResult(TransResult.SUCC, authCodeTv.getText().toString()));
                }
            }

            @Override
            protected void onKeyCancel() {
                authCodeTv.setText("");
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            }
        });
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
