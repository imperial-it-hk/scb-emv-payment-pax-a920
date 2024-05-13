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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.pay.password;

import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;

/**
 * Change terminal password
 */
public class ChangeMerchantPwdActivity extends BaseChangePwdActivity {
    private static final String TAG = ChangeMerchantPwdActivity.class.getSimpleName();

    @Override
    protected void savePwd() {
        if(!ConfigUtils.getInstance().setDeviceConf(ConfigConst.MERCHANT_PASSWORD, pwd)) {
            LogUtils.e(TAG, "Save of new password FAILED!");
        }
    }
}
