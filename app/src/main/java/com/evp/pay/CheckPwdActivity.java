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
 * 20190108  	         lixc                    Create
 * ===========================================================================================
 */
package com.evp.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.model.Controller;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;
import com.evp.view.UserGuideManager;
import com.evp.view.dialog.DialogUtils;
import com.shizhefei.guide.GuideHelper;


/**
 * The type Check pwd activity.
 */
public class CheckPwdActivity extends BaseActivity {

    /**
     * The constant REQ_WIZARD.
     */
    public static final int REQ_WIZARD = 1;

    private EditText edtPwd;

    /**
     * On check pwd.
     *
     * @param activity    the activity
     * @param requestCode the request code
     */
    public static void onCheckPwd(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CheckPwdActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void loadParam() {
        // do nothing
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_check_pwd_layout;
    }

    @Override
    protected void initViews() {
        enableActionBar(false);
        edtPwd = (EditText) findViewById(R.id.operator_pwd_edt);
        SpannableString ss = new SpannableString(Utils.getString(R.string.init_pwd_hint));
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(18, true);
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        edtPwd.setHint(new SpannedString(ss));
        edtPwd.requestFocus();

        initUserGuideView();
    }

    private void initUserGuideView() {
        if (!UserGuideManager.getInstance().isEnabled()) {
            return;
        }
        final GuideHelper guideHelper = new GuideHelper(this);
        GuideHelper.TipData tipData = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_terminal_password_jap : R.drawable.tip_terminal_password, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, edtPwd);
        tipData.setViewBg(getResources().getDrawable(R.drawable.white_bg));
        GuideHelper.TipData tipOk = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_ok_btn_jap : R.drawable.tip_ok_btn_en, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tipOk.setLocation(0, -ViewUtils.dp2px(150));
        tipOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideHelper.nextPage();
            }
        });
        guideHelper.addPage(false, tipData, tipOk);
        guideHelper.show(false);
    }

    @Override
    protected void setListeners() {
        edtPwd.setOnEditorActionListener(new PwdActionListener());
    }

    private class PwdActionListener extends EditorActionListener {
        @Override
        public void onKeyOk() {
            process();
        }

        @Override
        public void onKeyCancel() {
            exit();
        }

        /**
         * check password
         */
        private void process() {
            String password = edtPwd.getText().toString().trim();
            if (password.isEmpty()) {
                edtPwd.setFocusable(true);
                edtPwd.requestFocus();
                return;
            }
            if (!password.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.ADMIN_PASSWORD))) {
                ToastUtils.showMessage(R.string.error_password);
                edtPwd.setText("");
                edtPwd.setFocusable(true);
                edtPwd.requestFocus();
                return;
            }

            //start wizard activity
            Intent intent = new Intent(CheckPwdActivity.this, ConfigFirstActivity.class);
            startActivityForResult(intent, REQ_WIZARD);
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        // exit app
        exit();
        return true;
    }

    private void exit() {
        DialogUtils.showExitAppDialog(CheckPwdActivity.this);
        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_WIZARD) {
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            FinancialApplication.getController().set(Controller.IS_FIRST_RUN, false);
            finish();
        }
    }
}
