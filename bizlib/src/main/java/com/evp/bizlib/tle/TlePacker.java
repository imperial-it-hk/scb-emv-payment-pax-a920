package com.evp.bizlib.tle;

import androidx.annotation.NonNull;

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.convert.IConvert;
import com.evp.poslib.gl.impl.GL;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.IIso8583;
import com.pax.gl.pack.ITlv;
import com.pax.gl.pack.exception.Iso8583Exception;

import org.greenrobot.greendao.annotation.NotNull;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TlePacker {
    private static final String TAG = TlePacker.class.getSimpleName();

    private IIso8583 iso8583;
    private IIso8583.IIso8583Entity entity;
    private Map<String, byte[]> iso8583BinData;
    private Map<String, String> iso8583StrData;
    private int iso8583HeaderSize;

    public byte[] pack(@NotNull TransData transData, @NotNull Map<String, byte[]> isoBinData,
                       @NotNull Map<String, String> isoStrData,
                       @NotNull byte[] origIsoMsg) throws PedDevException {
        iso8583BinData = isoBinData;
        iso8583StrData = isoStrData;

        LogUtils.hex(TAG, "Original ISO: ", origIsoMsg);

        //Construct original ISO8583 message
        try {
            //Reset ISO object
            initEntity();
            setHeader(transData);
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }
        if (origIsoMsg == null || origIsoMsg.length == 0) {
            LogUtils.e(TAG, "ISO8583 message is empty!");
            return "".getBytes();
        }

        //Mark field 64 in message bitmap
        origIsoMsg[iso8583HeaderSize + TleConst.MSG_TYPE_SIZE + TleConst.MAC_BITMAP_POS] |= 1 << 0;
        LogUtils.hex(TAG, "Original ISO with field64 marked: ", origIsoMsg);

        //Calculate mac value over original ISO message without header
        byte[] mac;
        int lengthOfMacBuff = origIsoMsg.length - iso8583HeaderSize;
        byte[] calMacBuf = new byte[lengthOfMacBuff];
        System.arraycopy(origIsoMsg, iso8583HeaderSize, calMacBuf, 0, lengthOfMacBuff);
        LogUtils.hex(TAG, "Data for MAC calculation: ", calMacBuf);
        try {
            mac = PedHelper.getTleSha1Mac(KeyUtils.getTakIndex(transData.getAcquirer().getTleKeySetId()), calMacBuf);
        } catch(PedDevException e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }
        if (mac == null || mac.length == 0) {
            LogUtils.e(TAG, "No MAC data!");
            return "".getBytes();
        }
        LogUtils.hex(TAG, "Final MAC: ", mac);

        //Save MAC field for later use
        iso8583BinData.put("64", mac);

        //Parse sensitive fields and add all of them to TLV
        String[] tmp = transData.getAcquirer().getTleSensitiveFields().split(",");
        List<String> sensitiveFields = Arrays.asList(tmp);

        ITlv tlv = GL.getGL().getPacker().getTlv();
        ITlv.ITlvDataObjList field57DataList = tlv.createTlvDataObjectList();

        try {
            for (Map.Entry<String, String> field : iso8583StrData.entrySet()) {
                if (sensitiveFields.contains(field.getKey())) {
                    //Create ISO8583 field data
                    initEntity();
                    entity.setFieldValue("m", "");
                    setBitData(field.getKey(), field.getValue());
                    byte[] isoMsg = iso8583.pack();
                    byte[] isoData = new byte[isoMsg.length - 10];
                    //Get rid of bitmap and MTI
                    System.arraycopy(isoMsg, 10, isoData, 0, isoMsg.length - 10);
                    //Add data to TLV
                    ITlv.ITlvDataObj obj = tlv.createTlvDataObject();
                    obj.setTag(ConvertUtils.isoFieldNoToTlvTag(field.getKey()));
                    obj.setValue(isoData);
                    field57DataList.addDataObj(obj);
                }
            }
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        try {
            for (Map.Entry<String, byte[]> field : iso8583BinData.entrySet()) {
                if (sensitiveFields.contains(field.getKey())) {
                    //Create ISO8583 field data
                    initEntity();
                    entity.setFieldValue("m", "");
                    setBitData(field.getKey(), field.getValue());
                    byte[] isoMsg = iso8583.pack();
                    byte[] isoData = new byte[isoMsg.length - 10];
                    //Get rid of bitmap and MTI
                    System.arraycopy(isoMsg, 10, isoData, 0, isoMsg.length - 10);
                    //Add data to TLV
                    ITlv.ITlvDataObj obj = tlv.createTlvDataObject();
                    obj.setTag(ConvertUtils.isoFieldNoToTlvTag(field.getKey()));
                    obj.setValue(isoData);
                    field57DataList.addDataObj(obj);
                }
            }
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        for(String s:sensitiveFields) {
            iso8583StrData.remove(s);
            iso8583BinData.remove(s);
        }

        byte[] tlvField57;
        try {
            tlvField57 = tlv.pack(field57DataList);
        } catch(Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        boolean tlvDataFound = true;
        if (tlvField57 == null || tlvField57.length == 0) {
            LogUtils.e(TAG, "No TLV data!");
            tlvDataFound = false;
        }

        byte[] IV = new byte[8];
        short lengthOfData = 0;
        byte[] encryptedTlv = null;
        if(tlvDataFound) {
            lengthOfData = (short) tlvField57.length;
            byte[] finalTlv;
            if (tlvField57.length % 8 != 0) {
                finalTlv = Arrays.copyOf(tlvField57, tlvField57.length + (8 - (tlvField57.length % 8)));
            } else {
                finalTlv = tlvField57;
            }
            LogUtils.hex(TAG, "TLV data: ", finalTlv);

            //Salt
            SecureRandom randomGen = new SecureRandom();
            randomGen.nextBytes(IV);

            //Encrypt with salt
            encryptedTlv = PedHelper.encTriDesCbc(KeyUtils.getTdkIndex(transData.getAcquirer().getTleKeySetId()), finalTlv, IV);
        }

        //Create field 57
        StringBuilder beginingData = new StringBuilder();
        beginingData.append(TleConst.TLE_HEAD);
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleVersion(), 2));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleAcquirerlId(), 3));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTerminalId(), 8));
        beginingData.append(TleConst.ENC_METHOD);
        beginingData.append(TleConst.KEY_SCHEME);
        beginingData.append(TleConst.MAC_ALGO);
        beginingData.append(TleConst.SALT);

        byte[] f57;
        if(tlvDataFound) {
            f57 = new byte[TleConst.HEADER_SIZE + IV.length + encryptedTlv.length];
        } else {
            f57 = new byte[TleConst.HEADER_SIZE + IV.length];
        }
        int position = 0;

        System.arraycopy(beginingData.toString().getBytes(), 0, f57, 0, beginingData.toString().length());
        position += beginingData.toString().length();

        byte[] twkId = transData.getAcquirer().getTleCurrentTwkId();
        if(twkId == null || twkId.length <= 0) {
            System.arraycopy(TleConst.EMPTY_TWK_ID, 0, f57, position, TleConst.EMPTY_TWK_ID.length);
        } else {
            System.arraycopy(twkId, 0, f57, position, 4);
        }
        position += TleConst.EMPTY_TWK_ID.length;

        System.arraycopy(TleConst.TWK_ID_FILL, 0, f57, position, TleConst.TWK_ID_FILL.length);
        position += TleConst.TWK_ID_FILL.length;

        String lengthStr = ConvertUtils.getPaddedString(Short.toString(lengthOfData), 4);
        System.arraycopy(ConvertHelper.getConvert().strToBcd( lengthStr, IConvert.EPaddingPosition.PADDING_LEFT), 0, f57, position, 2);
        position += 2;

        System.arraycopy(TleConst.PIN_TRANSLATE, 0, f57, position, TleConst.PIN_TRANSLATE.length);
        position += TleConst.PIN_TRANSLATE.length;

        System.arraycopy(IV, 0, f57, position, IV.length);
        position += IV.length;

        if(tlvDataFound) {
            System.arraycopy(encryptedTlv, 0, f57, position, encryptedTlv.length);
        }

        byte[] packData;
        try {
            initEntity();
            setHeader(transData);
            for (Map.Entry<String, String> field : iso8583StrData.entrySet()) {
                setBitData(field.getKey(), field.getValue());
            }
            for (Map.Entry<String, byte[]> field : iso8583BinData.entrySet()) {
                setBitData(field.getKey(), field.getValue());
            }
            setBitData("57", f57);
            packData = iso8583.pack();
            LogUtils.hex(TAG, "TLE ISO", packData);
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        return packData;
    }

    public int unpack(@NonNull TransData transData, HashMap<String, byte[]> receivedData) {
        byte[] f57 = receivedData.get("57");
        if(f57 == null || f57.length <= 0) {
            LogUtils.e(TAG, "ERROR - TLE field 57 missing!");
            return TransResult.SUCC; //Return success so response from field 39 will be populated to user
        }
        if(f57.length < TleConst.HEADER_SIZE) {
            LogUtils.e(TAG, "ERROR - TLE field 57 not long enough!");
            return TransResult.ERR_PACKET;
        }
        String tmp = new String(f57, 0, TleConst.TLE_HEAD.length());
        if(!tmp.contains(TleConst.TLE_HEAD)) {
            LogUtils.e(TAG, "ERROR - TLE header mismatch!");
            return TransResult.ERR_PACKET;
        }

        byte[] length = new byte[2];
        System.arraycopy(f57, TleConst.SIZE_POSITION, length, 0, length.length);
        tmp = ConvertHelper.getConvert().bcdToStr(length);
        int encTextLength = Integer.parseInt(tmp);
        if(encTextLength <= 0) {
            LogUtils.e(TAG, "TLE field 57 no encrypted data");
            if(!isMacOk(transData, receivedData)) {
                return TransResult.ERR_MAC;
            }
            return TransResult.SUCC; //Return success so response from field 39 will be populated to user
        }

        //Get salt
        byte[] IV = new byte[TleConst.SALT_SIZE];
        System.arraycopy(f57, TleConst.SALT_POSITION, IV, 0, TleConst.SALT_SIZE);

        //Get encrypted data
        byte[] encryptedTlv = new byte[f57.length - TleConst.HEADER_SIZE - TleConst.SALT_SIZE];
        System.arraycopy(f57, TleConst.ENC_DATA_POSITION, encryptedTlv, 0, encryptedTlv.length);

        //Decrypt data using 3DES CBC
        byte[] decryptedTlv = PedHelper.decrTriDesCbc(KeyUtils.getTdkIndex(transData.getAcquirer().getTleKeySetId()), encryptedTlv, IV);
        if (decryptedTlv == null || decryptedTlv.length == 0) {
            LogUtils.e(TAG, "Decryption of field 57 failed!");
            return TransResult.ERR_PACKET;
        }

        //Trim padding if any
        if(decryptedTlv.length != encTextLength) {
            decryptedTlv = Arrays.copyOf(decryptedTlv, encTextLength);
        }
        LogUtils.hex(TAG, "Decrypted TLV data: ", decryptedTlv);

        //TODO reconstruct orig ISO8583 and check MAC
        return TransResult.SUCC;

    }

    private boolean isMacOk(@NonNull TransData transData, HashMap<String, byte[]> receivedData) {
        byte[] receivedMac = receivedData.get("64");
        //Reconstruct original ISO8583 message for MAC calculation
        byte[] origIsoMsg;
        try {
            initEntity();
            entity.setFieldValue("m", receivedData.get("m"));
            receivedData.remove("h");
            receivedData.remove("m");
            receivedData.remove("57");
            receivedData.remove("64");
            for (Map.Entry<String, byte[]> field : receivedData.entrySet()) {
                setBitData(field.getKey(), field.getValue());
            }
            origIsoMsg = iso8583.pack();
            LogUtils.hex(TAG, "ORIG ISO FROM HOST: ", origIsoMsg);
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return false;
        }
        if (origIsoMsg == null || origIsoMsg.length == 0) {
            LogUtils.e(TAG, "ORIG ISO FROM HOST is empty!");
            return false;
        }

        byte[] mac;
        //Mark field 64 in message bitmap
        origIsoMsg[TleConst.MSG_TYPE_SIZE + TleConst.MAC_BITMAP_POS] |= 1 << 0;
        LogUtils.hex(TAG, "Data for MAC calculation: ", origIsoMsg);
        try {
            //Calculate mac value over original ISO message without header
            mac = PedHelper.getTleSha1Mac(KeyUtils.getTakIndex(transData.getAcquirer().getTleKeySetId()), origIsoMsg);
        } catch(PedDevException e) {
            LogUtils.e(TAG, "", e);
            return false;
        }
        if (mac == null || mac.length == 0) {
            LogUtils.e(TAG, "No MAC data!");
            return false;
        }
        LogUtils.hex(TAG, "Calculated MAC: ", mac);
        LogUtils.hex(TAG, "Received MAC: ", receivedMac);
        if (!ConvertHelper.getConvert().isByteArrayValueSame(receivedMac, 0, mac, 0, 8)) {
            LogUtils.e(TAG, "MAC is not correct.");
            return false;
        }

        return true;
    }

    private void initEntity() {
        iso8583 = GL.getGL().getPacker().getIso8583();
        try {
            entity = iso8583.getEntity();
            entity.loadTemplate(BaseApplication.getAppContext().getResources().getAssets().open("scb8583.xml"));
        } catch (Iso8583Exception | IOException | XmlPullParserException e) {
            LogUtils.e(TAG, "", e);
        }
    }

    private void setHeader(@NonNull TransData transData) throws Iso8583Exception {
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
        iso8583HeaderSize = pHeader.length() / 2; //Because it'll be in binary format

        // m
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL) {
            entity.setFieldValue("m", transType != null ? transType.getDupMsgType() : "");
        } else {
            entity.setFieldValue("m", transType != null ? transType.getMsgType() : "");
        }
    }

    private void setBitData(String field, String value) throws Iso8583Exception {
        if (value != null && !value.isEmpty()) {
            entity.setFieldValue(field, value);
        }
    }

    private void setBitData(String field, byte[] value) throws Iso8583Exception {
        if (value != null && value.length > 0) {
            entity.setFieldValue(field, value);
        }
    }
}
