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
 *  20190417   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.adapter;

import android.content.Context;
import android.view.View;

import com.evp.adapter.CommonAdapter;
import com.evp.adapter.ViewHolder;
import com.evp.pay.app.quickclick.QuickClickProtection;
import com.evp.payment.evpscb.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The type Config first adapter.
 *
 * @author ligq
 * @date 2019 /4/17 15:39
 */
public class ConfigFirstAdapter extends CommonAdapter<ConfigFirstAdapter.ItemConfigFirst> {
    private OnConfigItemClickListener onItemClickListener;

    /**
     * Instantiates a new Config first adapter.
     *
     * @param context  the context
     * @param layoutId the layout id
     * @param dataList the data list
     */
    public ConfigFirstAdapter(Context context, int layoutId, List<ItemConfigFirst> dataList) {
        super(context, layoutId, dataList);
    }

    @Override
    protected void convert(@NotNull ViewHolder holder, final ItemConfigFirst itemConfigFirst, int position) {
        holder.setText(R.id.tv_configs_title, itemConfigFirst.getTitle());
        holder.setBackgroundRes(R.id.iv_configs_left_icon, itemConfigFirst.getIconId());
        showLine2(holder, itemConfigFirst.showLine2);
        final QuickClickProtection quickClickProtection = QuickClickProtection.getInstance();
        holder.getView(R.id.cl_configs_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //AET-123
                if (!quickClickProtection.isStarted()) {
                    quickClickProtection.start();
                    onItemClickListener.onConfigsItemClick(itemConfigFirst.getTitle(), itemConfigFirst.getType());
                }
            }
        });
    }

    private void showLine2(ViewHolder holder, Boolean showLine2) {
        if (showLine2) {
            holder.setVisible(R.id.view_config_bottom_line1, false);
            holder.setVisible(R.id.view_configs_bottom_line2, true);
        } else {
            holder.setVisible(R.id.view_config_bottom_line1, true);
            holder.setVisible(R.id.view_configs_bottom_line2, false);
        }
    }

    /**
     * Sets on item click listener.
     *
     * @param onItemClickListener the on item click listener
     */
    public void setOnItemClickListener(OnConfigItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * The type Item config first.
     */
    public static class ItemConfigFirst {
        private int iconId;
        private String title;
        private boolean showLine2;
        private int type;

        /**
         * Instantiates a new Item config first.
         */
        public ItemConfigFirst() {
        }

        /**
         * Instantiates a new Item config first.
         *
         * @param iconId    the icon id
         * @param title     the title
         * @param showLine2 the show line 2
         * @param type      the type
         */
        public ItemConfigFirst(int iconId, String title, boolean showLine2, int type) {
            this.iconId = iconId;
            this.title = title;
            this.showLine2 = showLine2;
            this.type = type;
        }

        /**
         * Gets icon id.
         *
         * @return the icon id
         */
        public int getIconId() {
            return iconId;
        }

        /**
         * Sets icon id.
         *
         * @param iconId the icon id
         */
        public void setIconId(int iconId) {
            this.iconId = iconId;
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
         * Is show line 2 boolean.
         *
         * @return the boolean
         */
        public boolean isShowLine2() {
            return showLine2;
        }

        /**
         * Sets show line 2.
         *
         * @param showLine2 the show line 2
         */
        public void setShowLine2(boolean showLine2) {
            this.showLine2 = showLine2;
        }

        /**
         * Gets type.
         *
         * @return the type
         */
        public int getType() {
            return type;
        }

        /**
         * Sets type.
         *
         * @param type the type
         */
        public void setType(int type) {
            this.type = type;
        }
    }

    /**
     * The interface On config item click listener.
     */
    public interface OnConfigItemClickListener {
        /**
         * On configs item click.
         *
         * @param title the title
         * @param type  the type
         */
        void onConfigsItemClick(String title, int type);
    }
}
