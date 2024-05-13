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
 * 20190108  	         huangwp                 Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.payment.evpscb.R;

/**
 * The type User agreement activity.
 */
public class UserAgreementActivity extends BaseActivityWithTickForAction implements View.OnClickListener {
    /**
     * The constant ENTER_BUTTON.
     */
    public static final String ENTER_BUTTON = "ENTER";
    private static final String AGREEMENT_FILE_PATH = "file:///android_asset/agreement_content.html";

    private Button confirmBtn;
    private CheckBox checkBox;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_agreement_layout;
    }

    @Override
    protected void loadParam() {
        //do nothing
    }

    @Override
    protected String getTitleString() {
        return getString(R.string.trans_user_agreement);
    }

    @Override
    protected void initViews() {

        WebView agreementText = (WebView) findViewById(R.id.AgreementContent);
        agreementText.getSettings().setJavaScriptEnabled(false);
        agreementText.getSettings().setAllowFileAccess(false);
        agreementText.removeJavascriptInterface("searchBoxJavaBridge_");
        agreementText.removeJavascriptInterface("accessibility");
        agreementText.removeJavascriptInterface("accessibilityTraversal");
        agreementText.loadUrl(AGREEMENT_FILE_PATH);

        checkBox = (CheckBox) findViewById(R.id.AgreementCheck);
        checkBox.setChecked(false);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        confirmBtn = (Button) findViewById(R.id.enter_btn);
        confirmBtn.setEnabled(false); //AET-170
    }

    @Override
    protected void setListeners() {
        confirmBtn.setOnClickListener(this);
        checkBox.setOnClickListener(this);
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.enter_btn:
                finish(new ActionResult(TransResult.SUCC, ENTER_BUTTON));
                break;
            case R.id.AgreementCheck:
                confirmBtn.setEnabled(checkBox.isChecked());
                break;
            default:
                break;
        }
    }

}
