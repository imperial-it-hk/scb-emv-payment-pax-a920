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
import static com.evp.commonlib.utils.ConvertUtils.binToAscii;
import static com.evp.commonlib.utils.ConvertUtils.getPaddedString;

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
 * pack Redeem
 */
@RouterService(interfaces = IPacker.class, key = OnlinePacketConst.PACKET_REDEEM)
public class PackRedeem extends PackIso8583 {

    private static final String TAG = "PackRedeem";
    //private static final String OLS_VERSION = "02000100";
    private static final String OLS_VERSION = "13000100";
    private static final String LMIC_SPECIAL_PRODUCT = "L02";
    private static final String LMIC_BURN_POINT = "L01";
    private static final String LMIC_REDEEM_POINT = "X02";
    private static final String LMIC_REDEEM_DISCOUNT = "X03";

    public enum Plan {NO_REDEEM, VOUCHER, POINT_CREDIT, DISCOUNT, SPECIAL_REDEEM, ENQUIRY}

    public PackRedeem() {
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
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        Plan plan = ConvertUtils.getEnum(Plan.class, transData.getPaymentPlan());
        if (plan == Plan.POINT_CREDIT || plan == Plan.DISCOUNT)
            super.setBitData4(transData);
        else
            setBitData("4", "0");
    }


    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {

        String f63 = "";

        Plan plan = ConvertUtils.getEnum(Plan.class, transData.getPaymentPlan());
        switch (plan) {
            case SPECIAL_REDEEM:
                f63 = OLS_VERSION
                        .concat(binToAscii(LMIC_SPECIAL_PRODUCT.getBytes()))
                        .concat(binToAscii("000".getBytes()))
                        .concat("01")
                        .concat(binToAscii(String.format("%-20s", transData.getProductCode()).getBytes())) //right pad space
                        .concat(getPaddedString(transData.getRedeemQty(), 12))
                        .concat(getPaddedString("", 12))
                        .concat(getPaddedString("", 12))
                        .concat(getPaddedString("", 16))
                ;
                break;

            case VOUCHER:
                f63 = OLS_VERSION
                        .concat(binToAscii(LMIC_BURN_POINT.getBytes()))
                        .concat(binToAscii("001".getBytes()))
                        .concat(getPaddedString("", 12))
                        .concat("01")
                        .concat(binToAscii("000000".getBytes()))
                        .concat(getPaddedString(transData.getRedeemQty(), 10) + "00")
                        .concat(getPaddedString("", 2))
                        .concat(getPaddedString("", 12))
                        .concat(getPaddedString("", 16))
                ;
                break;

            case DISCOUNT:
            case POINT_CREDIT:
                f63 = OLS_VERSION
                        .concat(binToAscii((plan == Plan.DISCOUNT ? LMIC_REDEEM_DISCOUNT : LMIC_REDEEM_POINT).getBytes()))
                        .concat(binToAscii("1  ".getBytes()))
                        .concat(getPaddedString("", 12))
                        .concat("00")
                        .concat(binToAscii("000000".getBytes()))
                        .concat(getPaddedString(transData.getRedeemQty(), 10) + "00")
                        .concat(getPaddedString("", 2))
                        .concat(getPaddedString("", 12))
                        .concat(getPaddedString("", 16))
                ;
                break;

        }

        //String len = String.format("%04d", f63.length() / 2);
        LogUtils.i(TAG, "f63:" + f63);
        transData.setOrigField63(asciiToBin(f63));
        setBitData("63", asciiToBin(f63));
    }

}
