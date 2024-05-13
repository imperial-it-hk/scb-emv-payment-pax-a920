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
/**
 * pack echo
 */
@RouterService(interfaces = IPacker.class, key = OnlinePacketConst.PACKET_BATCH_UP)
public class PackBatchUp extends PackIso8583 {

    public PackBatchUp() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "Batch upload ISO pack START");

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
            setBitData37(transData);
            setBitData38(transData);
            setBitData39(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData54(transData);
            setBitData55(transData);
            setBitData60(transData);
            setBitData62(transData);

            //DCC extra ISO fields
            if(transData.getAcquirer().getName().equals(AppConstants.DCC_ACQUIRER)) {
                setBitData6(transData);
                setBitData10(transData);
                setBitData51(transData);
            }
            //region IPP OLS
            if (transData.getOrigTransType().equals(ETransType.INSTALLMENT.name())) {
                setBitData63(transData);
            }
            if (transData.getOrigTransType().equals(ETransType.REDEEM.name())) {
                setBitData63(transData);
                PackRedeem.Plan plan = ConvertUtils.getEnum(PackRedeem.Plan.class, transData.getPaymentPlan());
                if (!(plan == PackRedeem.Plan.POINT_CREDIT || plan == PackRedeem.Plan.DISCOUNT))
                    setBitData("4", "0");
            }
            //endregion
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "Batch upload ISO pack END");

        return pack(transData, false);
    }

    @Override
    protected void setBitData2(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("2", transData.getPan());
    }

    @Override
    protected void setBitData3(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("3", transData.getProcCode());
    }

    @Override
    protected void setBitData25(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());
        setBitData("25", transType != null ? transType.getServiceCode() : "");
    }

    @Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());
        String data = transType != null ? transType.getMsgType() : "    ";
        data += ConvertUtils.getPaddedNumber(transData.getOrigStanNo(), 6);
        data += "            ";
        setBitData("60", data);
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("62", ConvertUtils.getPaddedNumber(transData.getOrigTransNo(), 6));
    }

    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("63", transData.getOrigField63());
    }
}
