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

package com.evp.settings.adapter;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.evp.adapter.CommonAdapter;
import com.evp.adapter.ViewHolder;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.paxprinter.IPrintService;
import com.evp.paxprinter.constant.Constant;
import com.evp.paxprinter.impl.APrintBluetoothService;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.RxUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.SysParam;
import com.evp.view.dialog.DialogUtils;
import com.sankuai.waimai.router.Router;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * The type Config second adapter.
 *
 * @author ligq
 * @date 2019 /4/18 9:43
 */
public class ConfigSecondAdapter extends CommonAdapter<ConfigSecondAdapter.ItemConfigSecond> {
    private Issuer issuer;
    private Acquirer acquirer;
    private boolean enableItem = true;
    private String type = "";
    private String[] enableItemArr = {Utils.getString(R.string.commParam_menu_mobile_username),
            Utils.getString(R.string.commParam_menu_mobile_user_password),
            Utils.getString(R.string.edc_smtp_host_name),
            Utils.getString(R.string.edc_smtp_port),
            Utils.getString(R.string.edc_smtp_username),
            Utils.getString(R.string.edc_smtp_password),
            Utils.getString(R.string.edc_smtp_enable_ssl),
            Utils.getString(R.string.edc_smtp_ssl_port),
            Utils.getString(R.string.edc_smtp_from)};

    /**
     * Instantiates a new Config second adapter.
     *
     * @param context  the context
     * @param layoutId the layout id
     * @param dataList the data list
     */
    public ConfigSecondAdapter(@NotNull Context context, int layoutId, @NotNull List<ItemConfigSecond> dataList) {
        super(context, layoutId, dataList);
    }

    /**
     * Instantiates a new Config second adapter.
     *
     * @param context  the context
     * @param dataList the data list
     */
    public ConfigSecondAdapter(Context context, List<ItemConfigSecond> dataList) {
        this(context, R.layout.item_config_second, dataList);
    }

    /**
     * Instantiates a new Config second adapter.
     *
     * @param context  the context
     * @param dataList the data list
     * @param type     the type
     */
    public ConfigSecondAdapter(Context context, List<ItemConfigSecond> dataList, String type) {
        this(context, dataList);
        this.type = type;
    }

