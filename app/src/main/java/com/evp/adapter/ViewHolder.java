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
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by zhangyp on 2019/4/17
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    private Context context;
    private SparseArray<View> mViews = new SparseArray();

    /**
     * Instantiates a new View holder.
     *
     * @param context  the context
     * @param itemView the item view
     */
    public ViewHolder(Context context, @NonNull View itemView) {
        super(itemView);
        this.context = context;
    }

    /**
     * Create view holder view holder.
     *
     * @param context  the context
     * @param itemView the item view
     * @return the view holder
     */
    public static ViewHolder createViewHolder(Context context, View itemView) {
        return new ViewHolder(context, itemView);
    }

    /**
     * Create view holder view holder.
     *
     * @param context  the context
     * @param parent   the parent
     * @param layoutId the layout id
     * @return the view holder
     */
    public static ViewHolder createViewHolder(Context context, ViewGroup parent, int layoutId) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ViewHolder(context, itemView);
    }

    /**
     * Gets view.
     *
     * @param <T>    the type parameter
     * @param viewId the view id
     * @return the view
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * Gets convert view.
     *
     * @return the convert view
     */
    public View getConvertView() {
        return itemView;
    }

    /**
     * Sets text.
     *
     * @param viewId the view id
     * @param text   the text
     * @return the text
     */
    public ViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    /**
     * Sets image resource.
     *
     * @param viewId the view id
     * @param resId  the res id
     * @return the image resource
     */
    public ViewHolder setImageResource(int viewId, int resId) {
        ImageView view = getView(viewId);
        view.setImageResource(resId);
        return this;
    }

    /**
     * Sets image bitmap.
     *
     * @param viewId the view id
     * @param bitmap the bitmap
     * @return the image bitmap
     */
    public ViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    /**
     * Sets image drawable.
     *
     * @param viewId   the view id
     * @param drawable the drawable
     * @return the image drawable
     */
    public ViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = getView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    /**
     * Sets background color.
     *
     * @param viewId the view id
     * @param color  the color
     * @return the background color
     */
    public ViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    /**
     * Sets background res.
     *
     * @param viewId        the view id
     * @param backgroundRes the background res
     * @return the background res
     */
    public ViewHolder setBackgroundRes(int viewId, int backgroundRes) {
        View view = getView(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    /**
     * Sets text color.
     *
     * @param viewId    the view id
     * @param textColor the text color
     * @return the text color
     */
    public ViewHolder setTextColor(int viewId, int textColor) {
        TextView view = getView(viewId);
        view.setTextColor(textColor);
        return this;
    }

    /**
     * Sets text color res.
     *
     * @param viewId       the view id
     * @param textColorRes the text color res
     * @return the text color res
     */
    public ViewHolder setTextColorRes(int viewId, int textColorRes) {
        TextView view = getView(viewId);
        view.setTextColor(ContextCompat.getColor(context, textColorRes));
        return this;
    }

    /**
     * Sets visible.
     *
     * @param viewId  the view id
     * @param visible the visible
     * @return the visible
     */
    public ViewHolder setVisible(int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    /**
     * Link ify view holder.
     *
     * @param viewId the view id
     * @return the view holder
     */
    public ViewHolder linkIfy(int viewId) {
        TextView view = getView(viewId);
        Linkify.addLinks(view, Linkify.ALL);
        return this;
    }

    /**
     * Sets typeface.
     *
     * @param typeface the typeface
     * @param viewIds  the view ids
     * @return the typeface
     */
    public ViewHolder setTypeface(Typeface typeface, int... viewIds) {
        for (int viewId : viewIds) {
            TextView view = getView(viewId);
            view.setTypeface(typeface);
            view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
        return this;
    }

    /**
     * Sets progress.
     *
     * @param viewId   the view id
     * @param progress the progress
     * @return the progress
     */
    public ViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = getView(viewId);
        view.setProgress(progress);
        return this;
    }

    /**
     * Sets progress.
     *
     * @param viewId   the view id
     * @param progress the progress
     * @param max      the max
     * @return the progress
     */
    public ViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar view = getView(viewId);
        view.setMax(max);
        view.setProgress(progress);
        return this;
    }

    /**
     * Sets max.
     *
     * @param viewId the view id
     * @param max    the max
     * @return the max
     */
    public ViewHolder setMax(int viewId, int max) {
        ProgressBar view = getView(viewId);
        view.setMax(max);
        return this;
    }

    /**
     * Sets rating.
     *
     * @param viewId the view id
     * @param rating the rating
     * @return the rating
     */
    public ViewHolder setRating(int viewId, float rating) {
        RatingBar view = getView(viewId);
        view.setRating(rating);
        return this;
    }

    /**
     * Sets rating.
     *
     * @param viewId the view id
     * @param rating the rating
     * @param max    the max
     * @return the rating
     */
    public ViewHolder setRating(int viewId, float rating, int max) {
        RatingBar view = getView(viewId);
        view.setRating(rating);
        view.setMax(max);
        return this;
    }

    /**
     * Sets tag.
     *
     * @param viewId the view id
     * @param tag    the tag
     * @return the tag
     */
    public ViewHolder setTag(int viewId, Object tag) {
        View view = getView(viewId);
        view.setTag(tag);
        return this;
    }

    /**
     * Sets tag.
     *
     * @param viewId the view id
     * @param tag    the tag
     * @param key    the key
     * @return the tag
     */
    public ViewHolder setTag(int viewId, Object tag, int key) {
        View view = getView(viewId);
        view.setTag(key, tag);
        return this;
    }

    /**
     * Sets checked.
     *
     * @param viewId  the view id
     * @param checked the checked
     * @return the checked
     */
    public ViewHolder setChecked(int viewId, boolean checked) {
        Checkable view = getView(viewId);
        view.setChecked(checked);
        return this;
    }

    /**
     * Sets on click listener.
     *
     * @param viewId   the view id
     * @param listener the listener
     * @return the on click listener
     */
    public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    /**
     * Sets on touch listener.
     *
     * @param viewId   the view id
     * @param listener the listener
     * @return the on touch listener
     */
    public ViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        View view = getView(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    /**
     * Sets on long click listener.
     *
     * @param viewId   the view id
     * @param listener the listener
     * @return the on long click listener
     */
    public ViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

}
