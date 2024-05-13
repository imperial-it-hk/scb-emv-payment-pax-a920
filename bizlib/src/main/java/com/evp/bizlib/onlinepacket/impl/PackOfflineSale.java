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
 * pack OfflineTransSend
 */
@RouterService(interfaces = IPacker.class,key = OnlinePacketConst.PACKET_OFFLINE_SALE)
public class PackOfflineSale extends PackIso8583 {

    public PackOfflineSale() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "PackOfflineSale ISO pack START");

        try {
            setHeader(transData);
            setBitData2(transData);
            setBitData3(transData);
            setBitData4(transData);
            setBitData11(transData);
            setBitData14(transData);
            setBitData22(transData);
            setBitData23(transData);
            setBitData24(transData);
            setBitData25(transData);
            setBitData35(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData45(transData);
            setBitData52(transData);
            setBitData54(transData);
            setBitData55(transData);
            setBitData62(transData);
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "PackOfflineSale ISO pack END");

        return pack(transData, false);
    }
}
