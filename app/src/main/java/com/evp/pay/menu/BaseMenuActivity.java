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
package com.evp.pay.menu;

import android.app.ActionBar.LayoutParams;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.evp.pay.BaseActivity;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;

/**
 * Base Menu activity
 */
public abstract class BaseMenuActivity extends BaseActivity implements OnClickListener {
    //navigation title
    private String navTitle;
    private boolean navBack;
    protected boolean startsFromInvoke;
    /**
     * get layout ID
     * @return  layout ID
     */
    @Override
    protected int getLayoutId() {
        return R.layout.menu_layout;
    }
    /**
     * load parameter
     */
    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        startsFromInvoke = getIntent().getBooleanExtra(EUIParamKeys.IS_INVOKE.toString(), false);
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }
    /**
     * views initial
     */
    @Override
    protected void initViews() {
        LinearLayout llContainer = (LinearLayout) findViewById(R.id.ll_container);
        llContainer.setBackgroundColor(secondaryColor);
        android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        llContainer.addView(createMenuPage(), params);

    }

    /**
     * Create menu page menu page.
     *
     * @return the menu page
     */
    public abstract MenuPage createMenuPage();
    /**
     * set listeners
     */
    @Override
    protected void setListeners() {
        enableBackAction(navBack);
    }

    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }
}
