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
 * 20200825  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.pay.record;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.bizlib.data.entity.TransData;

public class TransDiffCallback extends DiffUtil.ItemCallback<TransData> {
    /**
     * Called to check whether two objects represent the same item.
     * <p>
     * For example, if your items have unique ids, this method should check their id equality.
     * <p>
     * Note: {@code null} items in the list are assumed to be the same as another {@code null}
     * item and are assumed to not be the same as a non-{@code null} item. This callback will
     * not be invoked for either of those cases.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the two items represent the same object or false if they are different.
     * @see DiffUtil.Callback#areItemsTheSame(int, int)
     */
    @Override
    public boolean areItemsTheSame(@NonNull TransData oldItem, @NonNull TransData newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    /**
     * Called to check whether two items have the same data.
     * <p>
     * This information is used to detect if the contents of an item have changed.
     * <p>
     * This method to check equality instead of {@link Object#equals(Object)} so that you can
     * change its behavior depending on your UI.
     * <p>
     * For example, if you are using DiffUtil with a
     * {@link RecyclerView.Adapter RecyclerView.Adapter}, you should
     * return whether the items' visual representations are the same.
     * <p>
     * This method is called only if {@link #areItemsTheSame(T, T)} returns {@code true} for
     * these items.
     * <p>
     * Note: Two {@code null} items are assumed to represent the same contents. This callback
     * will not be invoked for this case.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the contents of the items are the same or false if they are different.
     * @see DiffUtil.Callback#areContentsTheSame(int, int)
     */
    @Override
    public boolean areContentsTheSame(@NonNull TransData oldItem, @NonNull TransData newItem) {
        return oldItem.equals(newItem);
    }
}
