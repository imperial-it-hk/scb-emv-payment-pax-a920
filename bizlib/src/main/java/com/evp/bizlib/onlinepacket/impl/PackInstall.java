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


import static com.evp.commonlib.utils.ConvertUtils.asciiToBin;
import static com.evp.commonlib.utils.ConvertUtils.getPaddedString;

import androidx.annotation.NonNull;

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.annotation.RouterService;

import java.io.ByteArrayOutputStream;

/**
 * pack Installment
 */
@RouterService(interfaces = IPacker.class, key = OnlinePacketConst.PACKET_INSTALLMENT)
public class PackInstall extends PackIso8583 {

    private static final String TAG = "PackInstall";

    public enum Plan {NO_IPP, NORMAL_IPP, FIX_RATE_IPP, SPECIAL_IPP, PROMO_FIXED_RATE_IPP}

    public PackInstall() {
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
            setBitData52(transData);
            setBitData55(transData);
            setBitData62(transData);
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
        String f63 = "58"
                .concat(String.format("%d", transData.getPaymentPlan()))
                .concat(getPaddedString(transData.getPaymentTerm(), 2))
                .concat(getPaddedString(transData.getProductCode(), 9))
                .concat(getPaddedString(transData.getProductSN(), 20));
        String len = String.format("%04d", f63.length());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            buffer.write(asciiToBin(len));
            buffer.write(f63.getBytes());
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
        }
        transData.setOrigField63(buffer.toByteArray());
        LogUtils.i(TAG, "f63:" + f63);
        setBitData("63", buffer.toByteArray());
        //setBitData("63", f63.getBytes());
    }

}
