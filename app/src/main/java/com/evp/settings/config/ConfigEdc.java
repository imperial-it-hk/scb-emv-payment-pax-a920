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
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.ConfigInputInflater;
import com.evp.settings.inflater.ConfigSelectInflater;

/**
 * The type Config edc.
 *
 * @author ligq
 * @date 2019 /4/18 15:07
 */
public class ConfigEdc implements IConfig<ConfigInflater<ConfigThirdActivity>> {
    @Override
    public ConfigInflater<ConfigThirdActivity> getInflater(String title) {
        if (Utils.getString(R.string.edc_merchant_name).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_MERCHANT_NAME_EN), 60, 0);
        } else if (Utils.getString(R.string.edc_merchant_address).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_MERCHANT_ADDRESS), Integer.MAX_VALUE, 0);
        } else if (Utils.getString(R.string.currency_list).equals(title) ||
                Utils.getString(R.string.edc_ped_mode).equals(title)||
                Utils.getString(R.string.edc_clss_mode).equals(title)||
                Utils.getString(R.string.edc_printer_type).equals(title)) {
            return new ConfigSelectInflater();
        } else if (Utils.getString(R.string.edc_receipt_no).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_RECEIPT_NUM), 1, R.string.digits_3);
        } else if (Utils.getString(R.string.edc_trace_no).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_TRACE_NO), 6, R.string.digits_2);
        } else if (Utils.getString(R.string.edc_stan_no).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_STAN_NO), 6, R.string.digits_2);
        } else if (Utils.getString(R.string.edc_smtp_host_name).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_SMTP_HOST), 50, 0);
        } else if (Utils.getString(R.string.edc_smtp_port).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_SMTP_PORT), 5, R.string.digits_2);
        } else if (Utils.getString(R.string.edc_smtp_username).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_SMTP_USERNAME), 50, 0);
        } else if (Utils.getString(R.string.edc_smtp_password).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_SMTP_PASSWORD), 50, 0, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else if (Utils.getString(R.string.edc_smtp_ssl_port).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_SMTP_SSL_PORT), 5, R.string.digits_2);
        } else if (Utils.getString(R.string.edc_smtp_from).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.EDC_SMTP_FROM), 100, 0, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            return null;
        }
    }

    @Override
    public void doOkNext(Object... args) {

    }
}
