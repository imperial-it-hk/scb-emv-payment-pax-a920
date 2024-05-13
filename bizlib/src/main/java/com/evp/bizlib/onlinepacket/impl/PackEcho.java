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
package com.evp.bizlib.onlinepacket.impl;

import androidx.annotation.NonNull;

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.sankuai.waimai.router.annotation.RouterService;

/**
 * pack echo
 */
@RouterService(interfaces = IPacker.class, key = OnlinePacketConst.PACKET_ECHO)
public class PackEcho extends PackIso8583 {

    private static final String TAG = "PackEcho";

    public PackEcho() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "Comm test ISO pack START");

        try {
            setHeader(transData);
            setBitData3(transData);
            setBitData25(transData);
            setBitData24(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData11(transData);
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "Comm test ISO pack END");

        return pack(transData, false);
    }

}
