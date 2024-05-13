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

package com.evp.settings.config;

import com.evp.settings.inflater.ConfigInflater;

/**
 * The interface Config.
 *
 * @param <T> the type parameter
 * @author ligq
 * @date 2019 /4/18 14:31
 */
public interface IConfig<T extends ConfigInflater> {
    /**
     * Gets inflater.
     *
     * @param title the title
     * @return the inflater
     */
    T getInflater(String title);

    /**
     * Do ok next.
     *
     * @param args the args
     */
    void doOkNext(Object... args);
}
