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

import android.os.Build;
import android.provider.Settings;

import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.pay.utils.InjectKeyUtil;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.SysParam;
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.ConfigInputInflater;
import com.evp.settings.inflater.ConfigSelectInflater;

import org.jetbrains.annotations.NotNull;

/**
 * The type Config comm.
 *
 * @author ligq
 * @date 2019 /4/18 14:48
 */
public class ConfigComm implements IConfig<ConfigInflater<ConfigThirdActivity>>, ConfigSelectInflater.OnOkListener {
    private String selectKey;
    private String selectContent;
    private ConfigThirdActivity mActivity;
    private String oldCommType;

    /**
     * Instantiates a new Config comm.
     *
     * @param activity the activity
     */
    public ConfigComm(ConfigThirdActivity activity) {
        mActivity = activity;
    }

    @Override
    public ConfigInflater<ConfigThirdActivity> getInflater(String title) {
        if (Utils.getString(R.string.commParam_menu_comm_timeout).equals(title)) {
            return new ConfigSelectInflater();
        } else if (Utils.getString(R.string.settings_menu_communication_type).equals(title)) {
            oldCommType = (String)SysParam.getInstance().get(Utils.getString(R.string.COMM_TYPE), new String());
            ConfigSelectInflater configSelectInflater = new ConfigSelectInflater();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                configSelectInflater.setOnOkListener(this);
            }
            return configSelectInflater;
        } else if (Utils.getString(R.string.commParam_menu_mobile_dial_no).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MOBILE_TEL_NO), 50, 0);
        } else if (Utils.getString(R.string.commParam_menu_mobile_apn).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MOBILE_APN), 50, 0);
        } else if (Utils.getString(R.string.commParam_menu_mobile_username).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MOBILE_USER), 50, 0);
        } else if (Utils.getString(R.string.commParam_menu_mobile_user_password).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MOBILE_PWD), 50, 0);
        } else {
            return null;
        }
    }

    @Override
    public void doOkNext(Object... args) {
        if ((int) args[0] == SettingConst.REQ_WRITE_SETTINGS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(mActivity)) {
                saveCommType();
            } else {
                ToastUtils.showMessage(R.string.prompt_open_write_settings);
            }
        }
    }

    @Override
    public void onOkClick(@NotNull String key, @NotNull String content) {
        selectContent = content;
        selectKey = key;
        if (Utils.getString(R.string.COMM_TYPE).equals(key)) {
            if (content.equals(SysParam.CommType.DEMO) || oldCommType.equals(SysParam.CommType.DEMO)) {
                long countOfTrx = GreendaoHelper.getTransDataHelper().countOf();
                if(countOfTrx > 0) {
                    mActivity.showMessage(Utils.getString(R.string.swith_demo_mode), Utils.getString(R.string.has_trans_for_settle));
                    return;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(mActivity)) {
                mActivity.openWriteSettings();
            } else {
                saveCommType();
            }
        }
    }

    private void saveCommType() {
        SysParam.getInstance().set(selectKey, selectContent);
        InjectKeyUtil.injectMKSK(selectContent);
        mActivity.onKeyBackDown();
    }
}
