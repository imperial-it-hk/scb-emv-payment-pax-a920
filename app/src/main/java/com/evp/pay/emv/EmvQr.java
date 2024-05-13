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
 * 20190108  	         liliang                 Create
 * ===========================================================================================
 */

package com.evp.pay.emv;

import android.util.ArrayMap;
import android.util.Base64;

import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.utils.Tools;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.impl.GL;
import com.pax.gl.pack.ITlv;
import com.pax.gl.pack.exception.TlvException;

import java.lang.reflect.Field;

/**
 * UPI QRCODE
 */
public class EmvQr {

    /**
     * The constant TAG.
     */
    public static final String TAG = "EmvQr";

    private static final String AID_DEBIT_APP = "A000000333010101";
    private static final String AID_CREDIT_APP = "A000000333010102";
    private static final String AID_QCREDIT_APP = "A000000333010103";

    private String mAid;
    private String mTrackData;
    private String mPan;
    private String mExpDate;
    private String mCardSeqNum;
    private String mIccData;

    private EmvQr() {

    }

    /**
     * Decode emv qr emv qr.
     *
     * @param transData the trans data
     * @param emvQr     the emv qr
     * @return the emv qr
     */
    public static EmvQr decodeEmvQr(TransData transData, String emvQr) {
        if (emvQr == null) {
            return null;
        }

        try {
            byte[] bytes = Base64.decode(emvQr, Base64.DEFAULT);
            if (bytes == null) {
                return null;
            }

            LogUtils.d(TAG, Tools.bcd2Str(bytes));

            return decodeEmvQrBcd(transData, bytes);
        } catch (IllegalArgumentException e) {
            LogUtils.e(TAG, "", e);
            return null;
        }
    }

    private static EmvQr decodeEmvQrBcd(TransData transData, byte[] emvQr) {
        if (emvQr == null || emvQr.length == 0) {
            return null;
        }

        try {
            ITlv tlv = GL.getGL().getPacker().getTlv();
            ITlv.ITlvDataObjList objList = tlv.unpack(emvQr);
            if (objList == null) {
                return null;
            }

            byte[] bytes = objList.getValueByTag(0x61);
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            objList = tlv.unpack(bytes);

            EmvQr emvQrObj = new EmvQr();
            bytes = objList.getValueByTag(0x4F);
            emvQrObj.mAid = ConvertHelper.getConvert().bcdToStr(bytes);

            bytes = objList.getValueByTag(TagsTable.TRACK2);
            emvQrObj.mTrackData = ConvertHelper.getConvert().bcdToStr(bytes);
            if (emvQrObj.mTrackData.endsWith("F")) {
                emvQrObj.mTrackData = emvQrObj.mTrackData.substring(0, emvQrObj.mTrackData.length
                        () - 1);
            }
            emvQrObj.mPan = TrackUtils.getPan(emvQrObj.mTrackData);
            emvQrObj.mExpDate = TrackUtils.getExpDate(emvQrObj.mTrackData);

            bytes = objList.getValueByTag(0x5F34);
            emvQrObj.mCardSeqNum = ConvertHelper.getConvert().bcdToStr(bytes);

            // CouponNum 0x9F60

            bytes = objList.getValueByTag(0x63);
            emvQrObj.mIccData = createIccData(transData, bytes);

            return emvQrObj;

        } catch (Exception e) {
            LogUtils.e(TAG, "Failed to decode EMV QR.", e);
        }
        return null;
    }

    private static String createIccData(TransData transData, byte[] iccBytes) {
        if (iccBytes == null) {
            throw new IllegalArgumentException("iccBytes is null.");
        }

        ArrayMap<Integer, String> map = new ArrayMap<>(9);
        map.put(0x9F37, "12345678");
        map.put(0x95, "0000000800");
        map.put(TagsTable.TRANS_DATE, "010101");
        map.put(0x9C, "00");
        map.put(TagsTable.AMOUNT, "000000000001");
        map.put(TagsTable.CURRENCY_CODE, "0156");
        map.put(TagsTable.COUNTRY_CODE, "0156");
        map.put(TagsTable.AMOUNT_OTHER, "000000000000");
        map.put(TagsTable.TERMINAL_CAPABILITY, "E0E8C0");

        ITlv tlv = GL.getGL().getPacker().getTlv();
        ITlv.ITlvDataObjList iccDataList;
        try {
            iccDataList = tlv.unpack(iccBytes);
            for (Integer tag : map.keySet()) {
                if (iccDataList.getByTag(tag) == null) {
                    ITlv.ITlvDataObj obj = tlv.createTlvDataObject();
                    obj.setTag(tag);
                    byte[] value = ConvertHelper.getConvert().strToBcdPaddingRight(map.get(tag));
                    obj.setValue(value);
                    iccDataList.addDataObj(obj);
                }
            }

            byte[] bytes = tlv.pack(iccDataList);
            return ConvertHelper.getConvert().bcdToStr(bytes);
        } catch (TlvException e) {
            LogUtils.e(TAG, "Failed to decode icc data from EMV QR.", e);
        }

        return null;
    }

    /**
     * Is upi aid boolean.
     *
     * @return the boolean
     */
    public boolean isUpiAid() {
        if (mAid == null) {
            return false;
        }

        switch (mAid) {
            case AID_DEBIT_APP:
            case AID_CREDIT_APP:
            case AID_QCREDIT_APP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets aid.
     *
     * @return the aid
     */
    public String getAid() {
        return mAid;
    }

    /**
     * Gets track data.
     *
     * @return the track data
     */
    public String getTrackData() {
        return mTrackData;
    }

    /**
     * Gets pan.
     *
     * @return the pan
     */
    public String getPan() {
        return mPan;
    }

    /**
     * Gets expire date.
     *
     * @return the expire date
     */
    public String getExpireDate() {
        return mExpDate;
    }

    /**
     * Gets card seq num.
     *
     * @return the card seq num
     */
    public String getCardSeqNum() {
        return mCardSeqNum;
    }

    /**
     * Gets icc data.
     *
     * @return the icc data
     */
    public String getIccData() {
        return mIccData;
    }

    @Override
    public String toString() {
        Field[] fields = EmvQr.class.getDeclaredFields();
        StringBuilder stringBuilder = new StringBuilder();
        for (Field field : fields) {
            try {
                Object value = field.get(this);
                stringBuilder.append("[").append(field.getName()).append(":").append(value).append("]\n");
                LogUtils.d(TAG, "[" + field.getName() + ":" + value + "]");
            } catch (IllegalAccessException e) {
                LogUtils.e(TAG, "", e);
            }
        }
        return stringBuilder.toString();
    }
}
