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

import static com.evp.commonlib.utils.ConvertUtils.binToAscii;

import androidx.annotation.NonNull;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.annotation.RouterService;

import org.greenrobot.greendao.annotation.NotNull;

/**
 * pack SaleVoid
 */
@RouterService(interfaces = IPacker.class,key = OnlinePacketConst.PACKET_VOID)
public class PackSaleVoid extends PackIso8583 {

    public PackSaleVoid() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "Void ISO pack START");

        try {
            setHeader(transData);
            setBitData2(transData);
            setBitData3(transData);
            setBitData4(transData);
            setBitData11(transData);
            setBitData12(transData);
            setBitData13(transData);
            setBitData14(transData);
            setBitData22(transData);
            setBitData23(transData);
            setBitData24(transData);
            setBitData25(transData);
            setBitData35(transData);
            setBitData37(transData);
            setBitData38(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData55(transData);
            setBitData62(transData);

            //DCC extra ISO fields
            if(transData.getAcquirer().getName().equals(AppConstants.DCC_ACQUIRER)) {
                setBitData6(transData);
                setBitData10(transData);
                setBitData51(transData);
            }
            //region IPP OLS
            if (transData.getOrigTransType().equals(ETransType.INSTALLMENT.name())) {
                setBitData62(transData);
                setBitData63(transData);
            }
            if (transData.getOrigTransType().equals(ETransType.REDEEM.name())) {
                setBitData63(transData);
                PackRedeem.Plan plan = ConvertUtils.getEnum(PackRedeem.Plan.class, transData.getPaymentPlan());
                if (!(plan == PackRedeem.Plan.POINT_CREDIT || plan == PackRedeem.Plan.DISCOUNT))
                    setBitData("4", "0");
            }
            //endregion
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "Void ISO pack END");

        return pack(transData, false);
    }

    @Override
    protected void setBitData2(@NonNull TransData transData) throws Iso8583Exception {
        boolean upiAcquirer = AppConstants.UPI_ACQUIRER.equals(transData.getAcquirer().getName());
        if(transData.getEnterMode() == TransData.EnterMode.CLSS && !upiAcquirer) {
            return;
        }
        setBitData("2", transData.getPan());
    }

    @Override
    protected void setBitData3(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        ETransType origTransType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());
        if(AppConstants.UPI_ACQUIRER.equals(transData.getAcquirer().getName())
                && origTransType == ETransType.REFUND)
        {
            transData.setProcCode("220000");
            setBitData("3", "220000");
        } else {
            setBitData("3", transType != null ? transType.getProcCode() : "");
        }
    }

    @Override
    protected void setBitData12(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getOrigDateTime();
        if (temp != null && !temp.isEmpty()) {
            String time = temp.substring(8, temp.length());
            setBitData("12", time);
        }
    }

    @Override
    protected void setBitData13(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getOrigDateTime();
        if (temp != null && !temp.isEmpty()) {
            String date = temp.substring(4, 8);
            setBitData("13", date);
        }
    }

    @Override
    protected void setBitData14(@NonNull TransData transData) throws Iso8583Exception {
        if(transData.getEnterMode() == TransData.EnterMode.CLSS) {
            return;
        }
        setBitData("14", transData.getExpDate());
    }

    @Override
    protected void setBitData37(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("37", transData.getOrigRefNo());
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("62", ConvertUtils.getPaddedNumber(transData.getOrigTransNo(), 6));
    }

    @Override
    protected void setBitData63(@NonNull @NotNull TransData transData) throws Iso8583Exception {
        LogUtils.i(TAG, "f63:" + binToAscii(transData.getOrigField63()));
        setBitData("63", transData.getOrigField63());
    }
}

