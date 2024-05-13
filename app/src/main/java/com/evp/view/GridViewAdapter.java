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
package com.evp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ATransaction;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;
import com.pax.edc.expandablerecyclerview.BaseViewHolder;

import java.util.List;

/**
 * The type Grid view adapter.
 */
class GridViewAdapter extends BaseAdapter {

    private List<GridItem> itemList;
    private Context context;
    private int secondaryColor;

    /**
     * Instantiates a new Grid view adapter.
     *
     * @param context the context
     * @param list    the list
     */
    GridViewAdapter(Context context, List<GridItem> list) {
        this.context = context;
        itemList = list;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        } else {
            view = convertView;
        }

        ImageView iv = BaseViewHolder.get(view, R.id.iv_item);
        AlwaysMarqueeTextView tv = BaseViewHolder.get(view, R.id.tv_item);

        tv.setText(getViewText(position));

        if (getViewIconImage(position) != null) {
            iv.setImageBitmap(getViewIconImage(position));
        } else {
            iv.setImageDrawable(FinancialApplication.getApp().getResources().getDrawable(getViewIcon(position)));
        }
        return view;
    }

    private Integer getViewIcon(int position) {
        GridItem holder = itemList.get(position);
        return holder.getIcon();
    }

    private Bitmap getViewIconImage(int position) {
        GridItem holder = itemList.get(position);
        Bitmap result = holder.getIconImage();
        return result;
    }

    private String getViewText(int position) {
        GridItem holder = itemList.get(position);
        return holder.getName();
    }

    /**
     * The type Grid item.
     */
    static class GridItem {

        private String name;
        private int icon;
        private Bitmap iconImage;
        private ATransaction trans;
        private Class<?> activity;
        private AAction action;
        private Intent intent;
        private String index;

        /**
         * Instantiates a new Grid item.
         *
         * @param name the name
         * @param icon the icon
         */
        GridItem(String name, int icon) {
            this.name = name;
            this.icon = icon;
//            this.trans = trans;
        }

        /**
         * Instantiates a new Grid item.
         *
         * @param name the name
         * @param icon the icon
         * @param act  the act
         */
        GridItem(String name, int icon, Class<?> act) {
            this.name = name;
            this.icon = icon;
            this.activity = act;
        }

        GridItem(String name, Bitmap icon, String index) {
            this.name = name;
            this.iconImage = icon;
            this.index = index;
        }

        /**
         * Instantiates a new Grid item.
         *
         * @param name   the name
         * @param icon   the icon
         * @param action the action
         */
        GridItem(String name, int icon, AAction action) {
            this.name = name;
            this.icon = icon;
            this.action = action;
        }

        /**
         * Instantiates a new Grid item.
         *
         * @param name   the name
         * @param icon   the icon
         * @param intent the intent
         */
        GridItem(String name, int icon, Intent intent) {
            this.name = name;
            this.icon = icon;
            this.intent = intent;
        }

        /**
         * Gets icon.
         *
         * @return the icon
         */
        public int getIcon() {
            return icon;
        }

        /**
         * Sets icon.
         *
         * @param icon the icon
         */
        public void setIcon(int icon) {
            this.icon = icon;
        }

        /**
         * Gets iconImage.
         *
         * @return the iconImage
         */
        public Bitmap getIconImage() {
            return iconImage;
        }

        /**
         * Sets iconImage.
         *
         * @param iconImage the iconImage
         */
        public void setIcon(Bitmap iconImage) {
            this.iconImage = iconImage;
        }

        /**
         * Gets index.
         *
         * @return the index
         */
        public String getIndex() {
            return index;
        }

        /**
         * Sets iconImage.
         *
         * @param index the index
         */
        public void setIcon(String index) {
            this.index = index;
        }

        /**
         * Gets trans.
         *
         * @return the trans
         */
        public ATransaction getTrans() {
            return trans;
        }

        /**
         * Sets trans.
         *
         * @param trans the trans
         */
        public void setTrans(ATransaction trans) {
            this.trans = trans;
        }

        /**
         * Gets activity.
         *
         * @return the activity
         */
        public Class<?> getActivity() {
            return activity;
        }

        /**
         * Sets activity.
         *
         * @param act the act
         */
        public void setActivity(Class<?> act) {
            this.activity = act;
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets name.
         *
         * @param name the name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets action.
         *
         * @return the action
         */
        public AAction getAction() {
            return action;
        }

        /**
         * Sets action.
         *
         * @param action the action
         */
        public void setAction(AAction action) {
            this.action = action;
        }

        /**
         * Sets intent.
         *
         * @param intent the intent
         */
        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        /**
         * Gets intent.
         *
         * @return the intent
         */
        public Intent getIntent() {
            return intent;
        }

    }

}
