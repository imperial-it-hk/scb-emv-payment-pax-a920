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
package com.evp.pay;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.evp.pay.utils.RxUtils;
import com.evp.payment.evpscb.R;

/**
 * base activity for config activity
 */
public class BaseConfigActivity extends BaseActivity {
    /**
     * The Toolbar.
     */
    protected Toolbar toolbar;

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initViews() {
        initToolBar(false);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void loadParam() {

    }

    /**
     * init toolbar
     *
     * @param showOk whether show ok
     */
    protected void initToolBar(Boolean showOk) {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getToolBarTitle());
        if (showOk) {
            ImageView okImg = findViewById(R.id.iv_toolbar_ok);
            okImg.setVisibility(View.VISIBLE);
            okImg.setOnClickListener(this);
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyBackDown();
            }
        });
    }

    @Override
    protected void onClickProtected(View v) {
        if (v.getId() == R.id.iv_toolbar_ok) {
            onKeyOkDown();
        }
    }

    /**
     * Gets tool bar title.
     *
     * @return the tool bar title
     */
    protected String getToolBarTitle() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return "";
        }
        return extras.getString("title", getString(R.string.settings_menu_communication_parameter));
    }

    /**
     * On key ok down.
     */
    protected void onKeyOkDown() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.release();
    }
}
