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

package com.evp.settings.inflater;

import com.evp.pay.BaseActivity;

/**
 * The interface Config inflater.
 *
 * @param <T> the type parameter
 * @author ligq
 * @date 2019 /4/18 16:35
 */
public interface ConfigInflater<T extends BaseActivity> {
    /**
     * init the view by title
     *
     * @param act   activity
     * @param title title
     */
    void inflate(T act, String title);

    /**
     * do the next function after pressing the button
     * if no button ,please return false
     *
     * @return modify the value successfully or not
     */
    boolean doNextSuccess();
}
