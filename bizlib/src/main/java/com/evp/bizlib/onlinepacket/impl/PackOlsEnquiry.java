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
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.annotation.RouterService;

/**
 * pack Enquiry
 */
@RouterService(interfaces = IPacker.class,key = OnlinePacketConst.PACKET_OLS_ENQUIRY)
public class PackOlsEnquiry extends PackIso8583 {

    private static final String TAG = "PackOlsEnquiry";
    private static final String OLS_VERSION = "02000100";

    public PackOlsEnquiry() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "ISO pack START");

        try {
            setHeader(transData);
            setBitData2(transData);
            setBitData3(transData);
            setBitData12(transData);
            setBitData13(transData);
            setBitData23(transData);
            setBitData24(transData);
            setBitData35(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData52(transData);
            setBitData55(transData);
            setBitData63(transData);
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "ISO pack END");

        return pack(transData, false);
    }

    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        String f63 = OLS_VERSION;
        f63 = f63.concat(ConvertUtils.getPaddedString("", 32));

        //String len = String.format("%04d", f63.length() / 2);
        setBitData("63", ConvertUtils.asciiToBin(f63));
    }

}
