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
 *  20190419   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.inflater;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.InjectKeyUtil;
import com.evp.pay.utils.RxUtils;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigSecondActivity;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.SysParam;
import com.evp.settings.adapter.ConfigSelectAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Config select inflater.
 *
 * @author ligq
 * @date 2019 /4/19 10:24
 */
public class ConfigSelectInflater implements ConfigInflater<ConfigThirdActivity> {
    private String name;
    private String key;
    private ConfigSelectAdapter adapter;
    private OnOkListener onOkListener;

    /**
     * Instantiates a new Config select inflater.
     */
    public ConfigSelectInflater() {
    }

    /**
     * Instantiates a new Config select inflater.
     *
     * @param name the name
     */
    public ConfigSelectInflater(String name) {
        this.name = name;
    }

    @Override
    public void inflate(final ConfigThirdActivity act, final String title) {
        final ViewStub viewStub = act.findViewById(R.id.vs_third_content);
        InflaterUtils.initData(viewStub, new InflaterUtils.InflaterListener<ViewStub>() {
            boolean result;
            int selectPosition = 0;

            @Override
            public boolean init() {
                List<ConfigSelectAdapter.ItemConfigSelect> dataList = new ArrayList<>();
                List<String> contentList;
                if (Utils.getString(R.string.settings_menu_communication_type).equals(title)) {
                    key = Utils.getString(R.string.COMM_TYPE);
                    contentList = Utils.getMutableList(R.array.commParam_menu_comm_mode_values_list_entries);
                    result = true;
                } else if (Utils.getString(R.string.commParam_menu_comm_timeout).equals(title)) {
                    key = Utils.getString(R.string.COMM_TIMEOUT);
                    contentList = Utils.getMutableList(R.array.edc_connect_time_entries);
                    result = true;
                } else if (Utils.getString(R.string.currency_list).equals(title)) {
                    key = Utils.getString(R.string.EDC_CURRENCY_LIST);
                    contentList = new ArrayList<>();
                    CurrencyConverter.getSupportedLocaleList(contentList);
                    result = true;
                } else if (Utils.getString(R.string.edc_ped_mode).equals(title)) {
                    key = Utils.getString(R.string.EDC_PED_MODE);
                    contentList = Utils.getMutableList(R.array.edc_ped_mode_entries);
                    result = true;
                } else if (Utils.getString(R.string.edc_clss_mode).equals(title)) {
                    key = Utils.getString(R.string.EDC_CLSS_MODE);
                    contentList = Utils.getMutableList(R.array.edc_clss_mode_entries);
                    result = true;
                } else if (Utils.getString(R.string.edc_printer_type).equals(title)) {
                    key = Utils.getString(R.string.EDC_PRINTER_TYPE);
                    contentList = Utils.getSupportedPrintType();
                    result = true;
                } else if (Utils.getString(R.string.settings_menu_issuer_parameter).equals(title)) {
                    key = SettingConst.TYPE_ISSUER;
                    List<Issuer> listIssuers = FinancialApplication.getAcqManager().findAllIssuers();
                    contentList = new ArrayList<>();
                    for (Issuer listIssuer : listIssuers) {
                        contentList.add(listIssuer.getName());
                    }
                    result = true;
                } else if (Utils.getString(R.string.settings_menu_acquirer_parameter).equals(title)) {
                    key = SettingConst.TYPE_ACQUIRER;
                    List<Acquirer> listAcquirers = FinancialApplication.getAcqManager().findAllAcquirers();
                    contentList = new ArrayList<>();
                    for (Acquirer listAcquirer : listAcquirers) {
                        contentList.add(listAcquirer.getName());
                    }
                    result = true;
                } else if (Utils.getString(R.string.SSL).equals(title)) {
                    key = Utils.getString(R.string.SSL);
                    contentList = Utils.getMutableList(R.array.acq_ssl_type_list_entries);
                    result = true;
                } else if (Utils.getString(R.string.acq_tle_key_set_id).equals(title)) {
                    key = Utils.getString(R.string.acq_tle_key_set_id);
                    contentList = Utils.getMutableList(R.array.acq_tls_key_set_list_entries);
                    result = true;
                } else {
                    key = "";
                    contentList = new ArrayList<>();
                    result = false;
                }
                if (contentList.isEmpty()) {
                    return false;
                }
                String selectedType = getSelectType(contentList);

                for (int i = 0; i < contentList.size(); i++) {
                    ConfigSelectAdapter.ItemConfigSelect element;
                    if (selectedType.equals(contentList.get(i))) {
                        selectPosition = i;
                        element = new ConfigSelectAdapter.ItemConfigSelect(contentList.get(i), View.VISIBLE);
                    } else {
                        element = new ConfigSelectAdapter.ItemConfigSelect(contentList.get(i));
                    }
                    dataList.add(element);
                }
                adapter = new ConfigSelectAdapter(act, R.layout.item_config_select
                        , dataList);
                adapter.setCurrentSelect(dataList.get(selectPosition));
                viewStub.setLayoutResource(R.layout.layout_config_rv);
                return result;
            }

            @Override
            public void next(ViewStub viewStub) {
                viewStub.inflate();
                RecyclerView rv = act.findViewById(R.id.rv_config);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(act);
                rv.setLayoutManager(linearLayoutManager);
                rv.setAdapter(adapter);
                rv.scrollToPosition(selectPosition);
            }
        });
    }

