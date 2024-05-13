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
package com.evp.pay.trans.action.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.mvp.contract.EnterPinContract;
import com.evp.mvp.presenter.EnterPinPresenter;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;
import com.evp.view.dialog.CustomAlertDialog;
import com.pax.dal.entity.RSAPinKey;
import com.pax.dal.exceptions.PedDevException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Enter pin activity.
 */
public class EnterPinActivity extends BaseActivityWithTickForAction implements EnterPinContract.View {
    private boolean isFirstStart = true;//判断界面是否第一次加载
    private EnterPinPresenter presenter;

    private String title;
    private String panBlock;
    private String prompt2;
    private String prompt1;
    private String totalAmount;
    private String tipAmount;
    private ActionEnterPin.EEnterPinType enterPinType;
    private boolean supportBypass;
    private RSAPinKey rsaPinKey;
    private int pinKeyIndex;

    private boolean landscape;
    private TextView pwdTv;
    private CustomAlertDialog promptDialog;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        presenter = new EnterPinPresenter(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        // 界面不需要超时， 超时有输密码接口控制
        tickTimer.stop();
        presenter.attachView(this);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        quickClickProtection.start();
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFirstStart) {
            presenter.startDetectFingerR(panBlock, supportBypass, enterPinType, pinKeyIndex);
            isFirstStart = false;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_pin;
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt1 = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        prompt2 = getIntent().getStringExtra(EUIParamKeys.PROMPT_2.toString());
        totalAmount = getIntent().getStringExtra(EUIParamKeys.TRANS_AMOUNT.toString());
        tipAmount = getIntent().getStringExtra(EUIParamKeys.TIP_AMOUNT.toString());
        enterPinType = (ActionEnterPin.EEnterPinType) getIntent().getSerializableExtra(EUIParamKeys.ENTERPINTYPE.toString());
        panBlock = getIntent().getStringExtra(EUIParamKeys.PANBLOCK.toString());
        pinKeyIndex = getIntent().getIntExtra(EUIParamKeys.PIN_KEY_INDEX.toString(), KeyUtils.getTpkIndex(Utils.getString(R.string.SET_1)));
        if (enterPinType == ActionEnterPin.EEnterPinType.ONLINE_PIN) {
            supportBypass = getIntent().getBooleanExtra(EUIParamKeys.SUPPORTBYPASS.toString(), false);
        } else {
            rsaPinKey = getIntent().getParcelableExtra(EUIParamKeys.RSA_PIN_KEY.toString());
        }
    }

    @Override
    protected void initViews() {
        landscape = !ViewUtils.isScreenOrientationPortrait(this);
        presenter.initParams(landscape, rsaPinKey,prompt1,prompt2);
        enableActionBar(false);

        TextView totalAmountTv = (TextView) findViewById(R.id.total_amount_txt);
        LinearLayout totalAmountLayout = (LinearLayout) findViewById(R.id.trans_total_amount_layout);
        if (totalAmount != null && !totalAmount.isEmpty()) {
            totalAmount = CurrencyConverter.convert(Utils.parseLongSafe(totalAmount, 0));
            totalAmountTv.setText(totalAmount);
        } else {
            totalAmountLayout.setVisibility(View.INVISIBLE);
        }

        TextView tipAmountTv = (TextView) findViewById(R.id.tip_amount_txt);
        LinearLayout tipAmountLayout = (LinearLayout) findViewById(R.id.trans_tip_amount_layout);
        if (tipAmount != null && !tipAmount.isEmpty()) {
            tipAmount = CurrencyConverter.convert(Utils.parseLongSafe(tipAmount, 0));
            tipAmountTv.setText(tipAmount);
        } else {
            tipAmountLayout.setVisibility(View.INVISIBLE);
        }

        TextView promptTv1 = (TextView) findViewById(R.id.prompt_title);
        promptTv1.setText(prompt1);

        TextView promptTv2 = (TextView) findViewById(R.id.prompt_no_pin);
        if (prompt2 != null) {
            promptTv2.setText(prompt2);
        } else {
            promptTv2.setVisibility(View.INVISIBLE);
        }

        pwdTv = (TextView) findViewById(R.id.pin_input_text);

    }

    @Override
    protected void setListeners() {

    }

    private final void setContentText(final String content) {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (pwdTv != null) {
                    pwdTv.setText(content);
                    pwdTv.setTextSize(FinancialApplication.getApp().getResources().getDimension(R.dimen.font_size_key));
                }
            }
        });
    }

    @Override
    protected String getTitleString() {
        return title;
    }

    @Override
    public void actionFinish(@NotNull ActionResult result) {
        finish(result);
    }

    @Override
    public void showFlingNotice() {
        FinancialApplication.getApp().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showMessage(R.string.no_long_press);
            }
        });
    }

    @Override
    public void showErrorDialog(@NotNull PedDevException e) {
        promptDialog = new CustomAlertDialog(EnterPinActivity.this, CustomAlertDialog.ERROR_TYPE);
        promptDialog.setTimeout(3);
        if (FinancialApplication.getCurrentETransType() != null) {
            promptDialog.setTitleText(FinancialApplication.getCurrentETransType().getTransName());
        }
        promptDialog.setContentText(e.getErrMsg());
        promptDialog.show();
//        promptDialog.showConfirmButton(true);
        promptDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
            }
        });
    }

    @Override
    public String getText() {
        return pwdTv.getText().toString();
    }

    @Override
    public void setText(String temp) {
        setContentText(temp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
