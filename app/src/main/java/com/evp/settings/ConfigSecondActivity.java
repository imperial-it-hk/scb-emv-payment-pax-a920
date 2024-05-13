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
 * 20190108  	         ligq                    Create
 * ===========================================================================================
 */
package com.evp.settings;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.BaseConfigActivity;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.model.AcqManager;
import com.evp.payment.evpscb.R;
import com.evp.settings.adapter.ConfigSecondAdapter;
import com.evp.settings.inflater.InflaterUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Config second activity.
 */
public class ConfigSecondActivity extends BaseConfigActivity {
    private ConfigSecondAdapter adapter = null;
    private boolean needRefresh = false;
    private String type = "";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config_second;
    }

    @Override
    protected void initViews() {
        EventBus.getDefault().register(this);
        super.initViews();
        final List<ConfigSecondAdapter.ItemConfigSecond> dataList = new ArrayList<>();
        InflaterUtils.initData(dataList, new InflaterUtils.InflaterListener<List<ConfigSecondAdapter.ItemConfigSecond>>() {
            @Override
            public boolean init() {
                boolean result = getTypeResult();
                initDataList(dataList);
                return result;
            }

            @Override
            public void next(List<ConfigSecondAdapter.ItemConfigSecond> itemConfigSeconds) {
                RecyclerView rvData = findViewById(R.id.rv_config);
                rvData.setLayoutManager(new LinearLayoutManager((Context) ConfigSecondActivity.this));
                adapter = new ConfigSecondAdapter(ConfigSecondActivity.this, itemConfigSeconds, type);
                rvData.setAdapter(adapter);

            }
        });

    }

    private boolean getTypeResult() {
        boolean result = true;
        String title = toolbar.getTitle().toString();
        if (getString(R.string.settings_menu_communication_parameter).equals(title)) {
            type = SettingConst.TYPE_COMM;
        } else if (getString(R.string.settings_menu_edc_parameter).equals(title)) {
            type = SettingConst.TYPE_EDC;
        } else if (getString(R.string.settings_menu_issuer_parameter).equals(title)) {
            type = SettingConst.TYPE_ISSUER;
        } else if (getString(R.string.settings_menu_acquirer_parameter).equals(title)) {
            type = SettingConst.TYPE_ACQUIRER;
        } else if (getString(R.string.settings_menu_otherManage).equals(title)) {
            type = SettingConst.TYPE_OTHER;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * init the page
     *
     * @param dataList data to be init
     */
    private void initDataList(List<ConfigSecondAdapter.ItemConfigSecond> dataList) {
        String[] titleArr;
        int[] switchArr;
        int[] valueArr;
        String[] keyArr;
        String defaultKey = SysParam.KEY_NONE;
        switch (type) {
            case SettingConst.TYPE_COMM:
                titleArr = getResources().getStringArray(R.array.config_menu_comm_title);
                switchArr = getResources().getIntArray(R.array.config_menu_comm_type_switch);
                valueArr = getResources().getIntArray(R.array.config_menu_comm_has_value);
                keyArr = getResources().getStringArray(R.array.config_menu_comm_key);
                break;
            case SettingConst.TYPE_EDC:
                titleArr = getResources().getStringArray(R.array.config_menu_edc_title);
                switchArr = getResources().getIntArray(R.array.config_menu_edc_type_switch);
                valueArr = getResources().getIntArray(R.array.config_menu_edc_has_value);
                keyArr = getResources().getStringArray(R.array.config_menu_edc_key);
                break;
            case SettingConst.TYPE_ISSUER:
                defaultKey = SettingConst.TYPE_ISSUER;
                titleArr = getResources().getStringArray(R.array.config_menu_issuer_title);
                switchArr = getResources().getIntArray(R.array.config_menu_issuer_type_switch);
                valueArr = getResources().getIntArray(R.array.config_menu_issuer_has_value);
                keyArr = null;
                break;
            case SettingConst.TYPE_ACQUIRER:
                defaultKey = SettingConst.TYPE_ACQUIRER;
                titleArr = getResources().getStringArray(R.array.config_menu_acquirer_title);
                switchArr = getResources().getIntArray(R.array.config_menu_acquirer_type_switch);
                valueArr = getResources().getIntArray(R.array.config_menu_acquirer_has_value);
                keyArr = null;
                break;
            default:
                titleArr = new String[0];
                switchArr = new int[0];
                valueArr = new int[0];
                keyArr = new String[0];
                break;
        }

        if (titleArr.length == 0) {
            return;
        }
        for (int i = 0; i < titleArr.length; i++) {
            dataList.add(new ConfigSecondAdapter.ItemConfigSecond(titleArr[i],
                    switchArr[i] == 1, valueArr[i], (keyArr == null) ? defaultKey : keyArr[i]));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needRefresh || adapter != null) {
            adapter.notifyDataSetChanged();
        }
        needRefresh = (!SettingConst.TYPE_ISSUER.equals(type) && !SettingConst.TYPE_ACQUIRER.equals(type));
    }

    /**
     * handle the acquirer and issuer configs
     *
     * @param received event data
     */
    @Subscribe
    public void handleSelectChanged(ConfigEvent received) {
        LogUtils.d(TAG, "handleSelectChanged:$received");
        AcqManager acqManager = FinancialApplication.getAcqManager();
        if (SettingConst.TYPE_ISSUER.equals(received.type)) {
            Issuer findIssuer = acqManager.findIssuer(received.msg);
            if (findIssuer != null) {
                adapter.setIssuer(findIssuer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } else if (SettingConst.TYPE_ACQUIRER.equals(received.type)) {
            Acquirer findAcquirer = acqManager.findAcquirer(received.msg);
            if (findAcquirer != null) {
                adapter.setAcquirer(findAcquirer);
                Long curAcqId = FinancialApplication.getAcqManager().getCurAcq().getId();
                if (findAcquirer.getId().equals(curAcqId)) {
                    FinancialApplication.getAcqManager().setCurAcq(findAcquirer);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } else if (getString(R.string.SSL).equals(received.type)) {
            Acquirer findAcquirer = acqManager.findAcquirer((String) received.data);
            if (findAcquirer != null) {
                findAcquirer.setSslType((getString(R.string.SSL).equals(received.msg)) ? SysParam.CommSslType.SSL.name() : SysParam.CommSslType.NO_SSL.name());
                acqManager.updateAcquirer(findAcquirer);
                adapter.setAcquirer(findAcquirer);
                FinancialApplication.getAcqManager().setCurAcq(findAcquirer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } else if (getString(R.string.acq_tle_key_set_id).equals(received.type)) {
            Acquirer findAcquirer = acqManager.findAcquirer((String) received.data);
            if (findAcquirer != null) {
                findAcquirer.setTleKeySetId(received.msg);
                acqManager.updateAcquirer(findAcquirer);
                adapter.setAcquirer(findAcquirer);
                FinancialApplication.getAcqManager().setCurAcq(findAcquirer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * The type Config event.
     */
    public static final class ConfigEvent {
        private final String type;
        private final String msg;
        private final Object data;

        /**
         * Gets type.
         *
         * @return the type
         */
        public final String getType() {
            return this.type;
        }

        /**
         * Gets msg.
         *
         * @return the msg
         */
        public final String getMsg() {
            return this.msg;
        }

        /**
         * Gets data.
         *
         * @return the data
         */
        public final Object getData() {
            return this.data;
        }

        /**
         * Instantiates a new Config event.
         *
         * @param type the type
         * @param msg  the msg
         * @param data the data
         */
        public ConfigEvent(String type, String msg, Object data) {
            super();
            this.type = type;
            this.msg = msg;
            this.data = data;
        }

    }
}
