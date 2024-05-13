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
 *  20190419   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.inflater;

import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.evp.abl.utils.EncUtils;
import com.evp.pay.app.quickclick.QuickClickProtection;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SysParam;

/**
 * The type Pwd inflater.
 *
 * @author ligq
 * @date 2019 /4/19 11:36
 */
public class PwdInflater implements ConfigInflater<ConfigThirdActivity> {
    private int len = 0;
    private String pwdValue;
    private EditText settingOldPwd;
    private EditText settingNewPwd;
    private EditText settingConfirmNewPwd;

    @Override
    public void inflate(ConfigThirdActivity act, String title) {
        ViewStub viewStub = act.findViewById(R.id.vs_third_content);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMarginStart((int) act.getResources().getDimension(R.dimen.space_horizontal));
        layoutParams.setMarginEnd((int) act.getResources().getDimension(R.dimen.space_horizontal));
        layoutParams.topMargin = (int) act.getResources().getDimension(R.dimen.space_vertical_small);
        viewStub.setLayoutParams(layoutParams);
        viewStub.setLayoutResource(R.layout.setting_pwd_manage_detail);
        viewStub.inflate();
        getPasswordParam();
        LinearLayout settingOldPwdLayout = act.findViewById(R.id.setting_old_pwd_layout);
        settingOldPwd = act.findViewById(R.id.setting_old_pwd);
        settingNewPwd = act.findViewById(R.id.setting_new_pwd);
        settingConfirmNewPwd = act.findViewById(R.id.setting_confirm_new_pwd);
        Button settingConfirmPwd = act.findViewById(R.id.setting_confirm_pwd);
        settingOldPwdLayout.setVisibility(View.VISIBLE);
        settingOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        settingNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        settingConfirmNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        ViewUtils.configInput(settingOldPwd);
        ViewUtils.configInput(settingNewPwd);
        ViewUtils.configInput(settingConfirmNewPwd);
        settingOldPwd.requestFocus();
        settingConfirmPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuickClickProtection quickClickProtection = QuickClickProtection.getInstance();
                if (quickClickProtection.isStarted()) {
                    return;
                }
                quickClickProtection.start();
                onClickProtected();
            }
        });
    }

    private boolean onClickProtected() {
        boolean flag = false;
        if (TextUtils.isEmpty(pwdValue)) {
            ToastUtils.showMessage(R.string.input_old_password);
        } else if (!EncUtils.sha1(settingOldPwd.getText().toString()).equals(pwdValue)) {
            ToastUtils.showMessage(R.string.error_old_password);
        } else if (TextUtils.isEmpty(settingNewPwd.getText().toString())) {
            ToastUtils.showMessage(R.string.input_new_password);
        } else if (TextUtils.isEmpty(settingConfirmNewPwd.getText().toString())) {
            ToastUtils.showMessage(R.string.input_again_new_password);
        } else if (settingNewPwd.length() != len) {
            ToastUtils.showMessage(R.string.error_input_length);
        } else if (!settingNewPwd.getText().toString().equals(settingConfirmNewPwd.getText().toString())) {
            ToastUtils.showMessage(R.string.error_password_no_same);
        } else {
            flag = true;
        }
        if (flag) {
            // 保存密码
            String password = settingNewPwd.getText().toString();
            SysParam.getInstance().set(R.string.SEC_SYS_PWD, EncUtils.sha1(password));
            ToastUtils.showMessage(R.string.password_modify_success);
            // AET-80
            settingOldPwd.setText("");
            settingNewPwd.setText("");
            settingConfirmNewPwd.setText("");
        }
        return flag;
    }

    private void getPasswordParam() {
        len = 6;
        pwdValue = SysParam.getInstance().getString(R.string.SEC_SYS_PWD);
    }

    @Override
    public boolean doNextSuccess() {
        return onClickProtected();
    }
}
