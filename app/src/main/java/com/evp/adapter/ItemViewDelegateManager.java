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

import androidx.collection.SparseArrayCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Item view delegate manager.
 *
 * @param <T> the type parameter
 */
public class ItemViewDelegateManager<T> {
    private SparseArrayCompat delegates = new SparseArrayCompat<ItemViewDelegate<T>>();

    /**
     * Gets item view delegate count.
     *
     * @return the item view delegate count
     */
    public int getItemViewDelegateCount() {
        return delegates.size();
    }

    /**
     * Add delegate item view delegate manager.
     *
     * @param delegate the delegate
     * @return the item view delegate manager
     */
    @NotNull
    public ItemViewDelegateManager<T> addDelegate(@Nullable ItemViewDelegate<T> delegate) {
        int viewType = delegates.size();
        if (delegate != null) {
            delegates.put(viewType, delegate);
        }
        return this;
    }

    /**
     * Add delegate item view delegate manager.
     *
     * @param viewType the view type
     * @param delegate the delegate
     * @return the item view delegate manager
     */
    @NotNull
    public ItemViewDelegateManager<T> addDelegate(int viewType, @NotNull ItemViewDelegate<T> delegate) {
        if (delegates.get(viewType) != null) {
            throw new IllegalArgumentException("An ItemViewDelegate is already registered for the viewType = " + viewType + ". Already registered ItemViewDelegate is " + delegates.get(viewType));
        }
        delegates.put(viewType, delegate);
        return this;

    }

    /**
     * Remove delegate item view delegate manager.
     *
     * @param delegate the delegate
     * @return the item view delegate manager
     */
    @NotNull
    public ItemViewDelegateManager<T> removeDelegate(@Nullable ItemViewDelegate<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("ItemViewDelegate is null");
        }
        int indexToRemove = delegates.indexOfValue(delegate);
        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }

        return this;

    }

    /**
     * Remove delegate item view delegate manager.
     *
     * @param itemType the item type
     * @return the item view delegate manager
     */
    @NotNull
    public  ItemViewDelegateManager<T> removeDelegate(int itemType) {
        int indexToRemove = delegates.indexOfKey(itemType);
        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }

        return this;
    }

    /**
     * Gets item view type.
     *
     * @param item     the item
     * @param position the position
     * @return the item view type
     */
    public  int getItemViewType(T item, int position)  {
        int delegatesCount = delegates.size();
        for (int i=delegatesCount - 1; i >= 0; i--) {
            ItemViewDelegate<T> delegate = (ItemViewDelegate<T>) delegates.valueAt(i);
            if (delegate.isForViewType(item, position)) {
                return delegates.keyAt(i);
            }
        }
        throw new IllegalArgumentException("No ItemViewDelegate added that matches position=" + position + " in data source");
    }

    /**
     * Convert.
     *
     * @param holder   the holder
     * @param item     the item
     * @param position the position
     */
    public  void convert(@NotNull ViewHolder holder, T item, int position)  {
        int delegatesCount = delegates.size();
        for (int i=0; i < delegatesCount; i++) {
            ItemViewDelegate<T> delegate = (ItemViewDelegate<T>) delegates.valueAt(i);
            if (delegate.isForViewType(item, position)) {
                delegate.convert(holder, item, position);
                return;
            }
        }

        throw new IllegalArgumentException("No ItemViewDelegateManager added that matches position=" + position + " in data source");
    }

    /**
     * Gets item view delegate.
     *
     * @param viewType the view type
     * @return the item view delegate
     */
    @Nullable
    public  ItemViewDelegate<T> getItemViewDelegate(int viewType) {
        return (ItemViewDelegate<T>) delegates.get(viewType);
    }

    /**
     * Gets item view layout id.
     *
     * @param viewType the view type
     * @return the item view layout id
     */
    public  int getItemViewLayoutId(int viewType) {
        return getItemViewDelegate(viewType).getItemViewLayoutId();
    }

    /**
     * Gets item view type.
     *
     * @param itemViewDelegate the item view delegate
     * @return the item view type
     */
    public  int getItemViewType(@NotNull ItemViewDelegate<T> itemViewDelegate) {
        return delegates.indexOfValue(itemViewDelegate);
    }
}

