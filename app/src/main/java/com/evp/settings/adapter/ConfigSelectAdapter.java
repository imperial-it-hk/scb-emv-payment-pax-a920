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
import android.view.View;
import android.widget.ImageView;

import com.evp.adapter.CommonAdapter;
import com.evp.adapter.ViewHolder;
import com.evp.pay.utils.RxUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * The type Config select adapter.
 *
 * @author ligq
 * @date 2019 /4/18 13:58
 */
public class ConfigSelectAdapter extends CommonAdapter<ConfigSelectAdapter.ItemConfigSelect> {
    private ItemConfigSelect currentSelect;

    /**
     * Instantiates a new Config select adapter.
     *
     * @param context  the context
     * @param layoutId the layout id
     * @param dataList the data list
     */
    public ConfigSelectAdapter(@NotNull Context context, int layoutId, @NotNull List<ItemConfigSelect> dataList) {
        super(context, layoutId, dataList);
    }

    @Override
    protected void convert(@NotNull ViewHolder holder, final ItemConfigSelect t, final int position) {
        holder.setText(R.id.tv_config_select_content, t.content);
        ImageView view = holder.getView(R.id.iv_config_selected);
        view.setVisibility(t.visible);
        holder.getView(R.id.cl_config_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelect = t;
                for (int i = 0; i < mDataList.size(); i++) {
                    if (i == position) {
                        mDataList.get(i).setVisible(View.VISIBLE);
                    } else {
                        mDataList.get(i).setVisible(View.INVISIBLE);
                    }
                }
                RxUtils.addDisposable(Observable.timer(100, TimeUnit.MILLISECONDS)
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                ((ConfigThirdActivity) mContext).onKeyOkDown();
                            }
                        }));
            }
        });
    }

    /**
     * Gets current select.
     *
     * @return the current select
     */
    public ItemConfigSelect getCurrentSelect() {
        return currentSelect;
    }

    /**
     * Sets current select.
     *
     * @param currentSelect the current select
     */
    public void setCurrentSelect(ItemConfigSelect currentSelect) {
        this.currentSelect = currentSelect;
    }

    /**
     * The type Item config select.
     */
    public static class ItemConfigSelect {
        private String content;
        private int visible;

        /**
         * Instantiates a new Item config select.
         */
        public ItemConfigSelect() {
        }

        /**
         * Instantiates a new Item config select.
         *
         * @param content the content
         */
        public ItemConfigSelect(String content) {
            this.content = content;
            this.visible = View.INVISIBLE;
        }

        /**
         * Instantiates a new Item config select.
         *
         * @param content the content
         * @param visible the visible
         */
        public ItemConfigSelect(String content, int visible) {
            this.content = content;
            this.visible = visible;
        }

        /**
         * Gets content.
         *
         * @return the content
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets content.
         *
         * @param content the content
         */
        public void setContent(String content) {
            this.content = content;
        }

        /**
         * Gets visible.
         *
         * @return the visible
         */
        public int getVisible() {
            return visible;
        }

        /**
         * Sets visible.
         *
         * @param visible the visible
         */
        public void setVisible(int visible) {
            this.visible = visible;
        }
    }
}
