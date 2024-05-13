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
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.bizlib.tle.TlePacker;
import com.evp.bizlib.tle.TleUtils;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.impl.GL;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.IIso8583;
import com.pax.gl.pack.exception.Iso8583Exception;

import org.greenrobot.greendao.annotation.NotNull;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class PackIso8583 implements IPacker<TransData, byte[]> {

    protected static final String TAG = "PackIso8583";

    private IIso8583 iso8583;
    private IIso8583.IIso8583Entity entity;
    private Map<String, byte[]> iso8583BinData = new HashMap<>();
    private Map<String, String> iso8583StrData = new HashMap<>();
    private int iso8583HeaderSize;

    private static final Map<TransData.EnterMode, String> enterModeMap = new EnumMap<TransData.EnterMode, String>(TransData.EnterMode.class);

    static {
        enterModeMap.put(TransData.EnterMode.MANUAL, "01");
        enterModeMap.put(TransData.EnterMode.SWIPE, "02");
        enterModeMap.put(TransData.EnterMode.INSERT, "05");
        enterModeMap.put(TransData.EnterMode.CLSS, "07");
        enterModeMap.put(TransData.EnterMode.FALLBACK, "80");
        enterModeMap.put(TransData.EnterMode.QR, "03");
    }

    public PackIso8583() {
        initEntity();
    }

    /**
     * 获取打包entity
     *
     * @return
     */
    private void initEntity() {
        iso8583 = GL.getGL().getPacker().getIso8583();
        try {
            entity = iso8583.getEntity();
            entity.loadTemplate(BaseApplication.getAppContext().getResources().getAssets().open("scb8583.xml"));
        } catch (Iso8583Exception | IOException | XmlPullParserException e) {
            LogUtils.e(TAG, "", e);
        }
    }

    protected final void setBitData(String field, String value) throws Iso8583Exception {
        if (value != null && !value.isEmpty()) {
            iso8583StrData.put(field, value);
            entity.setFieldValue(field, value);
        }
    }

    protected final void setBitData(String field, byte[] value) throws Iso8583Exception {
        if (value != null && value.length > 0) {
            iso8583BinData.put(field, value);
            entity.setFieldValue(field, value);
        }
    }

    @NonNull
    protected byte[] pack(@NotNull TransData transData, boolean isNeedMac) throws PedDevException {
        if(TleUtils.isTrxTle(transData)) {
            byte[] origIsoMsg;
            try {
                origIsoMsg = iso8583.pack();
            } catch (Iso8583Exception e) {
                LogUtils.e(TAG, "", e);
                return "".getBytes();
            }
            return new TlePacker().pack(transData, iso8583BinData, iso8583StrData, origIsoMsg);
        }

        iso8583BinData.clear();
        iso8583StrData.clear();

        byte[] packData = "".getBytes();
        try {
            if (isNeedMac) {
                setBitData("64", new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
            }
            // for debug entity.dump();
            packData = iso8583.pack();

        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }
        if (isNeedMac) {
            if (packData == null || packData.length == 0) {
                return "".getBytes();
            }

            int len = packData.length;

            byte[] calMacBuf = new byte[len - 11 - 8];//去掉header和mac
            System.arraycopy(packData, 11, calMacBuf, 0, len - 11 - 8);
            byte[] mac = PedHelper.getMac(KeyUtils.getTakIndex(transData.getAcquirer().getTleKeySetId()), calMacBuf);
            if (mac.length == 0) {
                return "".getBytes();
            }
            System.arraycopy(mac, 0, packData, len - 8, 8);
        }
        return packData;
    }

    @Override
    public int unpack(@NonNull TransData transData, final byte[] rsp) throws PedDevException {
        HashMap<String, byte[]> map;
        byte[] buff;

        try {
            map = iso8583.unpack(rsp, true);
            entity.dump();
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "Failed to unpack ISO8583. Exception happened.", e);
            return TransResult.ERR_UNPACK;
        }

        //Header and TPDU
        byte[] header = map.get("h");
        String rspTpdu = new String(header).substring(0, 10);
        String reqTpdu = transData.getTpdu();
        if (!rspTpdu.substring(2, 6).equals(reqTpdu.substring(6, 10))
                || !rspTpdu.substring(6, 10).equals(reqTpdu.substring(2, 6)))
        {
            LogUtils.e(TAG, "Failed to unpack ISO8583. TPDU is different.");
            return TransResult.ERR_UNPACK;
        }
        transData.setHeader(new String(header).substring(10));

        //UPI special behavior for ECHO test
        if(transData.getAcquirer().getName().equals(AppConstants.UPI_ACQUIRER)
                && ETransType.ECHO.name().equals(transData.getTransType()))
        {
            transData.setResponseCode("00");
        } else {
            //Fld 39 - response code
            buff = map.get("39");
            if (buff == null) {
                LogUtils.e(TAG, "Failed to unpack ISO8583. Field 39 is missing.");
                return TransResult.ERR_PACKET;
            }
            //TLE special error response code
            if (buff[0] == 'L' && buff[1] == 'E') {
                transData.setResponseCode(TleUtils.getTleErrorCode(map.get("63")));
            } else {
                transData.setResponseCode(new String(buff));
            }
        }

        //Some hosts for some trx do not send all mandatory ISO data when trx is not success.
        //We need to skip function checkRecvData so proper response message will be displayed to cashier.
        if("00".equals(transData.getResponseCode()))
        {
            //Compare received data with sent data
            int ret = checkRecvData(map, transData);
            if (ret != TransResult.SUCC) {
                return ret;
            }
        }

        //Fld 4 - Amount
        buff = map.get("4");
        if (buff != null && buff.length > 0) {
            transData.setAmount(new String(buff));
        }

        //Fld 6 - DCC Currency Amount, Trans.
        buff = map.get("6");
        if (buff != null && buff.length > 0) {
            transData.setDccForeignAmount(new String(buff));
        }

        //Fld 10 - Conversion Rate
        buff = map.get("10");
        if (buff != null && buff.length > 0) {
            transData.setDccExchangeRate(new String(buff));
        }

        //Fld 13 - Date
        String dateTime = "";
        buff = map.get("13");
        if (buff != null) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            dateTime = Integer.toString(year) + new String(buff);
        }

        //Fld 12 - Time
        buff = map.get("12");
        if (buff != null && buff.length > 0) {
            transData.setDateTime(dateTime + new String(buff));
        }

        //Fld 14 - Expitation date
        buff = map.get("14");
        if (buff != null && buff.length > 0) {
            String expDate = new String(buff);
            if (!"0000".equals(expDate)) {
                transData.setExpDate(expDate);
            }
        }

        //Fld 23 - PAN sequence number
        buff = map.get("23");
        if (buff != null && buff.length > 0) {
            transData.setCardSerialNo(new String(buff));
        }

        //Fld 37 - Retrieval reference number (RRN)
        buff = map.get("37");
        if (buff != null && buff.length > 0) {
            transData.setRefNo(new String(buff));
        }

        //Fld 38 - Authorization code
        buff = map.get("38");
        if (buff != null && buff.length > 0) {
            transData.setAuthCode(new String(buff));
        }

        //Fld 51 - DCC Currency Code
        buff = map.get("51");
        if (buff != null && buff.length > 0) {
            transData.setDccCurrencyCode(new String(buff));
        }

        //Fld 55 - ICC data
        buff = map.get("55");
        if (buff != null && buff.length > 0) {
            transData.setRecvIccData(buff);
        }

        //Fld 57 - TLE data
        //Will be parsed in unpackTle function

        //Fld 62 - TLE key download data
        buff = map.get("62");
        if (buff != null && buff.length > 0) {
            transData.setField62(buff);
        }

        //Fld 63 - private data
        buff = map.get("63");
        if (buff != null && buff.length > 0) {
            transData.setField63(buff);
        }

        //Fld 64 - MAC
        buff = map.get("64");
        if (buff != null && buff.length > 0) {
            if(!TleUtils.isTrxTle(transData)) {
                byte[] data = new byte[rsp.length - 11 - 8];
                System.arraycopy(rsp, 11, data, 0, data.length);
                byte[] mac = PedHelper.getMac(KeyUtils.getTakIndex(transData.getAcquirer().getTleKeySetId()), data);
                if (!ConvertHelper.getConvert().isByteArrayValueSame(buff, 0, mac, 0, 8)) {
                    LogUtils.e(TAG, "Failed to unpack ISO8583. MAC is not correct.");
                    return TransResult.ERR_MAC;
                }
            }
        }

        if(TleUtils.isTrxTle(transData)) {
            return new TlePacker().unpack(transData, map);
        }

        return TransResult.SUCC;
    }

    /**
     * 检查请求和返回的关键域field4, field11, field41, field42
     *
     * @param map        解包后的map
     * @param transData  请求
     * @return
     */
    protected int checkRecvData(@NonNull HashMap<String, byte[]> map, @NonNull TransData transData) {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        ETransType origTransType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());
        boolean checkAmt = true;

        if (transType == ETransType.SETTLE
                || transType == ETransType.ECHO
                || transType == ETransType.BATCH_UP
                || transType == ETransType.REDEEM
                || transType == ETransType.OLS_ENQUIRY
                || transType == ETransType.TMK_DOWNLOAD
                || transType == ETransType.TWK_DOWNLOAD
                || origTransType == ETransType.REDEEM)
        {
            checkAmt = false;
        }

        if (checkAmt && !checkAmount(map, transData)) {
            LogUtils.e(TAG, "Failed to unpack ISO8583. Transaction amount is different.");
            return TransResult.ERR_TRANS_AMT;
        }

        if (!checkStanNo(map, transData)) {
            LogUtils.e(TAG, "Failed to unpack ISO8583. Stan number is different.");
            return TransResult.ERR_STAN_NO;
        }

        if (!checkTerminalId(map, transData)) {
            LogUtils.e(TAG, "Failed to unpack ISO8583. Terminal ID is different.");
            return TransResult.ERR_TERM_ID;
        }

        if (!checkMerchantId(map, transData)) {
            LogUtils.e(TAG, "Failed to unpack ISO8583. Merchant ID is different.");
            return TransResult.ERR_MERCH_ID;
        }

        if (!checkProcessingCode(map, transData)) {
            LogUtils.e(TAG, "Failed to unpack ISO8583. Processing code is different.");
            return TransResult.ERR_PROC_CODE;
        }

        return TransResult.SUCC;
    }

    protected void setHeader(@NonNull TransData transData) throws Iso8583Exception {
        // h
        String tpdu;
        if(TleUtils.isTrxTle(transData)) {
            tpdu = "600" + transData.getAcquirer().getTleNii() + "0000";
        } else if(TleUtils.isTrxTleKeyDL(transData)) {
            tpdu = "600" + transData.getAcquirer().getTleKmsNii() + "0000";
        } else {
            tpdu = "600" + transData.getAcquirer().getNii() + "0000";
        }
        String pHeader = tpdu + transData.getHeader();
        entity.setFieldValue("h", pHeader);
        transData.setTpdu(tpdu);

        // m
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL) {
            entity.setFieldValue("m", transType != null ? transType.getDupMsgType() : "");
        } else {
            entity.setFieldValue("m", transType != null ? transType.getMsgType() : "");
        }
    }

    protected void setBitData1(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData2(@NonNull TransData transData) throws Iso8583Exception {
        TransData.EnterMode enterMode = transData.getEnterMode();
        String aid = transData.getAid();

        if (enterMode == TransData.EnterMode.MANUAL
                || (enterMode == TransData.EnterMode.CLSS && aid != null && aid.contains(AppConstants.MASTER_CARD_RID)))
        {
            setBitData("2", transData.getPan());
        }
    }

    protected void setBitData3(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        setBitData("3", transType != null ? transType.getProcCode() : "");
    }

    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("4", transData.getAmount());
    }

    protected void setBitData5(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData6(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("6", transData.getDccForeignAmount());
    }

    protected void setBitData7(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData8(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData9(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData10(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("10", transData.getDccExchangeRate());
    }

    protected void setBitData11(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("11", String.valueOf(transData.getStanNo()));
    }

    protected void setBitData12(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getDateTime();
        if (temp != null && !temp.isEmpty()) {
            String time = temp.substring(8, temp.length());
            setBitData("12", time);
        }
    }

    protected void setBitData13(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getDateTime();
        if (temp != null && !temp.isEmpty()) {
            String date = temp.substring(4, 8);
            setBitData("13", date);
        }
    }

    protected void setBitData14(@NonNull TransData transData) throws Iso8583Exception {
        TransData.EnterMode enterMode = transData.getEnterMode();

        if (enterMode == TransData.EnterMode.MANUAL) {
            setBitData("14", transData.getExpDate());
        }
    }

    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData16(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData17(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData18(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData19(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData20(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData21(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData22(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("22", getInputMethod(transData));
    }

    protected void setBitData23(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("23", transData.getCardSerialNo());
    }

    protected void setBitData24(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("24", transData.getNii());
    }

    protected void setBitData25(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        setBitData("25", transType != null ? transType.getServiceCode() : "");
    }

    protected void setBitData26(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData27(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData29(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData30(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData31(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData34(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData35(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("35", transData.getTrack2());
    }

    protected void setBitData36(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("36", transData.getTrack3());
    }

    protected void setBitData37(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("37", transData.getRefNo());
    }

    protected void setBitData38(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("38", transData.getAuthCode());
    }

    protected void setBitData39(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("39", transData.getResponseCode());
    }

    protected void setBitData40(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData41(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("41", transData.getAcquirer().getTerminalId());
    }

    protected void setBitData42(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("42", transData.getAcquirer().getMerchantId());
    }

    protected void setBitData43(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData44(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData45(@NonNull TransData transData) throws Iso8583Exception {
        TransData.EnterMode enterMode = transData.getEnterMode();

        if (enterMode == TransData.EnterMode.SWIPE) {
            setBitData("45", transData.getTrack1());
        }
    }

    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("48", transData.getField48());
    }

    protected void setBitData51(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("51", transData.getDccCurrencyCode());
    }

    protected void setBitData52(@NonNull TransData transData) throws Iso8583Exception {
        if (transData.isHasPin() && transData.getPin() != null) {
            setBitData("52", ConvertHelper.getConvert().strToBcdPaddingLeft(transData.getPin()));
        }
    }

    protected void setBitData54(@NonNull TransData transData) throws Iso8583Exception {
        String AddAmount = transData.getTipAmount();

        if(AddAmount != null && AddAmount.length() > 0 && !AddAmount.contains("0")) {
            setBitData("54", ConvertUtils.getPaddedNumber(ConvertUtils.parseLongSafe(transData.getTipAmount(), 0), 12));
        }
    }

    protected void setBitData55(@NonNull TransData transData) throws Iso8583Exception {
        String temp;

        if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL)
            temp = transData.getDupIccData();
        else
            temp = transData.getSendIccData();

        if (temp != null && temp.length() > 0) {
            setBitData("55", ConvertHelper.getConvert().strToBcdPaddingLeft(temp));
        }
    }

    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("60", ConvertUtils.getPaddedNumber(transData.getBatchNo(), 6));
    }

    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("62", ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6));
    }

    // set field 63
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("63", transData.getField63());
    }

    protected final String getInputMethod(final TransData transData) {
        final TransData.EnterMode enterMode = transData.getEnterMode();
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        String acquirer = transData.getAcquirer().getName();
        String inputMethod;
        String issuer = transData.getIssuer() == null ? "" : transData.getIssuer().getName();
        try {
            //UPI logic except VOID trx
            if(AppConstants.UPI_ACQUIRER.equals(acquirer) && transType != ETransType.VOID) {
                //UPI acquirer - "1" Online PIN. "2" Offline PIN or No PIN
                if(transData.isHasPin() && transData.getPin() != null && !transData.getPin().isEmpty()) {
                    inputMethod = enterModeMap.get(enterMode) + "1";
                } else {
                    inputMethod = enterModeMap.get(enterMode) + "2";
                }
            } else if(transType == ETransType.VOID) {
                //Void is triggered manually hence enter mode MANUAL
                if(AppConstants.TPN_ISSUER.equals(issuer)) {
                    inputMethod = enterModeMap.get(TransData.EnterMode.MANUAL) + "2";
                } else {
                    inputMethod = enterModeMap.get(TransData.EnterMode.MANUAL) + "1";
                }
            } else {
                inputMethod = enterModeMap.get(enterMode) + "1";
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
            return null;
        }

        return inputMethod;
    }

    private boolean checkAmount(@NonNull final HashMap<String, byte[]> map, @NonNull final TransData transData) {
        byte[] data = map.get("4");
        if (data != null && data.length > 0) {
            String temp = new String(data);
            if (ConvertUtils.parseLongSafe(temp, 0) != ConvertUtils.parseLongSafe(transData.getAmount(), 0)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkStanNo(@NonNull final HashMap<String, byte[]> map, @NonNull final TransData transData) {
        byte[] data = map.get("11");
        if (data != null && data.length > 0) {
            String temp = new String(data);
            if (!temp.equals(ConvertUtils.getPaddedNumber(transData.getStanNo(), 6))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkTerminalId(@NonNull final HashMap<String, byte[]> map, @NonNull final TransData transData) {
        byte[] data = map.get("41");
        if (data != null && data.length > 0) {
            String temp = new String(data);
            if (!temp.equals(transData.getAcquirer().getTerminalId())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMerchantId(@NonNull final HashMap<String, byte[]> map, @NonNull final TransData transData) {
        byte[] data = map.get("42");
        if (data != null && data.length > 0) {
            String temp = new String(data);
            if (!temp.equals(transData.getAcquirer().getMerchantId())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkProcessingCode(@NonNull final HashMap<String, byte[]> map, @NonNull final TransData transData) {
        byte[] data = map.get("3");
        if (data != null && data.length > 0) {
            String temp = new String(data);
            if (!temp.equals(transData.getProcCode())) {
                return false;
            }
        }
        return true;
    }
}
