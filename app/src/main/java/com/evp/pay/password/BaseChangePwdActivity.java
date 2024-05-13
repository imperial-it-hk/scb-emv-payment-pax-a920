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
package com.evp.pay.password;


import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.BaseActivity;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.ToastUtils;
import com.evp.payment.evpscb.R;
import com.evp.view.keyboard.KeyboardUtils;

/**
 * Chang password
 *
 * @author Steven.W
 */
public abstract class BaseChangePwdActivity extends BaseActivity {
    /**
     * The Edt new pwd.
     */
    protected EditText edtNewPwd;
    /**
     * The Edt new pwd confirm.
     */
    protected EditText edtNewPwdConfirm;

    /**
     * The Btn confirm.
     */
    protected Button btnConfirm;
    /**
     * The Nav title.
     */
    protected String navTitle;
    /**
     * The Pwd.
     */
    protected String pwd;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected void onPause() {
        KeyboardUtils.hideSystemKeyboard(getWindow().getDecorView()); // AET-121 AET-270
        super.onPause();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chg_pwd_layout;
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        edtNewPwd = (EditText) findViewById(R.id.setting_new_pwd);
        edtNewPwd.requestFocus();
        edtNewPwdConfirm = (EditText) findViewById(R.id.setting_confirm_new_pwd);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);
        btnConfirm = (Button) findViewById(R.id.setting_confirm_pwd);
        btnConfirm.setBackgroundColor(primaryColor);
    }

    @Override
    protected void setListeners() {
        btnConfirm.setOnClickListener(this);
        TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                        LogUtils.d("silly muhua", "物理按键的Enter, 等同于点击页面上的OK按钮");
                        onOkClicked();
                        return true;
                    }
                }
                return false;
            }
        };
        edtNewPwd.setOnEditorActionListener(actionListener);
        edtNewPwdConfirm.setOnEditorActionListener(actionListener);
    }

    @Override
    public void onClickProtected(View v) {
        if (v.getId() == R.id.setting_confirm_pwd) {
            onOkClicked();
        }
    }

    private void onOkClicked() {
        if (updatePwd()) {
            ToastUtils.showMessage(R.string.pwd_succ);
            finish();
        }
    }

    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }

    /**
     * Save pwd.
     */
//save to SP
    protected abstract void savePwd();

    /**
     * Update pwd boolean.
     *
     * @return the boolean
     */
    protected boolean updatePwd() {

        String newPWD = edtNewPwd.getText().toString();
        if ("".equals(newPWD)) {
            edtNewPwd.requestFocus();
            return false;
        }

        if (newPWD.length() != 6) {
            ToastUtils.showMessage(R.string.pwd_incorrect_length);
            return false;
        }

        String newAgainPWD = edtNewPwdConfirm.getText().toString();

        if ("".equals(newAgainPWD)) {
            edtNewPwdConfirm.requestFocus();
            return false;
        }

        if (!newAgainPWD.equals(newPWD)) {
            ToastUtils.showMessage(R.string.pwd_not_equal);
            return false;
        }
        pwd = edtNewPwd.getText().toString().trim();
        savePwd();
        return true;
    }

}
