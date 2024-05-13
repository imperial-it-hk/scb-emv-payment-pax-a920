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
 * 20190108  	         Rim.Z                   Create
 * ===========================================================================================
 */
package com.evp.pay.password;

import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;

/**
 * Change settle password
 */
public class ChangeSettlePwdActivity extends BaseChangePwdActivity {
    private static final String TAG = ChangeSettlePwdActivity.class.getSimpleName();

    @Override
    protected void savePwd() {
        if(!ConfigUtils.getInstance().setDeviceConf(ConfigConst.SETTLE_PASSWORD, pwd)) {
            LogUtils.e(TAG, "Save of new password FAILED!");
        }
    }
}
