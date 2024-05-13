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

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.ActionInputTransData.EInputType;
import com.evp.pay.trans.component.Component;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.EnterAmountTextWatcher;
import com.evp.pay.utils.ToastUtils;
import com.evp.payment.evpscb.R;
import com.evp.view.keyboard.CustomKeyboardEditText;

/**
 * The type Input trans data 1 activity.
 */
public class InputTransData1Activity extends BaseActivityWithTickForAction {

    private Button confirmBtn;
    private Button cancelBtn;
    private ImageButton scanBtn;

    private String prompt;
    private String navTitle;
    private int tickTime;

    private EInputType inputType;

    private boolean isGetLastTrans;
    private boolean isPaddingZero;
    private boolean isQrScan;

    private int maxLen;
    private int minLen;

    private CustomKeyboardEditText mEditText = null;
    private EditText editText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEditText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inputType == EInputType.NUM) {
            mEditText.setText("");
        } else {
            editText.setText("");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_trans_data1;
    }

    @Override
    protected void loadParam() {
        prompt = getIntent().getStringExtra(EUIParamKeys.PROMPT_1.toString());
        inputType = (EInputType) getIntent().getSerializableExtra(EUIParamKeys.INPUT_TYPE.toString());
        maxLen = getIntent().getIntExtra(EUIParamKeys.INPUT_MAX_LEN.toString(), 6);
        minLen = getIntent().getIntExtra(EUIParamKeys.INPUT_MIN_LEN.toString(), 0);
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        isGetLastTrans = getIntent().getBooleanExtra(EUIParamKeys.GET_LAST_TRANS_UI.toString(), false);
        isPaddingZero = getIntent().getBooleanExtra(EUIParamKeys.INPUT_PADDING_ZERO.toString(), true);
        isQrScan = getIntent().getBooleanExtra(EUIParamKeys.IS_QR_SCAN.toString(), false);
        tickTime = getIntent().getIntExtra(EUIParamKeys.TIKE_TIME.toString(), 0);
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        TextView promptText = (TextView) findViewById(R.id.prompt_message);
        promptText.setText(prompt);

        confirmBtn = (Button) findViewById(R.id.info_confirm);
        confirmBtn.setText(ConfigUtils.getInstance().getString("buttonOk"));
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setText(ConfigUtils.getInstance().getString("buttonCancel"));
        scanBtn = (ImageButton) findViewById(R.id.scan_code_btn);
        LinearLayout linearLayout = findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);
        if (!isQrScan) {
            scanBtn.setVisibility(View.INVISIBLE);
        }
        TextView promptDoLast = (TextView) findViewById(R.id.prompt_do_last);
        promptDoLast.setText(ConfigUtils.getInstance().getString("getLastTransactionByPressOK"));
        if (!isGetLastTrans) {
            promptDoLast.setVisibility(View.INVISIBLE);
        }

        if (tickTime > 0) {
            tickTimer.start(tickTime);
        }
    }

    private void setEditText() {
        setEditTextNum();
        if (inputType == EInputType.NUM) {
            if (mEditText != null) {
                mEditText.setOnEditorActionListener(new EditorActionListener() {
                    @Override
                    protected void onKeyOk() {
                        quickClickProtection.stop();
                        onClick(confirmBtn);
                    }

                    @Override
                    protected void onKeyCancel() {
                        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                    }
                });
            } else {
                if (editText != null) {
                    editText.setOnEditorActionListener(new EditorActionListener() {
                        @Override
                        protected void onKeyOk() {
                            quickClickProtection.stop();
                            onClick(confirmBtn);
                        }

                        @Override
                        protected void onKeyCancel() {
                            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                        }
                    });
                }
            }
        }
    }

    // 数字
    private void setEditTextNum() {
        if (inputType == EInputType.TEXT) {
            editText = (EditText) findViewById(R.id.ref_edit_text);
            mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_1);
            mEditText.setVisibility(View.GONE);
            editText.requestFocus();
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
            if (minLen == 0) {
                confirmBtn.setEnabled(true);
                confirmBtn.setBackgroundResource(R.drawable.confirmbtn);
            } else {
                editText.addTextChangedListener(new EnterAmountTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmBtnChange();
                    }
                });
            }
        } else {
            mEditText = (CustomKeyboardEditText) findViewById(R.id.input_data_1);
            editText = (EditText) findViewById(R.id.ref_edit_text);
            editText.setVisibility(View.GONE);
            mEditText.requestFocus();
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
            if (minLen == 0) {
                confirmBtn.setEnabled(true);
                confirmBtn.setBackgroundResource(R.drawable.confirmbtn);
            } else {
                mEditText.addTextChangedListener(new EnterAmountTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmBtnChange();
                    }
                });
            }
        }
    }

    @Override
    protected void setListeners() {
        confirmBtn.setOnClickListener(this);
        scanBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {
        if (v.getId() == R.id.info_confirm) {
            String content = process();
            onConfirmResult(content);
            finish(new ActionResult(TransResult.SUCC, content));
        } else if (v.getId() == R.id.scan_code_btn) {
            finish(new ActionResult(TransResult.SUCC, AppConstants.SALE_TYPE_SCAN));
        } else {
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        }
        return;
    }

    private void onConfirmResult(String content) {
        if (EInputType.NUM == inputType && minLen > 0 && (content == null || content.isEmpty())) {
            ToastUtils.showMessage(R.string.please_input_again);
        }
    }

    /**
     * 输入数值检查
     */
    private String process() {
        String content;
        if (inputType == EInputType.NUM) {
            content = mEditText.getText().toString().trim();
        } else {
            content = editText.getText().toString().trim();
        }
        if (content.isEmpty()) {
            return null;
        }
        if (content.length() >= minLen && content.length() <= maxLen) {
            if (isPaddingZero) {
                content = Component.getPaddedString(content, maxLen, '0');
            }
        } else {
            return null;
        }
        return content;
    }

    private void confirmBtnChange() {
        String content;
        if (inputType == EInputType.NUM) {
            content = mEditText.getText().toString();
        } else {
            content = editText.getText().toString();
        }
        if(content.length() < minLen){
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.greycancelbtn);
        }else {
            confirmBtn.setEnabled(true);
            confirmBtn.setBackgroundResource(R.drawable.confirmbtn);
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}