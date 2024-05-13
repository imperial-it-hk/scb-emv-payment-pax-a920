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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evp.payment.evpscb.R;
import com.pax.edc.expandablerecyclerview.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * The type New spinner adapter.
 *
 * @param <T> the type parameter
 */
public class NewSpinnerAdapter<T> extends BaseAdapter {
    private Context mContext;
    private List<T> list = new ArrayList<>();
    private OnTextUpdateListener listener;

    /**
     * Instantiates a new New spinner adapter.
     *
     * @param context the context
     */
    public NewSpinnerAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = LayoutInflater.from(mContext).inflate(R.layout.new_list_item, parent, false);
        } else {
            view = convertView;
        }

        TextView name = BaseViewHolder.get(view, R.id.new_item_name);
        name.setGravity(Gravity.CENTER | Gravity.START);

        if (listener != null) {
            String str = listener.onTextUpdate(list, position);
            if (str != null) {
                name.setText(str);
            }
        }

        return view;
    }

    /**
     * Gets list info.
     *
     * @return the list info
     */
    public List<T> getListInfo() {
        return list;
    }

    /**
     * Gets list info.
     *
     * @param pos the pos
     * @return the list info
     */
    public T getListInfo(int pos) {
        return list.get(pos);
    }

    /**
     * Sets list info.
     *
     * @param infos the infos
     */
    public void setListInfo(List<T> infos) {
        if (infos != null) {
            list.clear();
            list.addAll(infos);
            notifyDataSetChanged();
        }
    }

    /**
     * Sets on text update listener.
     *
     * @param listener the listener
     */
    public void setOnTextUpdateListener(OnTextUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * The interface On text update listener.
     */
    public interface OnTextUpdateListener {
        /**
         * On text update string.
         *
         * @param list     the list
         * @param position the position
         * @return the string
         */
        String onTextUpdate(final List<?> list, int position);
    }
}