    private String getSelectType(List<String> contentList) {
        if (TextUtils.isEmpty(name)) {
            if (Utils.getString(R.string.COMM_TIMEOUT).equals(key)) {
                return String.valueOf(SysParam.getInstance().getInt(key));
            } else {
                return (String) SysParam.getInstance().get(key, contentList.get(0));
            }
        } else {
            if (key.equals(Utils.getString(R.string.SSL))) {
                return FinancialApplication.getAcqManager().findAcquirer(name).getSslType();
            } else if (key.equals(Utils.getString(R.string.acq_tle_key_set_id))) {
                return FinancialApplication.getAcqManager().findAcquirer(name).getTleKeySetId();
            } else {
                return name;
            }
        }
    }

    @Override
    public boolean doNextSuccess() {
        RxUtils.release();
        final String content = adapter.getCurrentSelect().getContent();
        if (content == null) {
            return true;
        }
        if (onOkListener != null) {
            onOkListener.onOkClick(key, content);
            return false;
        }
        if (!TextUtils.isEmpty(key)) {
            if (Utils.getString(R.string.SSL).equals(key)) {
                EventBus.getDefault().post(new ConfigSecondActivity.ConfigEvent(key, content, name));
            } else if (SettingConst.TYPE_ACQUIRER.equals(key) || SettingConst.TYPE_ISSUER.equals(key)) {
                EventBus.getDefault().post(new ConfigSecondActivity.ConfigEvent(key, content, null));
            } else if (Utils.getString(R.string.acq_tle_key_set_id).equals(key)) {
                EventBus.getDefault().post(new ConfigSecondActivity.ConfigEvent(key, content, name));
            } else if (Utils.getString(R.string.COMM_TIMEOUT).equals(key)) {
                SysParam.getInstance().set(key, Integer.parseInt(content));
            } else if (Utils.getString(R.string.EDC_CURRENCY_LIST).equals(key)) {
                if (GreendaoHelper.getTransDataHelper().countOf() > 0) {
                    FinancialApplication.getApp().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showMessage(R.string.has_trans_for_settle);
                        }
                    });
                    return false;
                }
                SysParam.getInstance().set(key, content);
                Utils.changeAppLanguage(FinancialApplication.getApp(),
                        CurrencyConverter.setDefCurrency(content));
                Utils.restart();
            } else if (Utils.getString(R.string.EDC_PED_MODE).equals(key)) {
                SysParam.getInstance().set(key, content);
            } else if (Utils.getString(R.string.COMM_TYPE).equals(key)) {
                SysParam.getInstance().set(key, content);
                InjectKeyUtil.injectMKSK(content);
            } else {
                SysParam.getInstance().set(key, content);
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * Sets on ok listener.
     *
     * @param onOkListener the on ok listener
     */
    public void setOnOkListener(OnOkListener onOkListener) {
        this.onOkListener = onOkListener;
    }

    /**
     * The interface On ok listener.
     */
    public interface OnOkListener {
        /**
         * On ok click.
         *
         * @param key     the key
         * @param content the content
         */
        void onOkClick(String key, String content);
    }
}
