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
package com.evp.pay;

import android.content.Intent;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigSecondActivity;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.SettingConst;
import com.evp.settings.adapter.ConfigFirstAdapter;
import com.evp.view.UserGuideManager;
import com.shizhefei.guide.GuideHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * all the configs for the activity please refer to below
 *
 * @author ligq
 * @date 2018 /11/29 17:59
 * @see values/arrays_edc.xml...
 */
public class ConfigFirstActivity extends BaseConfigActivity implements ConfigFirstAdapter.OnConfigItemClickListener {
    private RecyclerView rvConfigs;
    private List<ConfigFirstAdapter.ItemConfigFirst> configsList;
    private boolean isFirst = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config_first;
    }

    @Override
    protected void loadParam() {
        isFirst = FinancialApplication.getController().isFirstRun();
        configsList = new ArrayList<>();
        int titleId = isFirst ? R.array.config_menu_title_init : R.array.config_menu_title_default;
        String[] titleArr = getResources().getStringArray(titleId);
        int iconId = isFirst ? R.array.config_menu_icon_init : R.array.config_menu_icon_default;
        TypedArray itemIconArr = getResources().obtainTypedArray(iconId);
        int showLineId = isFirst ? R.array.config_menu_show_line_init : R.array.config_menu_show_line_default;
        int[] showLineArr = getResources().getIntArray(showLineId);
        int typeId = isFirst ? R.array.config_menu_type_init : R.array.config_menu_type_default;
        int[] typeArr = getResources().getIntArray(typeId);
        for (int i = 0; i < titleArr.length; i++) {
            configsList.add(new ConfigFirstAdapter.ItemConfigFirst(itemIconArr.getResourceId(i, 0),
                    titleArr[i], showLineArr[i] == 1, typeArr[i]));
        }
        itemIconArr.recycle();
    }

    @Override
    protected void initViews() {
        super.initViews();
        rvConfigs = findViewById(R.id.rv_config_first);
        rvConfigs.setLayoutManager(new LinearLayoutManager(this));
        ConfigFirstAdapter configsAdapter = new ConfigFirstAdapter(this, R.layout.item_config_first, configsList);
        configsAdapter.setOnItemClickListener(this);
        rvConfigs.setAdapter(configsAdapter);
        rvConfigs.post(new Runnable() {
            @Override
            public void run() {
                initUserGuideView();
            }
        });
    }

    private void initUserGuideView() {
        if (!UserGuideManager.getInstance().isEnabled()) {
            return;
        }

        final GuideHelper guideHelper = new GuideHelper(this);
        View commItemView = rvConfigs.getChildAt(0);

        GuideHelper.TipData tipComm = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_comm_type_jap : R.drawable.tip_comm_type, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                commItemView);
        tipComm.setViewBg(ContextCompat.getDrawable(this, R.drawable.white_bg));
        tipComm.setLocation(0, 10);

        GuideHelper.TipData tipNext = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_next_btn_jap : R.drawable.tip_next_btn_en, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tipNext.setLocation(0, -ViewUtils.dp2px(225));
        tipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideHelper.nextPage();
            }
        });

        guideHelper.addPage(false, tipComm, tipNext);

        View keyManageItemView = rvConfigs.getChildAt(1);
        GuideHelper.TipData tipInjectKey = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_inject_key_jap : R.drawable.tip_inject_key, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, keyManageItemView);
        tipInjectKey.setViewBg(ContextCompat.getDrawable(this, R.drawable.white_bg));
        tipInjectKey.setLocation(0, 10);
        GuideHelper.TipData tipOk = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_ok_btn_jap : R.drawable.tip_ok_btn_en, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tipOk.setLocation(0, -ViewUtils.dp2px(200));
        tipOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideHelper.dismiss();
            }
        });
        guideHelper.addPage(false, tipInjectKey, tipOk);

        guideHelper.show(false);
    }

    @Override
    public void onConfigsItemClick(String title, int type) {
        Intent intent = null;
        if (type == SettingConst.FUN) {
            intent = new Intent(this, ConfigThirdActivity.class);
        } else {
            intent = new Intent(this, ConfigSecondActivity.class);
        }
        intent.putExtra("title", title);
        startActivity(intent);
    }

    @Override
    protected String getToolBarTitle() {
        return getString(R.string.settings_title);
    }

    @Override
    protected void onStop() {
        super.onStop();
        quickClickProtection.stop();
    }
}
