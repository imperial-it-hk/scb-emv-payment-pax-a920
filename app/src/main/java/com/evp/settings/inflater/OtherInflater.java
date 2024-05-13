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

import android.content.res.TypedArray;
import android.view.ViewStub;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.adapter.ConfigOtherAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Other inflater.
 *
 * @author ligq
 * @date 2019 /4/19 11:21
 */
public class OtherInflater implements ConfigInflater<ConfigThirdActivity> {
    private ConfigOtherAdapter adapter;

    @Override
    public void inflate(final ConfigThirdActivity act, String title) {
        final ViewStub viewStub = act.findViewById(R.id.vs_third_content);
        InflaterUtils.initData(viewStub, new InflaterUtils.InflaterListener<ViewStub>() {
            @Override
            public boolean init() {
                viewStub.setLayoutResource(R.layout.layout_config_rv);
                List<ConfigOtherAdapter.ItemOther> dataList = new ArrayList<>();
                String[] titleArr = act.getResources().getStringArray(R.array.config_menu_other_title);
                TypedArray iconArr = act.getResources().obtainTypedArray(R.array.config_menu_other_icon);
                for (int i = 0; i < titleArr.length; i++) {
                    dataList.add(new ConfigOtherAdapter.ItemOther(titleArr[i], iconArr.getResourceId(i, 0)));
                }
                iconArr.recycle();
                adapter = new ConfigOtherAdapter(act, R.layout.grid_item, dataList);
                return true;
            }

            @Override
            public void next(ViewStub viewStub) {
                viewStub.inflate();
                RecyclerView rvData = act.findViewById(R.id.rv_config);
                GridLayoutManager glm = new GridLayoutManager(act, 3);
                rvData.setLayoutManager(glm);
                rvData.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean doNextSuccess() {
        return false;
    }
}
