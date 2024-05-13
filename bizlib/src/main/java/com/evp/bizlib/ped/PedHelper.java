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
 * Date	                 Author	                Action
 * 20200318  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.bizlib.ped;

import androidx.annotation.NonNull;

import com.evp.bizlib.params.ParamHelper;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.neptune.Sdk;
import com.pax.dal.IDAL;
import com.pax.dal.IPed;
import com.pax.dal.entity.EAesCheckMode;
import com.pax.dal.entity.ECheckMode;
import com.pax.dal.entity.ECryptOperate;
import com.pax.dal.entity.ECryptOpt;
import com.pax.dal.entity.EPedKeyType;
import com.pax.dal.entity.EPedMacMode;
import com.pax.dal.entity.EPinBlockMode;
import com.pax.dal.exceptions.PedDevException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils for operate Ped
 */
public class PedHelper {
    private static final String TAG = "PedHelper";

    private static IDAL dal;
    static {
        dal = Sdk.getInstance().getDal(BaseApplication.getAppContext());
    }

    public static IPed getPed() {
        IPed ped;
        try {
            int pedMode = dal.getSys().getPedMode();
            if (pedMode == 2) {
                ped = dal.getPedKeyIsolation(ParamHelper.getCurrentPed());
            } else {
                ped = dal.getPed(ParamHelper.getCurrentPed());
            }
        } catch (Exception e) {
            LogUtils.e(e);
            ped = dal.getPed(ParamHelper.getCurrentPed());
        }
        return ped;
    }

