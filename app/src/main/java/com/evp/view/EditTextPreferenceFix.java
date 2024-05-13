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
package com.evp.view;

import android.app.Activity;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

import com.evp.view.keyboard.KeyboardUtils;

/**
 * The type Edit text preference fix.
 */
public class EditTextPreferenceFix extends EditTextPreference {

    /**
     * Instantiates a new Edit text preference fix.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public EditTextPreferenceFix(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Instantiates a new Edit text preference fix.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public EditTextPreferenceFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new Edit text preference fix.
     *
     * @param context the context
     */
    public EditTextPreferenceFix(Context context) {
        this(context, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        getEditText().clearFocus();
        hideSysInput();
    }

    private void hideSysInput() {
        Window window = ((Activity) getContext()).getWindow();
        final View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);

        if (contentView.getWindowToken() != null) {

            contentView.post(new Runnable() {
                @Override
                public void run() {
                    KeyboardUtils.hideSystemKeyboard(contentView);
                }
            });
        }
    }
}
