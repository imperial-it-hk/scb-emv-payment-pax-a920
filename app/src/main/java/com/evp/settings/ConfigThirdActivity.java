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
 * 20190108  	         ligq                    Create
 * ===========================================================================================
 */
package com.evp.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.BaseConfigActivity;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.payment.evpscb.R;
import com.evp.settings.config.ConfigAcquirer;
import com.evp.settings.config.ConfigComm;
import com.evp.settings.config.ConfigEdc;
import com.evp.settings.config.ConfigIssuer;
import com.evp.settings.config.ConfigOther;
import com.evp.settings.config.ConfigPwd;
import com.evp.settings.config.ConfigQuick;
import com.evp.settings.config.IConfig;
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.ConfigSelectInflater;
import com.evp.view.dialog.DialogUtils;

/**
 * execute the function from ConfigFirstActivity and ConfigSecondActivity
 * all the functions are executed in their own inflater
 */
public class ConfigThirdActivity extends BaseConfigActivity {
    private ConfigInflater<ConfigThirdActivity> inflater = null;
    /**
     * The M config.
     */
    IConfig<ConfigInflater<ConfigThirdActivity>> mConfig = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config_third;
    }

    @Override
    protected void initViews() {
        String name = "";
        String title = getToolBarTitle();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String issuerNameTemp = extras.getString(EUIParamKeys.ISSUER_NAME.toString());
            if (issuerNameTemp != null) {
                name = issuerNameTemp;
            }
            String acquirerNameTemp = extras.getString(EUIParamKeys.ACQUIRER_NAME.toString());
            if (acquirerNameTemp != null) {
                name = acquirerNameTemp;
            }
        }
        if (SettingConst.LIST_TYPE_QUICK.contains(title)) {
            mConfig = new ConfigQuick();
        } else if (SettingConst.LIST_TYPE_COMM.contains(title)) {
            mConfig = new ConfigComm(this);
        } else if (SettingConst.LIST_TYPE_EDC.contains(title)) {
            mConfig = new ConfigEdc();
        } else if (SettingConst.LIST_TYPE_ISSUER.contains(title)) {
            mConfig = new ConfigIssuer(name);
        } else if (SettingConst.LIST_TYPE_ACQUIRER.contains(title)) {
            mConfig = new ConfigAcquirer(name);
        } else if (getString(R.string.settings_menu_pwd_manage).equals(title)) {
            mConfig = new ConfigPwd();
        } else if (getString(R.string.settings_menu_otherManage).equals(title)) {
            mConfig = new ConfigOther();
        } else {
            mConfig = null;
        }
        if (mConfig != null) {
            inflater = mConfig.getInflater(title);
        }
        if (getString(R.string.settings_menu_pwd_manage).equals(title) ||
                getString(R.string.settings_menu_otherManage).equals(title) ||
                inflater instanceof ConfigSelectInflater) {
            initToolBar(false);
        } else {
            initToolBar(true);
        }
        if (inflater != null) {
            inflater.inflate(this, title);
        }
    }

    @Override
    public void onKeyOkDown() {
        LogUtils.d("ConfigThird", "doNextSuccess!");
        if (inflater == null || !inflater.doNextSuccess()) {
            return;
        }
        onKeyBackDown();
    }

    /**
     * Open write settings.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public void openWriteSettings() {
        Intent gotoSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        gotoSettings.setData(Uri.parse("package:"+getPackageName()));
        startActivityForResult(gotoSettings, SettingConst.REQ_WRITE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mConfig != null) {
            mConfig.doOkNext(requestCode);
        }
    }

    @Override
    public boolean onKeyBackDown() {
        return super.onKeyBackDown();
    }

    @Override
    protected boolean onKeyDel() {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            onKeyOkDown();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void showMessage(String title, String message) {
        DialogUtils.showErrMessage(ConfigThirdActivity.this, title, message, null, Constants.FAILED_DIALOG_SHOW_TIME);
    }
}