    /**
     * decrypt dataIn with ECB
     * @param dataIn encrypted data
     * @return decrypted data
     */
    public static byte[] calcDes(int tdkIndex, byte[] dataIn){
        if (dal == null){
            return new byte[0];
        }
        if(!isKeyInjected(EPedKeyType.TDK, tdkIndex)) {
            LogUtils.e(TAG, "Error - DES key is not injected");
            return "".getBytes();
        }
        IPed ped = getPed();
        try {
            return ped.calcDes((byte) tdkIndex, null,dataIn, (byte) 0);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * decrypt TDES with CBC
     * @param dataToDecrypt encrypted data
     * @param vector Init Vector
     * @return decrypted data
     */
    public static byte[] decrTDes(int tdkIndex, byte[] dataToDecrypt, byte[] vector){
        if (dal == null){
            return new byte[0];
        }
        if(!isKeyInjected(EPedKeyType.TDK, tdkIndex)) {
            LogUtils.e(TAG, "Error - DES key is not injected");
            return "".getBytes();
        }
        IPed ped = getPed();
        try {
            return ped.calcDes((byte) tdkIndex, vector, dataToDecrypt, (byte) 2);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * encrypt TDES with ECB
     * @param dataToEncrypt plain text data
     * @return decrypted data
     */
    public static byte[] encTDes(int tdkIndex, byte[] dataToEncrypt){
        if (dal == null){
            return new byte[0];
        }
        if(!isKeyInjected(EPedKeyType.TDK, tdkIndex)) {
            LogUtils.e(TAG, "Error - DES key is not injected");
            return "".getBytes();
        }
        IPed ped = getPed();
        try {
            return ped.calcDes((byte) tdkIndex, null, dataToEncrypt, (byte) 1);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * encrypt dataIn with CBC
     * @param keyId 3DES key ID
     * @param dataIn data to encrypt
     * @param IV initialization vector
     * @return encrypted data
     */
    public static byte[] encTriDesCbc(int keyId, byte[] dataIn, byte[] IV){
        if (dal == null){
            return new byte[0];
        }
        IPed ped = getPed();
        try {
            return ped.calcDes((byte) keyId, IV, dataIn, (byte) 3);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * decrypt dataIn with CBC
     * @param keyIndex 3DES key ID
     * @param dataIn data to decrypt
     * @param IV initialization vector
     * @return decrypted data
     */
    public static byte[] decrTriDesCbc(int keyIndex, byte[] dataIn, byte[] IV){
        if (dal == null){
            return new byte[0];
        }
        IPed ped = getPed();
        try {
            return ped.calcDes((byte) keyIndex, IV, dataIn, (byte) 2);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * encrypt dataIn with CBC
     * @param keyId AES key ID
     * @param dataIn data to encrypt
     * @param IV initialization vector
     * @return encrypted data
     */
    public static byte[] encAesCbc(int keyId, byte[] dataIn, byte[] IV){
        if (dal == null){
            return new byte[0];
        }
        IPed ped = getPed();
        try {
            return ped.calcAes((byte) keyId, IV, dataIn, ECryptOperate.ENCRYPT, ECryptOpt.CBC);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * decrypt dataIn with CBC
     * @param keyIndex AES key ID
     * @param dataIn data to decrypt
     * @param IV initialization vector
     * @return decrypted data
     */
    public static byte[] decrAesCbc(int keyIndex, byte[] dataIn, byte[] IV){
        if (dal == null){
            return new byte[0];
        }
        IPed ped = getPed();
        try {
            return ped.calcAes((byte) keyIndex, IV, dataIn, ECryptOperate.DECRYPT, ECryptOpt.CBC);
        } catch (PedDevException e) {
            LogUtils.e(e);
            return new byte[0];
        }
    }

    /**
     * calculate MAC with TAK
     *
     * @param keyIndex TAK index
     * @param data input data
     * @return MAC value
     * @throws PedDevException the ped dev exception
     */
    @NonNull
    public static byte[] calcMac(int keyIndex, String data) throws PedDevException {
        if(!isKeyInjected(EPedKeyType.TAK, keyIndex)) {
            LogUtils.e(TAG, "Error - MAC key is not injected");
            return "".getBytes();
        }
        IPed ped = getPed();
        return ped.getMac((byte) keyIndex, data.getBytes(), EPedMacMode.MODE_02);
    }

    /**
     * Get mac byte [ ].
     *
     * @param keyIndex TAK index
     * @param data the data
     * @return the byte [ ]
     * @throws PedDevException the ped dev exception
     */
    public static byte[] getMac(int keyIndex, byte[] data) throws PedDevException {
        String beforeCalcMacData = ConvertHelper.getConvert().bcdToStr(data);
        byte[] mac = calcMac(keyIndex, beforeCalcMacData);
        if (mac.length > 0) {
            return ConvertHelper.getConvert().bcdToStr(mac).substring(0, 8).getBytes();
        }
        return "".getBytes();
    }

    /**
     * Get SHA1 MAC data used by TLE.
     *
     * @param takIndex TAK index
     * @param data the data for MAC calculation
     * @return 8 bytes of MAC
     * @throws PedDevException the ped dev exception
     */
    public static byte [] getTleSha1Mac (int takIndex, byte [] data) throws PedDevException {
        if (dal == null){
            return new byte[0];
        }
        IPed ped = getPed();
        if(!isKeyInjected(EPedKeyType.TDK, takIndex)) {
            LogUtils.e(TAG, "Error - MAC Key is not injected");
            return "".getBytes();
        }

        //Step 1 - calculate sha1 hash and add padding
        byte[] dataToEncrypt = new byte[24];
        dataToEncrypt[20] = (byte)0x80;
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            digest.update(data);
            byte[] calcShaHash = digest.digest();
            System.arraycopy(calcShaHash, 0, dataToEncrypt, 0, calcShaHash.length);
        } catch (NoSuchAlgorithmException e) {
            LogUtils.w("SHA-1", e);
        }

        //Step 2 - encrypt padded hash using 3DES CBC
        byte[] IV = new byte[8];
        byte[] macBin = ped.calcDes((byte) takIndex, IV, dataToEncrypt, (byte)3);
        if (macBin.length > 8) {
            byte[] outBin = new byte[8];
            System.arraycopy(macBin, macBin.length - 8, outBin, 0, 4);
            return outBin;
        }
        return "".getBytes();
    }

    /**
     * write TMK
     *
     * @param tmkIndex TMK index
     * @param tmkValue TMK value
     * @throws PedDevException exception
     */
    public static void writeTMK(int tmkIndex, byte[] tmkValue) throws PedDevException {
        IPed ped = getPed();
        ped.writeKey(EPedKeyType.TLK, (byte) 0, EPedKeyType.TMK, (byte)tmkIndex,
                tmkValue, ECheckMode.KCV_NONE, null);
    }

    /**
     * write TPK
     *
     * @param tmkIndex TMK index
     * @param tpkIndex TPK index
     * @param tpkValue TPK value
     * @param tpkKcv   TPK KCV
     * @throws PedDevException exception
     */
    public static void writeTPK(int tmkIndex, int tpkIndex, byte[] tpkValue, byte[] tpkKcv) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (tpkKcv == null || tpkKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        IPed ped = getPed();
        ped.writeKey(EPedKeyType.TMK, (byte) tmkIndex,
                EPedKeyType.TPK, (byte) tpkIndex, tpkValue, checkMode, tpkKcv);
    }

    /**
     * write TAK
     *
     * @param tmkIndex TMK index
     * @param takIndex TAK index
     * @param takValue TAK value
     * @param takKcv   TAK KCV
     * @throws PedDevException exception
     */
    public static void writeTAK(int tmkIndex, int takIndex, byte[] takValue, byte[] takKcv) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (takKcv == null || takKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        IPed ped = getPed();
        ped.writeKey(EPedKeyType.TMK, (byte) tmkIndex,
                EPedKeyType.TAK, (byte) takIndex, takValue, checkMode, takKcv);
    }

    /**
     * write TDK
     *
     * @param tmkIndex TMK index
     * @param tdkIndex TDK index
     * @param tdkValue TDK value
     * @param tdkKcv   TDK KCV
     * @throws PedDevException exception
     */
    public static void writeTDK(int tmkIndex, int tdkIndex, byte[] tdkValue, byte[] tdkKcv) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (tdkKcv == null || tdkKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        IPed ped = getPed();
        ped.writeKey(EPedKeyType.TMK, (byte) tmkIndex,
                EPedKeyType.TDK, (byte) tdkIndex, tdkValue, checkMode, tdkKcv);
    }

    /**
     * write clean TDK
     *
     * @param tdkIndex TDK index
     * @param tdkValue TDK value
     * @param tdkKcv   TDK KCV
     * @throws PedDevException exception
     */
    public static void writeCleanTDK(int tdkIndex, byte[] tdkValue, byte[] tdkKcv) throws PedDevException {
        ECheckMode checkMode = ECheckMode.KCV_ENCRYPT_0;
        if (tdkKcv == null || tdkKcv.length == 0) {
            checkMode = ECheckMode.KCV_NONE;
        }
        IPed ped = getPed();
        ped.writeKey(EPedKeyType.TMK, (byte) 0,
                EPedKeyType.TDK, (byte) tdkIndex, tdkValue, checkMode, tdkKcv);
    }

    /**
     * write TAESK
     *
     * @param tmkIndex TMK index
     * @param taeskIndex TAESK index
     * @param taeskValue TAESK value
     * @param taeskKcv   TAESK KCV
     * @throws PedDevException exception
     */
    public static void writeTAESK(int tmkIndex, int taeskIndex, byte[] taeskValue, byte[] taeskKcv) throws PedDevException {
        EAesCheckMode checkMode = EAesCheckMode.KCV_ENCRYPT_0;
        if (taeskKcv == null || taeskKcv.length == 0) {
            checkMode = EAesCheckMode.KCV_NONE;
        }
        IPed ped = getPed();
        ped.writeAesKey(EPedKeyType.TMK, (byte) tmkIndex,
                (byte) taeskIndex, taeskValue, checkMode, taeskKcv);
    }

    /**
     * calculate PIN block
     *
     * @param panBlock      shifted pan block
     * @param supportBypass the support bypass
     * @param landscape     the landscape
     * @return PIN block
     * @throws PedDevException exception
     */
    public static byte[] getPinBlock(int pinKeyIndex, String panBlock, boolean supportBypass, boolean landscape) throws PedDevException {
        if(panBlock == null) {
            LogUtils.e(TAG, "Error - PAN block is empty!");
            return "".getBytes();
        }
        if(!isKeyInjected(EPedKeyType.TPK, pinKeyIndex)) {
            LogUtils.e(TAG, "Error - PIN key is not injected");
            return "".getBytes();
        }
        IPed ped = getPed();
        String pinLen = "4,5,6,7,8,9,10,11,12";
        if (supportBypass) {
            pinLen = "0," + pinLen;
        }
        //外置TpyeA协议只需设置最小、最大长度
        if (ParamHelper.isExternalTypeAPed()){
            pinLen = "4,12";
        }
        if (ParamHelper.isInternalPed()){
            ped.setKeyboardLayoutLandscape(landscape);//设置密码键盘横向显示。仅支持EPedType.INTERNAL 类型。
        }
        return ped.getPinBlock((byte) pinKeyIndex, pinLen, panBlock.getBytes(), EPinBlockMode.ISO9564_0, 60 * 1000);
    }

    /**
     * erase all keys
     *
     * @return the boolean
     */
    public static boolean eraseKeys() {
        try {
            return getPed().erase();
        } catch (PedDevException e) {
            LogUtils.e(TAG, "", e);
        }
        return false;
    }

    /**
     * Check if key is already injected in device
     *
     * @param keyType EPedKeyType
     * @param keyIndex index of key
     * @return
     */
    public static boolean isKeyInjected(EPedKeyType keyType, int keyIndex) {
        //AES keys logic
        if(keyType == EPedKeyType.TAESK) {
            final byte[] data = new byte[16];
            final byte[] IV = new byte[16];
            byte[] out = encAesCbc(keyIndex, data, IV);
            if(out.length > 1) {
                return true;
            }
            LogUtils.e(TAG, "Key is not injected. Index " + keyIndex + ", type " + ConvertUtils.enumValue(EPedKeyType.class, keyType.toString()));
            return false;
        }
        //Other keys logic
        try {
            final byte KCV_ENCRYPT = 0;
            final byte[] data = new byte[8];
            IPed ped = getPed();
            byte[] kcv = ped.getKCV(keyType, (byte) keyIndex,  KCV_ENCRYPT, data);
            if(kcv == null || kcv.length <= 0) {
                LogUtils.e(TAG, "Key is not injected. Index " + keyIndex + ", type " + ConvertUtils.enumValue(EPedKeyType.class, keyType.toString()));
                return false;
            }
        } catch (PedDevException e) {
            LogUtils.e(TAG, "Key is not injected. Index " + keyIndex + ", type " + ConvertUtils.enumValue(EPedKeyType.class, keyType.toString()));
            return false;
        }
        return true;
    }

    /**
     * Check if key is already injected in device
     *
     * @param keyType EPedKeyType
     * @param keyIndex index of key
     * @return
     */
    public static byte[] getKcv(EPedKeyType keyType, int keyIndex) {
        //AES keys logic
        if(keyType == EPedKeyType.TAESK) {
            final byte[] data = new byte[16];
            final byte[] IV = new byte[16];
            byte[] out = encAesCbc(keyIndex, data, IV);
            if(out.length > 1) {
                return out;
            }
            return "".getBytes();
        }

        //Other keys logic
        try {
            final byte KCV_ENCRYPT = 0;
            final byte[] data = new byte[8];
            IPed ped = getPed();
            return ped.getKCV(keyType, (byte) keyIndex,  KCV_ENCRYPT, data);
        } catch (PedDevException e) {
            LogUtils.e(TAG, e);
        }
        return "".getBytes();
    }
}
