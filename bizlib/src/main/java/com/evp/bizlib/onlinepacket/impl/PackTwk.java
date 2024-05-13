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
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.bizlib.tle.TleConst;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.annotation.RouterService;

/**
 * pack echo
 */
@RouterService(interfaces = IPacker.class, key = OnlinePacketConst.PACKET_TWK_DOWNLOAD)
public class PackTwk extends PackIso8583 {
    private static final String TAG = PackTwk.class.getSimpleName();

    public PackTwk() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "TWK ISO pack START");

        try {
            setHeader(transData);
            setBitData3(transData);
            setBitData11(transData);
            setBitData24(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData62(transData);
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "TWK ISO pack END");

        return pack(transData, false);
    }

    @Override
    protected void setBitData24(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("24", transData.getAcquirer().getTleKmsNii());
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        StringBuilder beginingData = new StringBuilder();
        beginingData.append(TleConst.TLE_HEAD);
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleVersion(), 2));
        beginingData.append(TleConst.RQ_TYPE);
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleAcquirerlId(), 3));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleAcquirerlId(), 3));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTerminalId(), 8));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleVendorId(), 8));

        byte[] tmkId;
        if(transData.getAcquirer().getTleCurrentTmkId() == null) {
            tmkId = new byte[]{0x00, 0x00, 0x00, 0x00};
        } else {
            tmkId = transData.getAcquirer().getTleCurrentTmkId();
        }

        byte[] twkId;
        if(transData.getAcquirer().getTleCurrentTwkId() == null) {
            twkId = new byte[]{0x00, 0x00, 0x00, 0x00};
        } else {
            twkId = transData.getAcquirer().getTleCurrentTwkId();
        }

        int length = beginingData.toString().length() + tmkId.length + twkId.length;
        final String acqName = transData.getAcquirer().getName();
        final boolean pinKeyRequired = !acqName.equals(AppConstants.DCC_ACQUIRER);
        if(pinKeyRequired) {
            length += TleConst.PIN_KEY_RQ.length();
        }
        byte[] f62 = new byte[length];
        int position = 0;

        System.arraycopy(beginingData.toString().getBytes(), 0, f62, 0, beginingData.toString().length());
        position += beginingData.toString().length();

        System.arraycopy(tmkId, 0, f62, position, tmkId.length);
        position += tmkId.length;

        System.arraycopy(twkId, 0, f62, position, twkId.length);
        position += twkId.length;

        if(pinKeyRequired) {
            LogUtils.i(TAG, "PIN key requested");
            System.arraycopy(TleConst.PIN_KEY_RQ.getBytes(), 0, f62, position, TleConst.PIN_KEY_RQ.length());
        }

        setBitData("62", f62);
    }
}

