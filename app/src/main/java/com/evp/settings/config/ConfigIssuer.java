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

package com.evp.settings.config;

import android.text.InputType;

import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.ConfigInputAmountInflater;
import com.evp.settings.inflater.ConfigInputInflater;
import com.evp.settings.inflater.ConfigSelectInflater;

/**
 * The type Config issuer.
 *
 * @author ligq
 * @date 2019 /4/18 15:16
 */
public class ConfigIssuer implements IConfig<ConfigInflater<ConfigThirdActivity>> {
    private String issuerName;

    /**
     * Instantiates a new Config issuer.
     *
     * @param issuerName the issuer name
     */
    public ConfigIssuer(String issuerName) {
        this.issuerName = issuerName;
    }

    @Override
    public ConfigInflater<ConfigThirdActivity> getInflater(String title) {
        if (Utils.getString(R.string.settings_menu_issuer_parameter).equals(title)) {
            return new ConfigSelectInflater(issuerName);
        } else if (Utils.getString(R.string.issuer_non_emv_tran_floor_limit).equals(title)) {
            return new ConfigInputAmountInflater(issuerName, SettingConst.TYPE_ISSUER);
        } else if (Utils.getString(R.string.issuer_adjust_percent).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ISSUER, issuerName,
                    InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, 5, 0, false);
        } else {
            return null;
        }
    }

    @Override
    public void doOkNext(Object... args) {

    }
}
