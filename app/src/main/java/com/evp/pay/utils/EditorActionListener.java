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
package com.evp.pay.utils;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;

/**
 * The type Editor action listener.
 */
public abstract class EditorActionListener implements TextView.OnEditorActionListener {
    public static final int QR_KEY_PRESSED = -1000;
    public static final int SCAN_KEY_PRESSED = -1001;
    public static final int CARD_KEY_PRESSED = -1002;

    @Override
    public boolean onEditorAction(TextView v, final int actionId, KeyEvent event) {
        if (actionId <= QR_KEY_PRESSED) {
            FinancialApplication.getApp().runOnUiThread(() -> onKeyCustomAction(actionId));
            return true;
        } else if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                FinancialApplication.getApp().runOnUiThread(this::onKeyOk);
                return true;
            }
        } else if (actionId == EditorInfo.IME_ACTION_DONE) {
            FinancialApplication.getApp().runOnUiThread(this::onKeyOk);
            return true;
        } else if (actionId == EditorInfo.IME_ACTION_NONE) {
            FinancialApplication.getApp().runOnUiThread(this::onKeyCancel);
            return true;
        }
        return false;
    }

    /**
     * On key custom action.
     */
    protected void onKeyCustomAction(int actionId) {}

    /**
     * On key ok.
     */
    protected abstract void onKeyOk();

    /**
     * On key cancel.
     */
    protected abstract void onKeyCancel();
}
