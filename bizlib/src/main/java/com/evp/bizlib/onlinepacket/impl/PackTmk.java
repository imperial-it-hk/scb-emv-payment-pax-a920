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

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.bizlib.tle.TleConst;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.CryptoUtils;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.annotation.RouterService;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

/**
 * pack echo
 */
@RouterService(interfaces = IPacker.class, key = OnlinePacketConst.PACKET_TMK_DOWNLOAD)
public class PackTmk extends PackIso8583 {
    private static final String TAG = PackTmk.class.getSimpleName();

    private KeyPair RsaKeySet;

    public PackTmk() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "TMK ISO pack START");

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

        LogUtils.i(TAG, "TMK ISO pack END");

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
        beginingData.append(TleConst.DL_TYPE);
        beginingData.append(TleConst.RQ_TYPE);
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleAcquirerlId(), 3));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTerminalId(), 8));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleVendorId(), 8));
        beginingData.append(ConvertUtils.getPaddedString(transData.getAcquirer().getTleTeId(), 8));

        byte[] hash3 = getHash3(transData);
        LogUtils.hex(TAG, "Final hash", hash3);

        byte[] exponent = ((RSAPublicKey) RsaKeySet.getPublic()).getPublicExponent().toByteArray();
        LogUtils.hex(TAG, "Exponent", exponent);

        byte[] pubKey = ConvertUtils.asciiToBin( ((RSAPublicKey) RsaKeySet.getPublic()).getModulus().toString(16) );
        LogUtils.hex(TAG, "Pub Modulus", pubKey);

        int length = beginingData.toString().length() + hash3.length + exponent.length + pubKey.length;
        LogUtils.i(TAG, "Length all: " + Integer.toString(length));

        byte[] f62 = new byte[length];
        int position = 0;

        System.arraycopy(beginingData.toString().getBytes(), 0, f62, 0, beginingData.toString().length());
        position += beginingData.toString().length();

        System.arraycopy(hash3, 0, f62, position, hash3.length);
        position += hash3.length;

        System.arraycopy(exponent, 0, f62, position, exponent.length);
        position += exponent.length;

        System.arraycopy(pubKey, 0, f62, position, pubKey.length);

        setBitData("62", f62);
    }

    private byte[] getHash3(@NonNull TransData transData)
    {
        try {
            RsaKeySet = CryptoUtils.genRsaKeyPair(TleConst.RSA_KEY_LENGTH);
            transData.setRsaKeyPair(RsaKeySet);

            String hashThis = transData.getAcquirer().getTleTeId() + transData.getAcquirer().getTleTePin() + TleConst.PIN_HASH_TAIL;
            hashThis = hashThis.toUpperCase();
            LogUtils.i(TAG, "TXN-HASH Calculation data: " + hashThis);

            String pinHash = CryptoUtils.sha1(hashThis).substring(0, 8);
            pinHash = pinHash.toUpperCase();
            LogUtils.i(TAG, "TXN-HASH result: " + pinHash);

            byte[] pubKey = ConvertUtils.asciiToBin(((RSAPublicKey) RsaKeySet.getPublic()).getModulus().toString(16));

            String tmp = ConvertUtils.getPaddedString(transData.getAcquirer().getTerminalId(), 8);
            tmp = tmp.toUpperCase();
            byte[] tidBytes = tmp.getBytes();
            LogUtils.hex(TAG, "TID", tidBytes);

            tmp = ConvertUtils.getPaddedNumber(transData.getStanNo(), 6).substring(2);
            byte[] stanBytes = tmp.getBytes();
            LogUtils.hex(TAG, "STAN", stanBytes);

            int length = pinHash.length() + tidBytes.length + stanBytes.length + pubKey.length;
            LogUtils.i(TAG, "Length hash3: " + Integer.toString(length));

            byte[] hashThis2 = new byte[length];
            int position = 0;

            System.arraycopy(pinHash.getBytes(), 0, hashThis2, position, pinHash.length());
            position += pinHash.length();

            System.arraycopy(tidBytes, 0, hashThis2, position, tidBytes.length);
            position += tidBytes.length;

            System.arraycopy(stanBytes, 0, hashThis2, position, stanBytes.length);
            position += stanBytes.length;

            System.arraycopy(pubKey, 0, hashThis2, position, pubKey.length);

            byte[] hash3 = CryptoUtils.sha1(hashThis2);

            length = TleConst.HASH3_PREFIX.length + TleConst.HASH3_LENGTH + TleConst.HASH3_POSTFIX.length;
            byte[] out = new byte[length];
            position = 0;

            System.arraycopy(TleConst.HASH3_PREFIX, 0, out, 0, TleConst.HASH3_PREFIX.length);
            position += TleConst.HASH3_PREFIX.length;

            System.arraycopy(hash3, 0, out, position, TleConst.HASH3_LENGTH);
            position += 4;

            System.arraycopy(TleConst.HASH3_POSTFIX, 0, out, position, TleConst.HASH3_POSTFIX.length);

            return out;
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
        }
        return "".getBytes();
    }

}