    @Override
    public void onViewHolderCreated(@NotNull ViewHolder holder, @NotNull View itemView) {
        switch (type) {
            case SettingConst.TYPE_COMM:
                enableItem = SysParam.getInstance().getBoolean(R.string.MOBILE_NEED_USER);
                break;
            case SettingConst.TYPE_EDC:
                enableItem = SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_PAPERLESS);
                break;
            case SettingConst.TYPE_ISSUER:
                if (issuer == null) {
                    issuer = FinancialApplication.getAcqManager().findAllIssuers().get(0);
                }
                break;
            case SettingConst.TYPE_ACQUIRER:
                if (acquirer == null) {
                    acquirer = FinancialApplication.getAcqManager().findAllAcquirers().get(0);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void convert(@NotNull final ViewHolder holder, final ItemConfigSecond itemConfigSecond, int position) {
        initAdapter(holder, itemConfigSecond);
        holder.setText(R.id.tv_config_item_title, itemConfigSecond.title);
        TextView content = holder.getView(R.id.tv_config_item_value);
        if (itemConfigSecond.getValueType() != 0) {
            String value = getStringValue(itemConfigSecond);
            if (!TextUtils.isEmpty(value)) {
                content.setVisibility(View.VISIBLE);
                switch (itemConfigSecond.getValueType()) {
                    case 1:
                        content.setText(value);
                        break;
                    case 2:
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < value.length(); i++) {
                            sb.append("*");
                        }
                        content.setText(sb.toString());
                        break;
                    default:
                        break;
                }
            } else {
                content.setVisibility(View.GONE);
            }
        } else {
            content.setVisibility(View.GONE);
        }
        final Switch swit = holder.getView(R.id.switch_config_item_right);
        if (itemConfigSecond.isSwitch()) {
            swit.setVisibility(View.VISIBLE);
            holder.setVisible(R.id.iv_config_item_arrow, false);
            swit.setChecked(getSwitchValue(itemConfigSecond));
            swit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    TextView view = holder.getView(R.id.tv_config_item_title);
                    if (itemConfigSecond.getTitle().equals(view.getText().toString())) {
                        setSwitchValue(itemConfigSecond, isChecked);
                        onCheckChanged(itemConfigSecond);
                    }
                }
            });
        } else {
            holder.setVisible(R.id.iv_config_item_arrow, true);
            swit.setVisibility(View.INVISIBLE);
        }

        holder.setOnClickListener(R.id.cl_config_second_item, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemConfigSecond.isSwitch()) {
                    swit.setChecked(!swit.isChecked());
                } else if (SettingConst.KEY_NONE.equals(itemConfigSecond.key)) {
                    onKeyClick(itemConfigSecond);
                } else if (SettingConst.TYPE_ISSUER.equals(itemConfigSecond.key)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(SettingConst.INTENT_TITLE, itemConfigSecond.title);
                    bundle.putString(EUIParamKeys.ISSUER_NAME.toString(), issuer.getName());
                    Utils.jumpActivity(mContext, ConfigThirdActivity.class, bundle);
                } else if (SettingConst.TYPE_ACQUIRER.equals(itemConfigSecond.key)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(SettingConst.INTENT_TITLE, itemConfigSecond.title);
                    bundle.putString(EUIParamKeys.ACQUIRER_NAME.toString(), acquirer.getName());
                    Utils.jumpActivity(mContext, ConfigThirdActivity.class, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(SettingConst.INTENT_TITLE, itemConfigSecond.getTitle());
                    Utils.jumpActivity(mContext, ConfigThirdActivity.class, bundle);
                }
            }
        });
    }

    private void onKeyClick(ItemConfigSecond itemConfigSecond) {
        String title = itemConfigSecond.getTitle();
        if (Utils.getString(R.string.open_wifi).equals(title)) {
            Utils.callSystemSettings(mContext, Settings.ACTION_WIFI_SETTINGS);
        }else if (Utils.getString(R.string.reset_bp60a).equals(title)){
            IPrintService preService = Router.getService(IPrintService.class, Constant.PRINT_BUILD_BE_C2);
            if (preService != null){
                APrintBluetoothService printBluetoothService = (APrintBluetoothService) preService;
                printBluetoothService.clearPreviousMac(FinancialApplication.getApp());
                printBluetoothService.disConnect();
            }
            DialogUtils.showMessage(mContext,mContext.getString(R.string.dialog_reset_bp60a),null, Constants.SUCCESS_DIALOG_SHOW_TIME);
        }
    }

    private void onCheckChanged(ItemConfigSecond itemConfigSecond) {
        String title = itemConfigSecond.getTitle();
        if (Utils.getString(R.string.commParam_menu_mobile_need_user).equals(title)) {
            enableItem = SysParam.getInstance().getBoolean(R.string.MOBILE_NEED_USER);
        } else if (Utils.getString(R.string.edc_enable_paperless).equals(title)) {
            enableItem = SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_PAPERLESS);
        } else {
            enableItem = true;
        }
        RxUtils.addDisposable(Observable.timer(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        notifyDataSetChanged();
                    }
                }));
    }

    private void setSwitchValue(ItemConfigSecond itemConfigSecond, boolean isChecked) {
        String title = itemConfigSecond.getTitle();
        if (Utils.getString(R.string.issuer_enable_adjust).equals(title)) {
            issuer.setEnableAdjust(isChecked);
        } else if (Utils.getString(R.string.issuer_enable_offline).equals(title)) {
            issuer.setEnableOffline(isChecked);
        } else if (Utils.getString(R.string.issuer_enable_refund).equals(title)) {
            issuer.setIsEnableRefund(isChecked);
        } else if (Utils.getString(R.string.issuer_enable_expiry).equals(title)) {
            issuer.setAllowExpiry(isChecked);
        } else if (Utils.getString(R.string.issuer_enable_manualPan).equals(title)) {
            issuer.setAllowManualPan(isChecked);
        } else if (Utils.getString(R.string.issuer_check_adjust).equals(title)) {
            issuer.setAllowCheckExpiry(isChecked);
        } else if (Utils.getString(R.string.issuer_check_pan).equals(title)) {
            issuer.setAllowCheckPanMod10(isChecked);
        } else if (Utils.getString(R.string.issuer_enable_print).equals(title)) {
            issuer.setAllowPrint(isChecked);
        } else if (Utils.getString(R.string.issuer_not_emv_tran_pin_required).equals(title)) {
            issuer.setNonEmvTranRequirePIN(isChecked);
        } else if (Utils.getString(R.string.acq_tle_enabled).equals(title)){
            if (acquirer != null){
                acquirer.setTleEnabled(isChecked);
            }
        } else {
            SysParam.getInstance().set(itemConfigSecond.getKey(), isChecked);
        }

        if (issuer != null) {
            FinancialApplication.getAcqManager().updateIssuer(issuer);
        }

        if (acquirer != null) {
            FinancialApplication.getAcqManager().updateAcquirer(acquirer);
        }
    }

    private boolean getSwitchValue(ItemConfigSecond itemConfigSecond) {
        String title = itemConfigSecond.getTitle();
        if (Utils.getString(R.string.issuer_enable_adjust).equals(title)) {
            return issuer.isEnableAdjust();
        } else if (Utils.getString(R.string.issuer_enable_offline).equals(title)) {
            return issuer.getIsEnableOffline();
        } else if (Utils.getString(R.string.issuer_enable_refund).equals(title)) {
            return issuer.getIsEnableRefund();
        } else if (Utils.getString(R.string.issuer_enable_expiry).equals(title)) {
            return issuer.isAllowExpiry();
        } else if (Utils.getString(R.string.issuer_enable_manualPan).equals(title)) {
            return issuer.getIsAllowManualPan();
        } else if (Utils.getString(R.string.issuer_check_adjust).equals(title)) {
            return issuer.isAllowCheckExpiry();
        } else if (Utils.getString(R.string.issuer_check_pan).equals(title)) {
            return issuer.isAllowCheckPanMod10();
        } else if (Utils.getString(R.string.issuer_enable_print).equals(title)) {
            return issuer.isAllowPrint();
        } else if (Utils.getString(R.string.issuer_not_emv_tran_pin_required).equals(title)) {
            return issuer.getIsRequirePIN();
        } else if (Utils.getString(R.string.acq_tle_enabled).equals(title)){
            return acquirer.getTleEnabled();
        }
        else {
            return SysParam.getInstance().getBoolean(itemConfigSecond.getKey());
        }
    }

    private String getStringValue(ItemConfigSecond itemConfigSecond) {
        String title = itemConfigSecond.getTitle();
        if (Utils.getString(R.string.settings_menu_issuer_parameter).equals(title)) {
            return issuer.getName();
        } else if (Utils.getString(R.string.issuer_non_emv_tran_floor_limit).equals(title)) {
            return CurrencyConverter.convert(issuer.getNonEmvTranFloorLimit());
        } else if (Utils.getString(R.string.issuer_adjust_percent).equals(title)) {
            return String.valueOf(issuer.getAdjustPercent());
        } else if (Utils.getString(R.string.settings_menu_acquirer_parameter).equals(title)) {
            return acquirer.getName();
        } else if (Utils.getString(R.string.acq_terminal_id).equals(title)) {
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
        } else if (Utils.getString(R.string.SSL).equals(title)) {
            return acquirer.getSslType();
        } else if (Utils.getString(R.string.edc_ped_mode).equals(title)) {
            return SysParam.getInstance().getString(itemConfigSecond.getKey());
        }else if (Utils.getString(R.string.edc_clss_mode).equals(title)) {
            return SysParam.getInstance().getString(itemConfigSecond.getKey());
        } else if (Utils.getString(R.string.commParam_menu_comm_timeout).equals(title)
                || Utils.getString(R.string.edc_trace_no).equals(title)
                || Utils.getString(R.string.edc_stan_no).equals(title)
                || Utils.getString(R.string.edc_smtp_ssl_port).equals(title)
                || Utils.getString(R.string.edc_smtp_port).equals(title)
                || Utils.getString(R.string.keyManage_menu_tmk_index).equals(title)
                || Utils.getString(R.string.keyManage_menu_tmk_index_no).equals(title)
                || Utils.getString(R.string.edc_receipt_no).equals(title)
        ) {
            return String.valueOf(SysParam.getInstance().getInt(itemConfigSecond.key));
        } else if (Utils.getString(R.string.acq_tle_version).equals(title)) {
            return String.valueOf(acquirer.getTleVersion());
        } else if (Utils.getString(R.string.acq_tle_nii).equals(title)) {
            return String.valueOf(acquirer.getTleNii());
        } else if (Utils.getString(R.string.acq_tle_kms_nii).equals(title)) {
            return String.valueOf(acquirer.getTleKmsNii());
        } else if (Utils.getString(R.string.acq_tle_vendor_id).equals(title)) {
            return String.valueOf(acquirer.getTleVendorId());
        } else if (Utils.getString(R.string.acq_tle_key_set_id).equals(title)) {
            return String.valueOf(acquirer.getTleKeySetId());
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
        } else {
            return SysParam.getInstance().getString(itemConfigSecond.getKey());
        }
    }

    private void initAdapter(ViewHolder holder, ItemConfigSecond itemConfigSecond) {
        String title = itemConfigSecond.getTitle();
        if (title.equals(enableItemArr[0]) || title.equals(enableItemArr[1])
                || title.equals(enableItemArr[2]) || title.equals(enableItemArr[3])
                || title.equals(enableItemArr[4]) || title.equals(enableItemArr[5])
                || title.equals(enableItemArr[6]) || title.equals(enableItemArr[8])) {
            setItemEnable(holder, enableItem);
        } else if (title.equals(enableItemArr[7])) {
            setItemEnable(holder, SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_PAPERLESS) &&
                    SysParam.getInstance().getBoolean(R.string.EDC_SMTP_ENABLE_SSL));
        } else {
            setItemEnable(holder, true);
        }
    }

    private void setItemEnable(ViewHolder holder, boolean enable) {
        holder.getView(R.id.cl_config_second_item).setEnabled(enable);
        int color;
        if (!enable) {
            color = Utils.getColor(R.color.color_config_item_value);
        } else {
            color = Utils.getColor(R.color.color_configs_title);
        }
        holder.setTextColor(R.id.tv_config_item_title, color);
    }

    /**
     * Gets issuer.
     *
     * @return the issuer
     */
    public Issuer getIssuer() {
        return issuer;
    }

    /**
     * Sets issuer.
     *
     * @param issuer the issuer
     */
    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    /**
     * Gets acquirer.
     *
     * @return the acquirer
     */
    public Acquirer getAcquirer() {
        return acquirer;
    }

    /**
     * Sets acquirer.
     *
     * @param acquirer the acquirer
     */
    public void setAcquirer(Acquirer acquirer) {
        this.acquirer = acquirer;
    }

    /**
     * The type Item config second.
     */
    public static class ItemConfigSecond {
        private String title;
        private boolean isSwitch;
        private int valueType;
        private String key;

        /**
         * Instantiates a new Item config second.
         */
        public ItemConfigSecond() {
        }

        /**
         * Instantiates a new Item config second.
         *
         * @param title     the title
         * @param isSwitch  the is switch
         * @param valueType the value type
         * @param key       the key
         */
        public ItemConfigSecond(String title, boolean isSwitch, int valueType, String key) {
            this.title = title;
            this.isSwitch = isSwitch;
            this.valueType = valueType;
            this.key = key;
        }

        /**
         * Gets title.
         *
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets title.
         *
         * @param title the title
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * Is switch boolean.
         *
         * @return the boolean
         */
        public boolean isSwitch() {
            return isSwitch;
        }

        /**
         * Sets switch.
         *
         * @param aSwitch the a switch
         */
        public void setSwitch(boolean aSwitch) {
            isSwitch = aSwitch;
        }

        /**
         * Gets value type.
         *
         * @return the value type
         */
        public int getValueType() {
            return valueType;
        }

        /**
         * Sets value type.
         *
         * @param valueType the value type
         */
        public void setValueType(int valueType) {
            this.valueType = valueType;
        }

        /**
         * Gets key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets key.
         *
         * @param key the key
         */
        public void setKey(String key) {
            this.key = key;
        }
    }
}
