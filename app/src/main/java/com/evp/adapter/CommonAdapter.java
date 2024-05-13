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
package com.evp.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * common RecyclerView adapter
 *
 * @param <T> entity
 */
public abstract class CommonAdapter<T> extends MultiItemTypeAdapter<T> {
    @NotNull
    private LayoutInflater mInflater;

    /**
     * Gets m inflater.
     *
     * @return the m inflater
     */
    @NotNull
    protected final LayoutInflater getMInflater() {
        return this.mInflater;
    }

    /**
     * Sets m inflater.
     *
     * @param var1 the var 1
     */
    protected final void setMInflater(@NotNull LayoutInflater var1) {
        this.mInflater = var1;
    }

    /**
     * Instantiates a new Common adapter.
     *
     * @param context  the context
     * @param layoutId the layout id
     * @param dataList the data list
     */
    public CommonAdapter(@NotNull Context context, final int layoutId, @NotNull List<T> dataList) {
        super(context, dataList);
        mInflater = LayoutInflater.from(context);
        ItemViewDelegate itemViewDelegate = new ItemViewDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return layoutId;
            }

            @Override
            public boolean isForViewType(T item, int position) {
                return true;
            }

            @Override
            public void convert(ViewHolder holder, T item, int position) {
                CommonAdapter.this.convert(holder,item,position);

            }
        };
        addItemViewDelegate((ItemViewDelegate) itemViewDelegate);
    }

    /**
     * Convert.
     *
     * @param holder   the holder
     * @param t        the t
     * @param position the position
     */
    protected abstract void convert(ViewHolder holder, T t, int position);
}
