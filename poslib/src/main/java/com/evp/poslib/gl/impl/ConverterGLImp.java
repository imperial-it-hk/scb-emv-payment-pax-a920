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
package com.evp.poslib.gl.impl;

import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.convert.IConvert;
import com.pax.gl.utils.impl.Convert;

public class ConverterGLImp implements IConvert {
    private static ConverterGLImp converterImp;

    public ConverterGLImp() {
    }

    public static IConvert getConvert() {
        if (converterImp == null) {
            converterImp = new ConverterGLImp();
        }
        return converterImp;
    }

    /**
     * convert bcd to string
     * @param b bcd bytes
     * @return string
     */
    public String bcdToStr(byte[] b) {
        try {
            return Convert.bcdToStr(b);
        } catch (IllegalArgumentException e) {
            LogUtils.e(e);
            return "";
        }
    }

    @Override
    public byte[] strToBcdPaddingLeft(String str) {
        return strToBcd(str, EPaddingPosition.PADDING_LEFT);
    }

    @Override
    public byte[] strToBcdPaddingRight(String str) {
        return strToBcd(str, EPaddingPosition.PADDING_RIGHT);
    }

    /**
     * convert string to bcd bytes
     * @param str string
     * @param paddingPosition padding position
     * @return bcd bytes
     */
    public byte[] strToBcd(String str, EPaddingPosition paddingPosition) {
        byte[] result = new byte[0];
        try {
            if (paddingPosition == EPaddingPosition.PADDING_RIGHT)
                result = Convert.strToBcd(str, Convert.EPaddingPosition.PADDING_RIGHT);
            else {
                result = Convert.strToBcd(str, Convert.EPaddingPosition.PADDING_LEFT);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
        return result;
    }

    /**
     * convert long to byte array
     * @param l long
     * @param to byte array
     * @param offset begin position
     * @param endian endian
     */
    public void longToByteArray(long l, byte[] to, int offset, EEndian endian) {
        try {
            if (endian == EEndian.BIG_ENDIAN)
                Convert.longToByteArray(l, to, offset, Convert.EEndian.BIG_ENDIAN);
            else {
                Convert.longToByteArray(l, to, offset, Convert.EEndian.LITTLE_ENDIAN);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
    }

    /**
     * convert long to byte array
     * @param l long
     * @param endian endian
     * @return byte array
     */
    public byte[] longToByteArray(long l, EEndian endian) {
        if (endian == EEndian.BIG_ENDIAN)
            return Convert.longToByteArray(l, Convert.EEndian.BIG_ENDIAN);
        else {
            return Convert.longToByteArray(l, Convert.EEndian.LITTLE_ENDIAN);
        }
    }

    /**
     * convert int to byte array
     * @param paramInt1 int value
     * @param paramArrayOfByte byte array
     * @param paramInt2 begin position
     * @param paramEEndian endian
     */
    public void intToByteArray(int paramInt1, byte[] paramArrayOfByte, int paramInt2, EEndian paramEEndian) {
        try {
            if (paramEEndian == EEndian.BIG_ENDIAN)
                Convert.intToByteArray(paramInt1, paramArrayOfByte, paramInt2, Convert.EEndian.BIG_ENDIAN);
            else {
                Convert.intToByteArray(paramInt1, paramArrayOfByte, paramInt2, Convert.EEndian.LITTLE_ENDIAN);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
    }

    /**
     * convert int to byte array
     * @param paramInt int value
     * @param paramEEndian endian
     * @return byte array
     */
    public byte[] intToByteArray(int paramInt, EEndian paramEEndian) {
        if (paramEEndian == EEndian.BIG_ENDIAN)
            return Convert.intToByteArray(paramInt, Convert.EEndian.BIG_ENDIAN);
        else {
            return Convert.intToByteArray(paramInt, Convert.EEndian.LITTLE_ENDIAN);
        }
    }

    /**
     * convert short to byte array
     * @param paramShort short value
     * @param paramArrayOfByte byte array
     * @param paramInt begin position
     * @param paramEEndian endian
     */
    public void shortToByteArray(short paramShort, byte[] paramArrayOfByte, int paramInt, EEndian paramEEndian) {
        try {
            if (paramEEndian == EEndian.BIG_ENDIAN)
                Convert.shortToByteArray(paramShort, paramArrayOfByte, paramInt, Convert.EEndian.BIG_ENDIAN);
            else {
                Convert.shortToByteArray(paramShort, paramArrayOfByte, paramInt, Convert.EEndian.LITTLE_ENDIAN);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
    }

    /**
     * convert short to byte array
     * @param paramShort short value
     * @param paramEEndian endian
     * @return byte array
     */
    public byte[] shortToByteArray(short paramShort, EEndian paramEEndian) {
        if (paramEEndian == EEndian.BIG_ENDIAN)
            return Convert.shortToByteArray(paramShort, Convert.EEndian.BIG_ENDIAN);
        else {
            return Convert.shortToByteArray(paramShort, Convert.EEndian.LITTLE_ENDIAN);
        }
    }

    /**
     * convert byte array to long
     * @param paramArrayOfByte byte array
     * @param paramInt begin position
     * @param paramEEndian endian
     * @return long value
     */
    public long longFromByteArray(byte[] paramArrayOfByte, int paramInt, EEndian paramEEndian) {
        long result = -1;
        try {
            if (paramEEndian == EEndian.BIG_ENDIAN)
                result = Convert.longFromByteArray(paramArrayOfByte, paramInt, Convert.EEndian.BIG_ENDIAN);
            else {
                result = Convert.longFromByteArray(paramArrayOfByte, paramInt, Convert.EEndian.LITTLE_ENDIAN);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
        return result;
    }

    /**
     * convert byte array to int
     * @param paramArrayOfByte byte array
     * @param paramInt  begin position
     * @param paramEEndian endian
     * @return int value
     */
    public int intFromByteArray(byte[] paramArrayOfByte, int paramInt, EEndian paramEEndian) {
        int result = -1;
        try {
            if (paramEEndian == EEndian.BIG_ENDIAN)
                result = Convert.intFromByteArray(paramArrayOfByte, paramInt, Convert.EEndian.BIG_ENDIAN);
            else {
                result = Convert.intFromByteArray(paramArrayOfByte, paramInt, Convert.EEndian.LITTLE_ENDIAN);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
        return result;
    }

    /**
     * convert byte array to short
     * @param paramArrayOfByte  byte array
     * @param paramInt begin position
     * @param paramEEndian endian
     * @return short value
     */
    public short shortFromByteArray(byte[] paramArrayOfByte, int paramInt, EEndian paramEEndian) {
        short result = -1;
        try {
            if (paramEEndian == EEndian.BIG_ENDIAN)
                result = Convert.shortFromByteArray(paramArrayOfByte, paramInt, Convert.EEndian.BIG_ENDIAN);
            else {
                result = Convert.shortFromByteArray(paramArrayOfByte, paramInt, Convert.EEndian.LITTLE_ENDIAN);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
        return result;


    }

    public String stringPadding(String paramString, char paramChar, long paramLong, EPaddingPosition paramEPaddingPosition) {
        String result = "";
        try {
            if (paramEPaddingPosition == EPaddingPosition.PADDING_LEFT)
                result = Convert.stringPadding(paramString, paramChar, paramLong, Convert.EPaddingPosition.PADDING_LEFT);
            else {
                result = Convert.stringPadding(paramString, paramChar, paramLong, Convert.EPaddingPosition.PADDING_RIGHT);
            }
        }catch (IllegalArgumentException e){
            LogUtils.e(e);
        }
        return result;
    }

    public boolean isByteArrayValueSame(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2, int paramInt3) {
        if ((paramArrayOfByte1 == null) || (paramArrayOfByte2 == null)) {
            return false;
        }

        if ((paramInt1 + paramInt3 > paramArrayOfByte1.length) || (paramInt2 + paramInt3 > paramArrayOfByte2.length)) {
            return false;
        }

        for (int i = 0; i < paramInt3; i++) {
            if (paramArrayOfByte1[(paramInt1 + i)] != paramArrayOfByte2[(paramInt2 + i)]) {
                return false;
            }
        }

        return true;
    }
}
