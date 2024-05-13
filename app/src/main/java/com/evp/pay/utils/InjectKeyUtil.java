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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.pay.utils;

import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.settings.SysParam;
import com.pax.dal.entity.EPedKeyType;

/**
 * The type Inject key util.
 */
public class InjectKeyUtil {
    /**
     * Inject mksk. Only for DEMO mode!
     *
     * @param type the type
     */
    public static void injectMKSK(String type) {
        if (!SysParam.CommType.DEMO.equals(type)) {
            return;
        }
        String setId = Utils.getString(R.string.SET_1);
        if (!PedHelper.isKeyInjected(EPedKeyType.TAK, KeyUtils.getTakIndex(setId))) {
            return;
        }
        try {
            PedHelper.writeTMK(KeyUtils.getTmkIndex(setId), ConvertHelper.getConvert().strToBcdPaddingLeft("54DCBF79AEB970329E97B98651E619CE"));
            PedHelper.writeTPK(KeyUtils.getTmkIndex(setId), KeyUtils.getTpkIndex(setId), ConvertHelper.getConvert().strToBcdPaddingLeft("9D8D00D7DB16D3F8C1C475F192B22751"), null);
            PedHelper.writeTAK(KeyUtils.getTmkIndex(setId), KeyUtils.getTakIndex(setId), ConvertHelper.getConvert().strToBcdPaddingLeft("B5DDF952B2707E670102030405060708"), null);
            PedHelper.writeTDK(KeyUtils.getTmkIndex(setId), KeyUtils.getTdkIndex(setId), ConvertHelper.getConvert().strToBcdPaddingLeft("768A5B4EFB48BC30C1CC3FD22522714D"), null);
        } catch (Exception e) {
            LogUtils.e("InjectKeyUtil", e);
        }
    }
}
