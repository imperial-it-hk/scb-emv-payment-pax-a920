/*
 *
 *  ============================================================================
 *  PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *  This software is supplied under the terms of a license agreement or nondisclosure
 *  agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *  disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2019 -? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *  Description:
 *  Revision History:
 *  Date	             Author	                Action
 *  20190418   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.inflater;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.device.Device;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.TextValueWatcher;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.settings.ConfigSecondActivity;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.SysParam;
import com.evp.view.keyboard.KeyboardUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.pax.dal.exceptions.PedDevException;

import org.greenrobot.eventbus.EventBus;

/**
 * The type Config input inflater.
 *
 * @author ligq
 * @date 2019 /4/18 16:54
 */
public class ConfigInputInflater implements ConfigInflater<ConfigThirdActivity> {
    private TextInputEditText tieInput;
    private int inputType;
    private String title = "";
    private Object any = null;
    private String name = "";
    private boolean rawInputType = false;

    private String key;
    private int maxLen;
    private int digitsId;

    /**
     * Instantiates a new Config input inflater.
     *
     * @param key      the key
     * @param maxLen   the max len
     * @param digitsId the digits id
     */
    public ConfigInputInflater(String key, int maxLen, int digitsId) {
        this.key = key;
        this.maxLen = maxLen;
        this.digitsId = digitsId;
        if (digitsId == R.string.digits_2 || digitsId == R.string.digits_3) {
            inputType = InputType.TYPE_CLASS_NUMBER;
        } else {
            inputType = -1;
        }
    }

    /**
     * Instantiates a new Config input inflater.
     *
     * @param key       the key
     * @param maxLen    the max len
     * @param digitsId  the digits id
     * @param inputType the input type
     */
    public ConfigInputInflater(String key, int maxLen, int digitsId, int inputType) {
        this(key, maxLen, digitsId);
        this.inputType = inputType;
    }

    /**
     * Instantiates a new Config input inflater.
     *
     * @param key          the key
     * @param name         the name
     * @param inputType    the input type
     * @param maxLen       the max len
     * @param digitsId     the digits id
     * @param rawInputType the raw input type
     */
    public ConfigInputInflater(String key, String name, int inputType, int maxLen, int digitsId, boolean rawInputType) {
        this(key, maxLen, digitsId);
        this.name = name;
        this.inputType = inputType;
        this.rawInputType = rawInputType;
    }

    @Override
    public void inflate(final ConfigThirdActivity act, String title) {
        this.title = title;
        ViewStub viewStub = act.findViewById(R.id.vs_third_content);
        viewStub.setLayoutResource(R.layout.layout_config_input);
        viewStub.inflate();

        ViewStub vsInput = act.findViewById(R.id.vs_config_input);
        vsInput.setLayoutResource(R.layout.layout_config_type_input);
        vsInput.inflate();

        tieInput = act.findViewById(R.id.tie_config_input_content);
        if (inputType != -1) {
            if (rawInputType) {
                tieInput.setRawInputType(inputType);
            } else {
                tieInput.setInputType(inputType);
            }
        }
        setDigits();
        tieInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        initEditText(act, title);
        tieInput.setText(getInitValue(key, title));
    }

