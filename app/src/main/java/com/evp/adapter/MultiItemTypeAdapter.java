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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by zhangyp on 2019/4/18
 *
 * @param <T> the type parameter
 */
public class MultiItemTypeAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    /**
     * The M context.
     */
    protected Context mContext;
    /**
     * The M data list.
     */
    protected List<T> mDataList;
    /**
     * The M item view delegate manager.
     */
    protected ItemViewDelegateManager<T> mItemViewDelegateManager=new ItemViewDelegateManager<>();
    /**
     * The M on item click listener.
     */
    protected OnItemClickListener mOnItemClickListener;

    /**
     * The interface On item click listener.
     */
    interface OnItemClickListener {
        /**
         * On item click.
         *
         * @param view     the view
         * @param holder   the holder
         * @param position the position
         */
        void onItemClick(View view, RecyclerView.ViewHolder holder, int position);

        /**
         * On item long click boolean.
         *
         * @param view     the view
         * @param holder   the holder
         * @param position the position
         * @return the boolean
         */
        boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position);
    }

    /**
     * Instantiates a new Multi item type adapter.
     *
     * @param mContext  the m context
     * @param mDataList the m data list
     */
    public MultiItemTypeAdapter(Context mContext, List<T> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
    }

    @Override
    public int getItemViewType(int position) {
        if (!useItemViewDelegateManager())
            return super.getItemViewType(position);
        else {
            return mItemViewDelegateManager.getItemViewType(mDataList.get(position), position);
        }
    }

    /**
     * Use item view delegate manager boolean.
     *
     * @return the boolean
     */
    protected boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ItemViewDelegate<T> itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        ViewHolder holder = ViewHolder.createViewHolder(mContext, viewGroup, layoutId);
        onViewHolderCreated(holder, holder.getConvertView());
        setListener(viewGroup, holder, viewType);
        return holder;
    }

    private void setListener(ViewGroup viewGroup, final ViewHolder viewHolder, int viewType) {
        viewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(v, viewHolder, position);
                }
            }
        });
        viewHolder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    return mOnItemClickListener.onItemLongClick(v, viewHolder, position);
                }
                return false;
            }
        });
    }

    /**
     * On view holder created.
     *
     * @param holder      the holder
     * @param convertView the convert view
     */
    public void onViewHolderCreated(ViewHolder holder, View convertView) {
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        convert(holder, mDataList.get(position));
    }

    private void convert(ViewHolder holder, T t) {
        mItemViewDelegateManager.convert(holder, t, holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * Gets datas.
     *
     * @return the datas
     */
    public List<T> getDatas() {
        return mDataList;
    }

    /**
     * Add item view delegate multi item type adapter.
     *
     * @param itemViewDelegate the item view delegate
     * @return the multi item type adapter
     */
    public MultiItemTypeAdapter<T> addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(itemViewDelegate);
        return this;
    }

    /**
     * Add item view delegate multi item type adapter.
     *
     * @param viewType         the view type
     * @param itemViewDelegate the item view delegate
     * @return the multi item type adapter
     */
    public MultiItemTypeAdapter<T> addItemViewDelegate(int viewType, ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(viewType, itemViewDelegate);
        return this;
    }

    /**
     * Sets on item click listener.
     *
     * @param onItemClickListener the on item click listener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
