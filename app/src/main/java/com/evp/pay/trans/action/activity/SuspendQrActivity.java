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

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.evp.abl.core.ActionResult;
import com.evp.adapter.TransDataAdapter;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.component.Component;
import com.evp.payment.evpscb.R;

import java.util.List;

/**
 * The type Trans query activity.
 */
public class SuspendQrActivity extends BaseActivityWithTickForAction {
    private ListView transDataListView;
    private String title;
    private List<TransData> transDataList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_suspend_table;
    }

    @Override
    protected void initViews() {
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root);
        constraintLayout.setBackgroundColor(secondaryColor);
        transDataListView = findViewById(R.id.suspend_list);
    }

    @Override
    protected String getTitleString() {
        return title;
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        transDataList = GreendaoHelper.getTransDataHelper().findAllSuspendQrCodeTransData();
        transDataListView = (ListView) findViewById(R.id.suspend_list);
        TransDataAdapter transDataAdapter = new TransDataAdapter(this, transDataList);
        transDataListView.setAdapter(transDataAdapter);
        transDataListView.setOnItemClickListener(onClickListView);
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            finish(new ActionResult(TransResult.SUCC, Component.getPaddedNumber(transDataList.get(position).getTraceNo(), 6)));
        }

    };
}