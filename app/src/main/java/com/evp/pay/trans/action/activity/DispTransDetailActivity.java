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
package com.evp.pay.trans.action.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;

import java.util.ArrayList;

/**
 * The type Disp trans detail activity.
 */
public class DispTransDetailActivity extends BaseActivityWithTickForAction {
    private Button btnConfirm;
    private Button cancelBtn;

    private String navTitle;
    private boolean navBack;
    private String fundingSource;

    private ArrayList<String> leftColumns = new ArrayList<>();
    private ArrayList<String> rightColumns = new ArrayList<>();

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
        fundingSource = bundle.getString("fundingSource");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trans_detail_layout;
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        LinearLayout llDetailContainer = (LinearLayout) findViewById(R.id.detail_layout);
        ImageView paymentIconImageView = (ImageView) findViewById(R.id.bar_payment_icon);
        LinearLayout paymentIconLayout = (LinearLayout) findViewById(R.id.bar_payment_layout);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root);
        constraintLayout.setBackgroundColor(secondaryColor);

        if (fundingSource != null) {
            paymentIconImageView.setImageBitmap(ConfigUtils.getInstance().getWalletImage(fundingSource));
        } else {
            paymentIconLayout.setVisibility(View.INVISIBLE);
        }

        if (primaryColor != -1) {
            paymentIconLayout.setBackground(new ColorDrawable(primaryColor));
        }

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;

        for (int i = 0; i < leftColumns.size(); i++) {
            LinearLayout layer = ViewUtils.genSingleLineLayout(DispTransDetailActivity.this, leftColumns.get(i),
                    rightColumns.get(i));
            if(layer != null) {
                llDetailContainer.addView(layer, params);
            }
        }

        btnConfirm = (Button) findViewById(R.id.confirm_btn);
        btnConfirm.setText(ConfigUtils.getInstance().getString("buttonConfirm"));
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setText(ConfigUtils.getInstance().getString("buttonCancel"));
    }

    @Override
    protected void setListeners() {
        enableBackAction(navBack);
        btnConfirm.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {
        if (v.getId() == R.id.confirm_btn)
            finish(new ActionResult(TransResult.SUCC, null));
        else
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
