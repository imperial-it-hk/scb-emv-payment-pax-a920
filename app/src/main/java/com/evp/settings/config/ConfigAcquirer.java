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

import android.content.res.Configuration;
import android.text.InputType;

import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.ConfigInputInflater;
import com.evp.settings.inflater.ConfigSelectInflater;

/**
 * The type Config acquirer.
 *
 * @author ligq
 * @date 2019 /4/18 14:35
 */
public class ConfigAcquirer implements IConfig<ConfigInflater<ConfigThirdActivity>> {
    private String acquirerName;

    /**
     * Instantiates a new Config acquirer.
     *
     * @param acquirerName the acquirer name
     */
    public ConfigAcquirer(String acquirerName) {
        this.acquirerName = acquirerName;
    }

    @Override
    public ConfigInflater<ConfigThirdActivity> getInflater(String title) {
        if (Utils.getString(R.string.settings_menu_acquirer_parameter).equals(title)) {
            return new ConfigSelectInflater(acquirerName);
        } else if (Utils.getString(R.string.acq_terminal_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 8, 0, false);
        } else if (Utils.getString(R.string.acq_merchant_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 15, 0, false);
        } else if (Utils.getString(R.string.acq_nii).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 3, 0, false);
        } else if (Utils.getString(R.string.acq_batch_no).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 6, 0, false);
        } else if (Utils.getString(R.string.acq_ip).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, Configuration.KEYBOARD_QWERTY, 15, R.string.digits_4, true);
        } else if (Utils.getString(R.string.acq_port).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 6, 0, false);
        } else if (Utils.getString(R.string.SSL).equals(title)) {
            return new ConfigSelectInflater(acquirerName);
        } else if (Utils.getString(R.string.acq_tle_version).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 2, 0, false);
        } else if (Utils.getString(R.string.acq_tle_nii).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 3, 0, false);
        } else if (Utils.getString(R.string.acq_tle_kms_nii).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 3, 0, false);
        } else if (Utils.getString(R.string.acq_tle_vendor_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 8, 0, false);
        } else if (Utils.getString(R.string.acq_tle_key_set_id).equals(title)) {
            return new ConfigSelectInflater(acquirerName);
        }  else if (Utils.getString(R.string.acq_tle_te_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 8, 0, false);
        }  else if (Utils.getString(R.string.acq_tle_te_pin).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 8, 0, false);
        } else if (Utils.getString(R.string.acq_tle_acqid).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 3, 0, false);
        } else if (Utils.getString(R.string.acq_tle_sensitive_fields).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_TEXT, 20, 0, false);
        } else if (Utils.getString(R.string.alipay_terminal_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 8, 0, false);
        } else if (Utils.getString(R.string.alipay_merchant_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.alipay_acquirer).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.wechat_terminal_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 8, 0, false);
        } else if (Utils.getString(R.string.wechat_merchant_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.wechat_acquirer).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.tag30_terminal_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 8, 0, false);
        } else if (Utils.getString(R.string.tag30_merchant_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.tag30_biller_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.tag30_merchant_name).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.tag30_partner_code).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.qrcs_terminal_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 8, 0, false);
        } else if (Utils.getString(R.string.qrcs_merchant_id).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.qrcs_partner_code).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, -1, 20, 0, false);
        } else if (Utils.getString(R.string.inquiry_timeout).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 20, 0, false);
        } else if (Utils.getString(R.string.inquiry_retries).equals(title)) {
            return new ConfigInputInflater(SettingConst.TYPE_ACQUIRER, acquirerName, InputType.TYPE_CLASS_NUMBER, 20, 0, false);
        } else {
            return null;
        }
    }

    @Override
    public void doOkNext(Object... args) {

    }
}
