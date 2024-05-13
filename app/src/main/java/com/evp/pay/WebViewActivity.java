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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.pay;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evp.pay.app.ActivityStack;
import com.evp.payment.evpscb.R;
import com.evp.view.dialog.ProgressHelper;
import com.evp.view.dialog.ProgressWheel;

/**
 * The type Web view activity.
 */
/* just a sample using default webview to show Ad link
   can be replace by some better webview, like Crosswalk.
 */
public class WebViewActivity extends BaseActivity {
    /**
     * The constant KEY.
     */
    public static final String KEY = "WEBVIEW";
    /**
     * The constant IS_FROM_WIDGET.
     */
    public static final String IS_FROM_WIDGET = "IS_FROM_WIDGET";

    private WebView webView;
    private String url;

    private ProgressWheel progressWheel;

    private boolean isFromWidget = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFromWidget) {
            ActivityStack.getInstance().popAll();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_web_view;
    }

    /**
     * load parameter
     */
    @Override
    protected void loadParam() {
        url = getIntent().getStringExtra(KEY);
        isFromWidget = getIntent().getBooleanExtra(IS_FROM_WIDGET, false);
    }

    @Override
    protected String getTitleString() {
        return getString(R.string.ad);
    }

    @Override
    protected void initViews() {
        progressWheel = (ProgressWheel) findViewById(R.id.progressWheel);

        webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl(url);
        webView.setWebViewClient(new Client());

        ProgressHelper progressHelper = new ProgressHelper(WebViewActivity.this);
        progressHelper.setProgressWheel(progressWheel);
    }

    /**
     * set listeners
     */
    @Override
    protected void setListeners() {
        //do nothing
    }

    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            webView.stopLoading();
            finish();
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }

    @Override
    protected boolean onKeyBackDown() {
        if (webView.canGoBack() && !webView.getUrl().equals(webView.getOriginalUrl())) {
            webView.goBack(); //goBack()表示返回WebView的上一页面
        } else {
            webView.stopLoading();
            finish();
        }
        return true;
    }

    //Web视图
    private class Client extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressWheel.startAnimation(AnimationUtils.loadAnimation(WebViewActivity.this, R.anim.slide_in_from_top));
            progressWheel.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressWheel.startAnimation(AnimationUtils.loadAnimation(WebViewActivity.this, R.anim.slide_out_to_top));
            progressWheel.setVisibility(View.GONE);
        }
    }
}
