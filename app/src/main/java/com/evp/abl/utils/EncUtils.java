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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.abl.utils;

import com.evp.commonlib.utils.LogUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * encryption utils
 */
public class EncUtils {
    private EncUtils() {
        //do nothing
    }

    /**
     * sha1 calc
     *
     * @param str input data
     * @return sha1 value
     */
    public static String sha1(String str) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();

            for (byte i : messageDigest) {
                String shaHex = Integer.toHexString(i & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            LogUtils.w("SHA-1", e);
        }
        return "";
    }

    /**
     * md5 calc
     *
     * @param str input data
     * @return md5 value
     */
    public static String md5(String str) {
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            mdInst.update(str.getBytes());

            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            StringBuilder hexString = new StringBuilder();
            // 字节数组转换为 十六进制 数
            for (byte i : md) {
                String shaHex = Integer.toHexString(i & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LogUtils.w("MD5", e);
        }
        return "";
    }
}
