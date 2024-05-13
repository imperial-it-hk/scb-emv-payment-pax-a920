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

/**
 * Created by zhangyp on 2019/4/18
 *
 * @param <T> the type parameter
 */
public interface ItemViewDelegate<T> {
    /**
     * Gets item view layout id.
     *
     * @return the item view layout id
     */
    int getItemViewLayoutId();

    /**
     * Is for view type boolean.
     *
     * @param item     the item
     * @param position the position
     * @return the boolean
     */
    boolean isForViewType(T item, int position);

    /**
     * Convert.
     *
     * @param holder   the holder
     * @param t        the t
     * @param position the position
     */
    void convert(ViewHolder holder, T t, int position);
}
