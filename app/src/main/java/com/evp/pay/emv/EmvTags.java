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
package com.evp.pay.emv;

import androidx.annotation.NonNull;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.IEmvBase;
import com.evp.eemv.entity.TagsTable;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.impl.GL;
import com.pax.gl.pack.ITlv;
import com.pax.gl.pack.exception.TlvException;

/**
 * The type Emv tags.
 */
public class EmvTags {

    private static final String TAG = "EmvTags";
    /**
     * The Sale.
     */
    static final int[] SALE = {0x5F2A, 0x82, 0x84, 0x95, 0x9A, 0x9C, 0x9F02, 0x9F03, 0x9F09, 0x9F10, 0x9F1A,
            0x9F1E, 0x9F26, 0x9F27, 0x9F33, 0x9F34, 0x9F35, 0x9F36, 0x9F37, 0x9F41, 0x9F53,
            0x5F34};
    /**
     * The Sale for JCB.
     */
    static final int[] SALE_JCB = {0x5F2A, 0x82, 0x84, 0x95, 0x9A, 0x9C, 0x9F02, 0x9F03, 0x9F09, 0x9F10, 0x9F1A,
            0x9F1E, 0x9F26, 0x9F27, 0x9F33, 0x9F34, 0x9F35, 0x9F36, 0x9F37, 0x9F41, 0x9F53, 0x4F, 0x9F6E, 0x9F7C};
    /**
     * reversal
     */
    static final int[] DUP = {0x5F2A, 0x82, 0x84, 0x95, 0x9A, 0x9C, 0x9F02, 0x9F03, 0x9F09, 0x9F10, 0x9F1A,
            0x9F1E, 0x9F26, 0x9F27, 0x9F33, 0x9F34, 0x9F35, 0x9F36, 0x9F37, 0x9F41, 0x9F53,
            0x5F34};
    /**
     * reversal for JCB.
     */
    static final int[] DUP_JCB = {0x5F2A, 0x82, 0x84, 0x95, 0x9A, 0x9C, 0x9F02, 0x9F03, 0x9F09, 0x9F10, 0x9F1A,
            0x9F1E, 0x9F26, 0x9F27, 0x9F33, 0x9F34, 0x9F35, 0x9F36, 0x9F37, 0x9F41, 0x9F53, 0x4F, 0x9F6E, 0x9F7C};

    /**
     * 交易承兑但卡片拒绝时发起的冲正
     */
    static final int[] POS_ACCEPT_DUP = {0x95, 0x9F10, 0x9F1E, 0x9F36, 0xDF31};

    private EmvTags() {

    }

    /**
     * generate field 55 by transaction type
     *
     * @param emv       the emv
     * @param transType type
     * @param isDup     is reversal
     * @return data of field 55
     */
    @NonNull
    public static byte[] getF55(IEmvBase emv, ETransType transType, boolean isDup) {
        int[] tagList = null;
        final byte[] aid = emv.getTlv(TagsTable.AID);
        final String aidStr = ConvertHelper.getConvert().bcdToStr(aid);
        LogUtils.i(TAG, String.format("%s%s%s%s", "Getting ISO8583 field55 for AID: ", aidStr, ", TransType: ", transType.getTransName()));
        switch (transType) {
//region OLS IPP
            case OLS_ENQUIRY:
            case REDEEM:
            case INSTALLMENT:
//endregion
            case SALE:
            case PREAUTH:
                if(aidStr.contains(AppConstants.JCB_CARD_RID)) {
                    tagList = isDup ? DUP_JCB : SALE_JCB;
                } else {
                    tagList = isDup ? DUP : SALE;
                }
                break;
            default:
                break;
        }
        if(tagList != null) {
            byte[] fld55Data = getValueList(emv, tagList);
            LogUtils.hex(TAG, "Field 55 for host: ", fld55Data);
            return fld55Data;
        }
        return "".getBytes();
    }

    /**
     * Get f 55 for pos accp dup byte [ ].
     *
     * @param emv the emv
     * @return the byte [ ]
     */
    @NonNull
    public static byte[] getF55forPosAccpDup(IEmvBase emv) {
        return getValueList(emv, POS_ACCEPT_DUP);
    }

    @NonNull
    private static byte[] getValueList(IEmvBase emv, int[] tags) {
        if (tags == null || tags.length == 0) {
            return "".getBytes();
        }

        ITlv tlv = GL.getGL().getPacker().getTlv();
        ITlv.ITlvDataObjList tlvList = tlv.createTlvDataObjectList();
        for (int tag : tags) {
            byte[] value = emv.getTlv(tag);
            if (value == null || value.length == 0) {
                if (tag == 0x9f03) {
                    value = new byte[6];
                } else {
                    continue;
                }
            }
            try {
                ITlv.ITlvDataObj obj = tlv.createTlvDataObject();
                obj.setTag(tag);
                obj.setValue(value);
                tlvList.addDataObj(obj);
            } catch (Exception e) {
                LogUtils.i(TAG, "", e);
            }
        }

        try {
            return tlv.pack(tlvList);
        } catch (TlvException e) {
            LogUtils.e(TAG, "", e);
        }

        return "".getBytes();
    }
}
