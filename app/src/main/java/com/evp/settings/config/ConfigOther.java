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

import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.OtherInflater;

/**
 * The type Config other.
 *
 * @author ligq
 * @date 2019 /4/18 15:21
 */
public class ConfigOther implements IConfig<ConfigInflater<ConfigThirdActivity>> {
    @Override
    public ConfigInflater<ConfigThirdActivity> getInflater(String title) {
        return new OtherInflater();
    }

    @Override
    public void doOkNext(Object... args) {

    }
}
