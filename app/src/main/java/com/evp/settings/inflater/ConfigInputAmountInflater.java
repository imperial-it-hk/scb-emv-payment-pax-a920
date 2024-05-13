/*
 *
 *  ============================================================================
 *  PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *  This software is supplied under the terms of a license agreement or nondisclosure
 *  agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *  disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2019 -? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *  Description:
 *  Revision History:
 *  Date	             Author	                Action
 *  20190418   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.inflater;

import android.view.ViewStub;

import com.evp.bizlib.data.entity.Issuer;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.EnterAmountTextWatcher;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigSecondActivity;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.view.keyboard.CustomKeyboardEditText;

import org.greenrobot.eventbus.EventBus;

/**
 * The type Config input amount inflater.
 *
 * @author ligq
 * @date 2019 /4/18 16:38
 */
public class ConfigInputAmountInflater implements ConfigInflater<ConfigThirdActivity> {
    private String name;
    private String type;
    private Issuer issuer;
    private CustomKeyboardEditText cak;
    private EnterAmountTextWatcher watcherNonEmvCvmLimit;

    /**
     * Instantiates a new Config input amount inflater.
     *
     * @param name the name
     * @param type the type
     */
    public ConfigInputAmountInflater(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void inflate(ConfigThirdActivity act, String title) {
        ViewStub viewStub = act.findViewById(R.id.vs_third_content);
        viewStub.setLayoutResource(R.layout.layout_config_input);
        viewStub.inflate();
        ViewStub vsInput = act.findViewById(R.id.vs_config_input);
        vsInput.setLayoutResource(R.layout.layout_config_input_amount);
        vsInput.inflate();
        cak = act.findViewById(R.id.cke_config_input_content);
        cak.requestFocus();
        initView();
        if (SettingConst.TYPE_ISSUER.equals(type)) {
            issuer = FinancialApplication.getAcqManager().findIssuer(name);
            watcherNonEmvCvmLimit.setAmount(0, issuer.getNonEmvTranFloorLimit());
            cak.setText(CurrencyConverter.convert(issuer.getNonEmvTranFloorLimit()));
        }
    }

    private void initView() {
        watcherNonEmvCvmLimit = new EnterAmountTextWatcher();
        watcherNonEmvCvmLimit.setMaxValue(99999999L);
        watcherNonEmvCvmLimit.setOnTipListener(new EnterAmountTextWatcher.OnTipListener() {
            @Override
            public void onUpdateTipListener(long baseAmount, long tipAmount) {
                issuer.setNonEmvTranFloorLimit(tipAmount);
            }

            @Override
            public boolean onVerifyTipListener(long baseAmount, long tipAmount) {
                return true;
            }
        });
        cak.addTextChangedListener(watcherNonEmvCvmLimit);
    }

    @Override
    public boolean doNextSuccess() {
        if (SettingConst.TYPE_ISSUER.equals(type)) {
            issuer.setNonEmvTranFloorLimit(CurrencyConverter.parse(cak.getText().toString()));
            FinancialApplication.getAcqManager().updateIssuer(issuer);
            EventBus.getDefault().post(new ConfigSecondActivity.ConfigEvent(type, issuer.getName(), null));
        }
        return true;
    }
}
