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
 *  20190417   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings;

import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Setting const.
 *
 * @author ligq
 * @date 2019 /4/17 15:56
 */
public class SettingConst {
    /**
     * The constant NORMAL.
     */
    public static final int NORMAL = 0;//menu items
    /**
     * The constant FUN.
     */
    public static final int FUN = 1;//Function, like echo

    /**
     * The constant PRINT_AID.
     */
    public static final String PRINT_AID = "aid";
    /**
     * The constant PRINT_CAPK.
     */
    public static final String PRINT_CAPK = "capk";

    /**
     * The constant TYPE_COMM.
     */
    public static final String TYPE_COMM = "Comm";
    /**
     * The constant TYPE_EDC.
     */
    public static final String TYPE_EDC = "EDC";
    /**
     * The constant TYPE_ISSUER.
     */
    public static final String TYPE_ISSUER = "Issuer";
    /**
     * The constant TYPE_ACQUIRER.
     */
    public static final String TYPE_ACQUIRER = "Acquirer";
    /**
     * The constant TYPE_OTHER.
     */
    public static final String TYPE_OTHER = "Other";
    /**
     * The constant KEY_NONE.
     */
    public static final String KEY_NONE = "N";

    /**
     * The constant INTENT_TITLE.
     */
    public static final String INTENT_TITLE = "title";

    /**
     * The constant TYPE_TMK.
     */
    public static final String TYPE_TMK = "tmk";
    /**
     * The constant TYPE_TPK.
     */
    public static final String TYPE_TPK = "tpk";
    /**
     * The constant TYPE_TAK.
     */
    public static final String TYPE_TAK = "tak";

    /**
     * The constant LIST_TYPE_QUICK.
     */
    public static final List<String> LIST_TYPE_QUICK = new ArrayList<>();
    /**
     * The constant LIST_TYPE_COMM.
     */
    public static final List<String> LIST_TYPE_COMM = new ArrayList<>();
    /**
     * The constant LIST_TYPE_EDC.
     */
    public static final List<String> LIST_TYPE_EDC = new ArrayList<>();
    /**
     * The constant LIST_TYPE_ISSUER.
     */
    public static final List<String> LIST_TYPE_ISSUER = new ArrayList<>();
    /**
     * The constant LIST_TYPE_ACQUIRER.
     */
    public static final List<String> LIST_TYPE_ACQUIRER = new ArrayList<>();

    /**
     * The constant REQ_WRITE_SETTINGS.
     */
    public static final int REQ_WRITE_SETTINGS = 0;

    static {
        LIST_TYPE_QUICK.add(Utils.getString(R.string.keyManage_menu_tmk_index));
        LIST_TYPE_QUICK.add(Utils.getString(R.string.keyManage_menu_tmk_index_no));
        LIST_TYPE_QUICK.add(Utils.getString(R.string.keyManage_menu_tmk_value));
        LIST_TYPE_QUICK.add(Utils.getString(R.string.keyManage_menu_tpk_value));
        LIST_TYPE_QUICK.add(Utils.getString(R.string.keyManage_menu_tak_value));

        LIST_TYPE_COMM.add(Utils.getString(R.string.settings_menu_communication_type));
        LIST_TYPE_COMM.add(Utils.getString(R.string.commParam_menu_comm_timeout));
        LIST_TYPE_COMM.add(Utils.getString(R.string.commParam_menu_mobile_dial_no));
        LIST_TYPE_COMM.add(Utils.getString(R.string.commParam_menu_mobile_apn));
        LIST_TYPE_COMM.add(Utils.getString(R.string.commParam_menu_mobile_username));
        LIST_TYPE_COMM.add(Utils.getString(R.string.commParam_menu_mobile_user_password));

        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_merchant_name));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_merchant_address));
        LIST_TYPE_EDC.add(Utils.getString(R.string.currency_list));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_ped_mode));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_clss_mode));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_printer_type));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_receipt_no));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_trace_no));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_stan_no));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_smtp_host_name));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_smtp_port));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_smtp_username));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_smtp_password));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_smtp_ssl_port));
        LIST_TYPE_EDC.add(Utils.getString(R.string.edc_smtp_from));

        LIST_TYPE_ISSUER.add(Utils.getString(R.string.settings_menu_issuer_parameter));
        LIST_TYPE_ISSUER.add(Utils.getString(R.string.issuer_non_emv_tran_floor_limit));
        LIST_TYPE_ISSUER.add(Utils.getString(R.string.issuer_adjust_percent));

        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.settings_menu_acquirer_parameter));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_terminal_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_merchant_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_nii));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_batch_no));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_ip));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_port));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.SSL));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_version));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_nii));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_kms_nii));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_vendor_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_acqid));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_key_set_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_te_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_te_pin));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.acq_tle_sensitive_fields));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.alipay_terminal_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.alipay_merchant_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.alipay_acquirer));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.wechat_terminal_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.wechat_merchant_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.wechat_acquirer));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.tag30_terminal_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.tag30_merchant_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.tag30_biller_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.tag30_merchant_name));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.tag30_partner_code));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.qrcs_terminal_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.qrcs_merchant_id));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.qrcs_partner_code));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.inquiry_timeout));
        LIST_TYPE_ACQUIRER.add(Utils.getString(R.string.inquiry_retries));
    }
}
