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

import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.ConfigThirdActivity;
import com.evp.settings.inflater.ConfigInflater;
import com.evp.settings.inflater.ConfigInputInflater;

/**
 * The type Config quick.
 *
 * @author ligq
 * @date 2019 /4/18 15:23
 */
public class ConfigQuick implements IConfig<ConfigInflater<ConfigThirdActivity>> {
    @Override
    public ConfigInflater<ConfigThirdActivity> getInflater(String title) {
        if (Utils.getString(R.string.keyManage_menu_tmk_index).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MK_INDEX)
                    , 2, R.string.digits_2);
        } else if (Utils.getString(R.string.keyManage_menu_tmk_index_no).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MK_INDEX_MANUAL),
                    2, R.string.digits_2);
        } else if (Utils.getString(R.string.keyManage_menu_tmk_value).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.MK_VALUE), 32, R.string.digits_1);
        } else if (Utils.getString(R.string.keyManage_menu_tpk_value).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.PK_VALUE), 32, R.string.digits_1);
        } else if (Utils.getString(R.string.keyManage_menu_tak_value).equals(title)) {
            return new ConfigInputInflater(Utils.getString(R.string.AK_VALUE), 32, R.string.digits_1);
        } else {
            return null;
        }
    }

    @Override
    public void doOkNext(Object... args) {

    }
}
