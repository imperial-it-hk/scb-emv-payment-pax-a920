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
package com.evp.pay.record;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.TransData;
import com.evp.commonlib.utils.LogUtils;
import com.evp.payment.evpscb.R;
import com.pax.edc.expandablerecyclerview.ExpandItemAnimator;


/**
 * The type Trans detail fragment.
 */
public class TransDetailFragment extends Fragment {
    private Context context;
    private RecyclerView mRecyclerView;
    private TransDetailAdapter adapter;
    private View noTransRecord;
    private boolean supportDoTrans = true;
    private String acquirerName = "";
    private LiveData<PagedList<TransData>> liveData;
    /**
     * Instantiates a new Trans detail fragment.
     */
    public TransDetailFragment() {
        //do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        PagedList<TransData> value = liveData.getValue();
        if (value != null){
            DataSource<?, TransData> dataSource = value.getDataSource();
            dataSource.invalidate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_layout, container, false);
        this.context = getContext();
//region OLS
        if (acquirerName.equals(AppConstants.OLS_ACQUIRER)) {
            TextView tvPoint = view.findViewById(R.id.trans_point_tv);
            if (tvPoint != null)
                tvPoint.setVisibility(View.VISIBLE);
        }
//endregion
        noTransRecord = view.findViewById(R.id.no_trans_record);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.trans_list);

        //AET-374,recyclerView notify exception
        mRecyclerView.setLayoutManager(new CustomLinearLayoutManager(this.getActivity()));
        mRecyclerView.setItemAnimator(new ExpandItemAnimator());
        adapter = new TransDetailAdapter(new TransDiffCallback(),supportDoTrans);
        mRecyclerView.setAdapter(adapter);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)//initial load size
                .setPageSize(20)//item count per page
                .setEnablePlaceholders(false)
                .build();
        liveData = new LivePagedListBuilder<>(new TransDataSourceFactory(acquirerName),config).build();
        liveData.observe(getViewLifecycleOwner(), new Observer<PagedList<TransData>>() {
            @Override
            public void onChanged(PagedList<TransData> transData) {
                if (transData == null ||transData.isEmpty()){
                    mRecyclerView.setVisibility(View.GONE);
                    noTransRecord.setVisibility(View.VISIBLE);
                    return;
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                noTransRecord.setVisibility(View.GONE);
                adapter.submitList(transData);
            }
        });
        return view;
    }

    /**
     * The type Custom linear layout manager.
     */
    static class CustomLinearLayoutManager extends LinearLayoutManager{

        /**
         * Instantiates a new Custom linear layout manager.
         *
         * @param context the context
         */
        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            }catch (IndexOutOfBoundsException e){
                LogUtils.e(e);
            }

        }
    }

    /**
     * Gets acquirer name.
     *
     * @return the acquirer name
     */
    public String getAcquirerName() {
        return acquirerName;
    }

    /**
     * Sets acquirer name.
     *
     * @param acquirerName the acquirer name
     */
    public void setAcquirerName(String acquirerName) {
        this.acquirerName = acquirerName;
    }

    /**
     * Is support do trans boolean.
     *
     * @return the boolean
     */
    public boolean isSupportDoTrans() {
        return supportDoTrans;
    }

    /**
     * Sets support do trans.
     *
     * @param supportDoTrans the support do trans
     */
    public void setSupportDoTrans(boolean supportDoTrans) {
        this.supportDoTrans = supportDoTrans;
    }
}