    @SuppressLint("DefaultLocale")
    private String getInitValue(String key, String title) {
        if (SettingConst.TYPE_ISSUER.equals(key)) {
            Issuer issuer = FinancialApplication.getAcqManager().findIssuer(name);
            any = issuer;
            if (Utils.getString(R.string.issuer_adjust_percent).equals(title)) {
                return String.format("%.2f", issuer.getAdjustPercent());
            } else {
                return "";
            }
        } else if (SettingConst.TYPE_ACQUIRER.equals(key)) {
            Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(name);
            any = acquirer;
            if (Utils.getString(R.string.acq_terminal_id).equals(title)) {
                return acquirer.getTerminalId();
            } else if (Utils.getString(R.string.acq_merchant_id).equals(title)) {
                return acquirer.getMerchantId();
            } else if (Utils.getString(R.string.acq_nii).equals(title)) {
                return acquirer.getNii();
            } else if (Utils.getString(R.string.acq_batch_no).equals(title)) {
                return String.valueOf(acquirer.getCurrBatchNo());
            } else if (Utils.getString(R.string.acq_ip).equals(title)) {
                return acquirer.getIp();
            } else if (Utils.getString(R.string.acq_port).equals(title)) {
                return String.valueOf(acquirer.getPort());
            } else if (Utils.getString(R.string.acq_tle_version).equals(title)) {
                return String.valueOf(acquirer.getTleVersion());
            } else if (Utils.getString(R.string.acq_tle_nii).equals(title)) {
                return String.valueOf(acquirer.getTleNii());
            } else if (Utils.getString(R.string.acq_tle_kms_nii).equals(title)) {
                return String.valueOf(acquirer.getTleKmsNii());
            } else if (Utils.getString(R.string.acq_tle_vendor_id).equals(title)) {
                return String.valueOf(acquirer.getTleVendorId());
            } else if (Utils.getString(R.string.acq_tle_te_id).equals(title)) {
                return String.valueOf(acquirer.getTleTeId());
            } else if (Utils.getString(R.string.acq_tle_te_pin).equals(title)) {
                return String.valueOf(acquirer.getTleTePin());
            } else if (Utils.getString(R.string.acq_tle_acqid).equals(title)) {
                return String.valueOf(acquirer.getTleAcquirerlId());
            } else if (Utils.getString(R.string.acq_tle_sensitive_fields).equals(title)) {
                return String.valueOf(acquirer.getTleSensitiveFields());
            } else if (Utils.getString(R.string.alipay_terminal_id).equals(title)) {
                return String.valueOf(acquirer.getAlipayTerminalId());
            } else if (Utils.getString(R.string.alipay_merchant_id).equals(title)) {
                return String.valueOf(acquirer.getAlipayMerchantId());
            } else if (Utils.getString(R.string.alipay_acquirer).equals(title)) {
                return String.valueOf(acquirer.getAlipayAcquirer());
            } else if (Utils.getString(R.string.wechat_terminal_id).equals(title)) {
                return String.valueOf(acquirer.getWechatTerminalId());
            } else if (Utils.getString(R.string.wechat_merchant_id).equals(title)) {
                return String.valueOf(acquirer.getWechatMerchantId());
            } else if (Utils.getString(R.string.wechat_acquirer).equals(title)) {
                return String.valueOf(acquirer.getWechatAcquirer());
            } else if (Utils.getString(R.string.tag30_terminal_id).equals(title)) {
                return String.valueOf(acquirer.getTag30TerminalId());
            } else if (Utils.getString(R.string.tag30_merchant_id).equals(title)) {
                return String.valueOf(acquirer.getTag30MerchantId());
            } else if (Utils.getString(R.string.tag30_biller_id).equals(title)) {
                return String.valueOf(acquirer.getTag30BillerId());
            } else if (Utils.getString(R.string.tag30_merchant_name).equals(title)) {
                return String.valueOf(acquirer.getTag30MerchantName());
            } else if (Utils.getString(R.string.tag30_partner_code).equals(title)) {
                return String.valueOf(acquirer.getTag30PartnerCode());
            } else if (Utils.getString(R.string.qrcs_terminal_id).equals(title)) {
                return String.valueOf(acquirer.getQrcsTerminalId());
            } else if (Utils.getString(R.string.qrcs_merchant_id).equals(title)) {
                return String.valueOf(acquirer.getQrcsMerchantId());
            } else if (Utils.getString(R.string.qrcs_partner_code).equals(title)) {
                return String.valueOf(acquirer.getQrcsPartnerCode());
            } else if (Utils.getString(R.string.inquiry_timeout).equals(title)) {
                return String.valueOf(acquirer.getInquiryTimeout());
            } else if (Utils.getString(R.string.inquiry_retries).equals(title)) {
                return String.valueOf(acquirer.getInquiryRetries());
            }
        }
        return "";

    }

