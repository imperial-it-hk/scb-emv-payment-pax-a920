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
package com.evp.pay;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.quickclick.QuickClickProtection;
import com.evp.payment.evpscb.BuildConfig;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.lang.reflect.Method;

/**
 * The type Base activity.
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The constant TAG.
     */
    protected static final String TAG = "The Activity";
    /**
     * The Quick click protection.
     */
    protected QuickClickProtection quickClickProtection = QuickClickProtection.getInstance();
    private ActionBar mActionBar;
    protected int primaryColor = -1;
    protected int secondaryColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.RELEASE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);//禁用截屏
        }

        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        if (getLayout() != null)
            setContentView(getLayout());
        else
            setContentView(getLayoutId());

        loadParam(); //AET-274

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(getTitleString());
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(true);
            if (SysParam.getInstance().getLanguage() == null || SysParam.getInstance().getLanguage() == "") {
                SysParam.getInstance().setLanguage(ConfigUtils.getInstance().getDeviceConf(ConfigConst.DEFAULT_LANGUAGE));
                ConfigUtils.getInstance().reloadLanguage();
            }
            primaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.PRIMARY_COLOR));
            secondaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SECONDARY_COLOR));
            mActionBar.setBackgroundDrawable(new ColorDrawable(primaryColor));
        }

        initViews();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        quickClickProtection.stop();
        LogUtils.i("onDestroy", "");
    }

    /**
     * get layout ID
     *
     * @return layout ID
     */
    protected abstract int getLayoutId();

    protected View getLayout() {
        return null;
    }

    /**
     * views initial
     */
    protected abstract void initViews();

    /**
     * set listeners
     */
    protected abstract void setListeners();

    /**
     * load parameter
     */
    protected abstract void loadParam();

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                LogUtils.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // AET-93
    @Override
    public final void onClick(View v) {
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();
        onClickProtected(v);
    }

    /**
     * On click protected.
     *
     * @param v the v
     */
    protected void onClickProtected(View v) {
        //do nothing
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (quickClickProtection.isStarted()) { //AET-123
            return true;
        }
        quickClickProtection.start();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return onKeyBackDown();
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            return onKeyDel();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * On key back down boolean.
     *
     * @return the boolean
     */
    protected boolean onKeyBackDown() {
        finish();
        return true;
    }

    /**
     * On key del boolean.
     *
     * @return the boolean
     */
    protected boolean onKeyDel() {
        return onKeyBackDown();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (quickClickProtection.isStarted()) { //AET-123
            return true;
        }
        quickClickProtection.start();
        return onOptionsItemSelectedSub(item);
    }

    /**
     * On options item selected sub boolean.
     *
     * @param item the item
     * @return the boolean
     */
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets title string.
     *
     * @return the title string
     */
    protected String getTitleString() {
        return getString(R.string.app_name);
    }

    /**
     * Enable back action.
     *
     * @param enableBack the enable back
     */
    protected void enableBackAction(boolean enableBack) {
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(enableBack);
        }
    }

    /**
     * 设置是否显示ActionBar
     *
     * @param showActionBar true 显示 false 隐藏
     */
    protected void enableActionBar(boolean showActionBar) {
        if (mActionBar != null) {
            if (showActionBar) {
                mActionBar.show();
            } else {
                mActionBar.hide();
            }
        }
    }
}
