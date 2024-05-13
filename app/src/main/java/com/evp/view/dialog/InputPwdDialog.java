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
package com.evp.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.utils.EditorActionListener;
import com.evp.payment.evpscb.R;
import com.evp.view.keyboard.CustomKeyboardView;
import com.evp.view.keyboard.KeyboardUtils;

/**
 * The type Input pwd dialog.
 */
public class InputPwdDialog extends Dialog {

    private String title; // 标题
    private String prompt; // 提示信息

    private EditText pwdEdt;
    private int maxLength;

    private OnPwdListener listener;

    private int primaryColor;
    private int secondaryColor;

    /**
     * Instantiates a new Input pwd dialog.
     *
     * @param context the context
     * @param length  the length
     * @param title   the title
     * @param prompt  the prompt
     */
    public InputPwdDialog(Context context, int length, String title, String prompt) {
        this(context, R.style.PopupDialog);
        this.maxLength = length;
        this.title = title;
        this.prompt = prompt;
        this.primaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.PRIMARY_COLOR));
        this.secondaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SECONDARY_COLOR));
    }

    /**
     * 输联机密码时调用次构造方法
     *
     * @param context the context
     * @param title   the title
     * @param prompt  the prompt
     */
    public InputPwdDialog(Context context, String title, String prompt) {
        super(context, R.style.PopupDialog);
        this.title = title;
        this.prompt = prompt;
        this.primaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.PRIMARY_COLOR));
        this.secondaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SECONDARY_COLOR));
    }

    /**
     * Instantiates a new Input pwd dialog.
     *
     * @param context the context
     * @param theme   the theme
     */
    public InputPwdDialog(Context context, int theme) {
        super(context, theme);
        this.primaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.PRIMARY_COLOR));
        this.secondaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SECONDARY_COLOR));
    }

    /**
     * The interface On pwd listener.
     */
    public interface OnPwdListener {
        /**
         * On succ.
         *
         * @param data the data
         */
        void onSucc(String data);

        /**
         * On err.
         */
        void onErr();
    }

    /**
     * Sets pwd listener.
     *
     * @param listener the listener
     */
    public void setPwdListener(OnPwdListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View convertView = getLayoutInflater().inflate(R.layout.activity_inner_pwd_layout, null);
        convertView.setBackgroundColor(secondaryColor);
        setContentView(convertView);
        if (getWindow() == null)
            return;
        getWindow().setGravity(Gravity.BOTTOM); // 显示在底部
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;//(int) (ViewUtils.getScreenHeight(this.getContext()) * 0.6);  // 屏幕高度（像素）

        getWindow().setAttributes(lp);
        initViews(convertView);
    }

    private void initViews(View view) {

        TextView titleTv = (TextView) view.findViewById(R.id.prompt_title);
        titleTv.setText(title);

        TextView subtitleTv = (TextView) view.findViewById(R.id.prompt_no_pwd);
        if (prompt != null) {
            subtitleTv.setText(prompt);
        } else {
            subtitleTv.setVisibility(View.GONE);
        }

        TextView pwdTv = (TextView) view.findViewById(R.id.pwd_input_text);
        pwdTv.setVisibility(View.GONE);
        pwdEdt = (EditText) view.findViewById(R.id.pwd_input_et);
        pwdEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        KeyboardUtils.hideSystemKeyboard(pwdEdt);
        pwdEdt.setInputType(InputType.TYPE_NULL);
        pwdEdt.setFocusable(true);
        pwdEdt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        pwdEdt.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        Keyboard keyboard = new Keyboard(view.getContext(), R.xml.numeric_keyboard_confirm);

        CustomKeyboardView keyboardView = (CustomKeyboardView) view.findViewById(R.id.pwd_keyboard);
        KeyboardUtils.bind(keyboardView, new KeyboardUtils(view.getContext(), keyboard, pwdEdt));

        pwdEdt.setOnEditorActionListener(new PwdActionListener());
    }

    private class PwdActionListener extends EditorActionListener {
        @Override
        public void onKeyOk() {
            String content = pwdEdt.getText().toString().trim();
            if (listener != null) {
                listener.onSucc(content);
            }
        }

        @Override
        public void onKeyCancel() {
            if (listener != null) {
                listener.onErr();
            }
        }
    }
}