    private void initEditText(final ConfigThirdActivity act, String title) {
        if (Utils.getString(R.string.acq_port).equals(title)) {
            TextValueWatcher textValueWatcher = new TextValueWatcher<>(0, 65535);
            textValueWatcher.setOnCompareListener(new TextValueWatcher.OnCompareListener() {
                @Override
                public boolean onCompare(String value, Object min, Object max) {
                    int temp = Integer.parseInt(value);
                    return temp >= (int) min && temp <= (int) max;
                }
            });
            textValueWatcher.setOnTextChangedListener(new TextValueWatcher.OnTextChangedListener() {
                @Override
                public void afterTextChanged(String value) {
                    ((Acquirer) any).setPort(Integer.parseInt(value));
                }
            });
            tieInput.addTextChangedListener(textValueWatcher);
            tieInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                            KeyboardUtils.hideSystemKeyboard(tieInput);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            });
        } else if (Utils.getString(R.string.acq_ip).equals(title)) {
            tieInput.addTextChangedListener(new Watcher(R.string.acq_ip));
        } else if (Utils.getString(R.string.acq_batch_no).equals(title)) {
            tieInput.addTextChangedListener(new Watcher(R.string.acq_batch_no));
        } else if (Utils.getString(R.string.issuer_adjust_percent).equals(title)) {
            TextValueWatcher textValueWatcher = new TextValueWatcher<>(0.0f, 100.0f);
            textValueWatcher.setOnCompareListener(new TextValueWatcher.OnCompareListener() {
                @Override
                public boolean onCompare(String value, Object min, Object max) {
                    float temp = Float.parseFloat(value);
                    return temp >= (float) min && temp <= (float) max;
                }
            });
            textValueWatcher.setOnTextChangedListener(new TextValueWatcher.OnTextChangedListener() {
                @Override
                public void afterTextChanged(String value) {
                    ((Issuer) any).setAdjustPercent((Float.parseFloat(value)));
                }
            });
            tieInput.addTextChangedListener(textValueWatcher);
        }
    }

    private void setDigits() {
        if (digitsId != 0) {
            tieInput.addTextChangedListener(new TextWatcher() {
                String temp = "";
                String digits = Utils.getString(digitsId);
                boolean errorInput = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s.toString();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String s1 = s.toString();
                    if (s1.equals(temp)) {
                        errorInput = false;
                        return;
                    }

                    if (TextUtils.isEmpty(s)) {
                        temp = "";
                        errorInput = false;
                    }

                    for (int i = 0; i < s1.length(); i++) {
                        if (!digits.contains(String.valueOf(s1.charAt(i)))) {
                            errorInput = true;
                            return;
                        }
                    }
                    temp = s1;
                    errorInput = false;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (errorInput) {
                        tieInput.setText(temp);
                    }
                }
            });
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public boolean doNextSuccess() {
        String input = tieInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            tieInput.setError(Utils.getString(R.string.err_config_empty_input));
            return false;
        }
        if (Utils.getString(R.string.MK_VALUE).equals(key)) {
            return injectKey(SettingConst.TYPE_TMK);
        } else if (Utils.getString(R.string.PK_VALUE).equals(key)) {
            return injectKey(SettingConst.TYPE_TPK);
        } else if (Utils.getString(R.string.AK_VALUE).equals(key)) {
            return injectKey(SettingConst.TYPE_TAK);
        } else if (SettingConst.TYPE_ISSUER.equals(key)) {
            Issuer issuer = (Issuer) any;
            boolean result = false;
            if (Utils.getString(R.string.issuer_adjust_percent).equals(title)) {
                issuer.setAdjustPercent(Float.parseFloat(String.format("%.2f", Float.parseFloat(input))));
                result = true;
            }
            if (result) {
                FinancialApplication.getAcqManager().updateIssuer(issuer);
                EventBus.getDefault().post(new ConfigSecondActivity.ConfigEvent(SettingConst.TYPE_ISSUER, issuer.getName(), null));
            }
            return result;
        } else if (SettingConst.TYPE_ACQUIRER.equals(key)) {
            Acquirer acquirer = (Acquirer) any;
            boolean result = true;
            if (Utils.getString(R.string.acq_terminal_id).equals(title)) {
                acquirer.setTerminalId(input);
            } else if (Utils.getString(R.string.acq_merchant_id).equals(title)) {
                acquirer.setMerchantId(input);
            } else if (Utils.getString(R.string.acq_nii).equals(title)) {
                acquirer.setNii(input);
            } else if (Utils.getString(R.string.acq_batch_no).equals(title)) {
                if (!GreendaoHelper.getTransDataHelper().findAllTransData(acquirer).isEmpty()) {
                    ToastUtils.showMessage(R.string.has_trans_for_settle);
                } else {
                    acquirer.setCurrBatchNo(Integer.parseInt(input));
                }
            } else if (Utils.getString(R.string.acq_ip).equals(title)) {
                if (Utils.checkIp(input)) {
                    acquirer.setIp(input);
                }
            } else if (Utils.getString(R.string.acq_port).equals(title)) {
                acquirer.setPort(Integer.parseInt(input));
            } else if (Utils.getString(R.string.acq_tle_version).equals(title)) {
                acquirer.setTleVersion(input);
            } else if (Utils.getString(R.string.acq_tle_nii).equals(title)) {
                acquirer.setTleNii(input);
            } else if (Utils.getString(R.string.acq_tle_kms_nii).equals(title)) {
                acquirer.setTleKmsNii(input);
            } else if (Utils.getString(R.string.acq_tle_vendor_id).equals(title)) {
                acquirer.setTleVendorId(input);
            } else if (Utils.getString(R.string.acq_tle_te_id).equals(title)) {
                acquirer.setTleTeId(input);
            } else if (Utils.getString(R.string.acq_tle_te_pin).equals(title)) {
                acquirer.setTleTePin(input);
            } else if (Utils.getString(R.string.acq_tle_acqid).equals(title)) {
                acquirer.setTleAcquirerlId(input);
            } else if (Utils.getString(R.string.acq_tle_sensitive_fields).equals(title)) {
                acquirer.setTleSensitiveFields(input);
            } else if (Utils.getString(R.string.alipay_terminal_id).equals(title)) {
                acquirer.setAlipayTerminalId(input);
            } else if (Utils.getString(R.string.alipay_merchant_id).equals(title)) {
                acquirer.setAlipayMerchantId(input);
            } else if (Utils.getString(R.string.alipay_acquirer).equals(title)) {
                acquirer.setAlipayAcquirer(input);
            } else if (Utils.getString(R.string.wechat_terminal_id).equals(title)) {
                acquirer.setWechatTerminalId(input);
            } else if (Utils.getString(R.string.wechat_merchant_id).equals(title)) {
                acquirer.setWechatMerchantId(input);
            } else if (Utils.getString(R.string.wechat_acquirer).equals(title)) {
                acquirer.setWechatAcquirer(input);
            } else if (Utils.getString(R.string.tag30_terminal_id).equals(title)) {
                acquirer.setTag30TerminalId(input);
            } else if (Utils.getString(R.string.tag30_merchant_id).equals(title)) {
                acquirer.setTag30MerchantId(input);
            } else if (Utils.getString(R.string.tag30_biller_id).equals(title)) {
                acquirer.setTag30BillerId(input);
            } else if (Utils.getString(R.string.tag30_merchant_name).equals(title)) {
                acquirer.setTag30MerchantName(input);
            } else if (Utils.getString(R.string.tag30_partner_code).equals(title)) {
                acquirer.setTag30PartnerCode(input);
            } else if (Utils.getString(R.string.qrcs_terminal_id).equals(title)) {
                acquirer.setQrcsTerminalId(input);
            } else if (Utils.getString(R.string.qrcs_merchant_id).equals(title)) {
                acquirer.setQrcsMerchantId(input);
            } else if (Utils.getString(R.string.qrcs_partner_code).equals(title)) {
                acquirer.setQrcsPartnerCode(input);
            } else if (Utils.getString(R.string.inquiry_timeout).equals(title)) {
                acquirer.setInquiryTimeout(Integer.parseInt(input));
            } else if (Utils.getString(R.string.inquiry_retries).equals(title)) {
                acquirer.setInquiryRetries(Integer.parseInt(input));
            } else {
                result = false;
            }
            if (result) {
                FinancialApplication.getAcqManager().updateAcquirer(acquirer);
                EventBus.getDefault().post(new ConfigSecondActivity.ConfigEvent(SettingConst.TYPE_ACQUIRER, acquirer.getName(),null));
            }
            return result;
        } else if (Utils.getString(R.string.COMM_TIMEOUT).equals(key) || Utils.getString(R.string.EDC_TRACE_NO).equals(key)
                || Utils.getString(R.string.EDC_SMTP_SSL_PORT).equals(key) || Utils.getString(R.string.EDC_SMTP_PORT).equals(key)
                || Utils.getString(R.string.MK_INDEX).equals(key) || Utils.getString(R.string.MK_INDEX_MANUAL).equals(key)
                || Utils.getString(R.string.EDC_RECEIPT_NUM).equals(key) || Utils.getString(R.string.EDC_STAN_NO).equals(key)) {
            SysParam.getInstance().set(key, Integer.parseInt(input));
            return true;
        } else {
            SysParam.getInstance().set(key, input);
            return true;
        }
    }

    private boolean injectKey(String type) {
        String input = tieInput.getText().toString();
        if (input.length() != maxLen) {
            tieInput.setError(Utils.getString(R.string.input_len_err));
            return false;
        }
        writeKey(type, input);
        return true;
    }

    private void writeKey(String type, String input) {
        try {
            switch (type) {
                case SettingConst.TYPE_TMK:
                    PedHelper.writeTMK(KeyUtils.getTmkIndex(Utils.getString(R.string.SET_1)), ConvertHelper.getConvert().strToBcdPaddingLeft(input));
                    break;
                case SettingConst.TYPE_TPK:
                    PedHelper.writeTPK(KeyUtils.getTmkIndex(Utils.getString(R.string.SET_1)), KeyUtils.getTpkIndex(Utils.getString(R.string.SET_1)), ConvertHelper.getConvert().strToBcdPaddingLeft(input), null);
                    break;
                case SettingConst.TYPE_TAK:
                    PedHelper.writeTAK(KeyUtils.getTmkIndex(Utils.getString(R.string.SET_1)), KeyUtils.getTakIndex(Utils.getString(R.string.SET_1)), ConvertHelper.getConvert().strToBcdPaddingLeft(input), null);
                    break;
                default:
                    break;
            }
            Device.beepOk();
            ToastUtils.showMessage(R.string.set_key_success);
        } catch (PedDevException e) {
            Device.beepErr();
            ToastUtils.showMessage(Utils.getString(R.string.set_key_fail) + ":${e.message}");
        }
    }

    private class Watcher implements TextWatcher {
        private int id;

        /**
         * Instantiates a new Watcher.
         *
         * @param id the id
         */
        Watcher(int id) {
            this.id = id;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String content = s.toString();
            Acquirer acquirer = (Acquirer) any;
            switch (id) {
                case R.string.acq_terminal_id:
                    acquirer.setTerminalId(content);
                    break;
                case R.string.acq_merchant_id:
                    acquirer.setMerchantId(content);
                    break;
                case R.string.acq_nii:
                    acquirer.setNii(content);
                    break;
                case R.string.acq_batch_no:
                    if (content.isEmpty()){
                        return;
                    }
                    updateBatchNo(content, acquirer);
                    break;
                case R.string.acq_ip:
                    if (Utils.checkIp(content)) {
                        acquirer.setIp(content);
                    }
                    break;
                default:
                    break;
            }
        }

        private void updateBatchNo(String content, Acquirer acquirer) {
            if (!GreendaoHelper.getTransDataHelper().findAllTransData(acquirer).isEmpty()) {
                ToastUtils.showMessage(R.string.has_trans_for_settle);
            } else {
                acquirer.setCurrBatchNo(Integer.parseInt(content));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            //do nothing
        }
    }
}
