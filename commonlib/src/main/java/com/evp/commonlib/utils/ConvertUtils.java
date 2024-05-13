/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) YYYY-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date	                 Author	                Action
 * 2020/7/7  	         xieYb      	        Create
 * ===========================================================================================
 */
package com.evp.commonlib.utils;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConvertUtils {
    public static final String TIME_PATTERN_TRANS = "yyyyMMddHHmmss";
    public static final String TIME_PATTERN_DISPLAY = "yyyy/MM/dd HH:mm:ss";
    public static final String TIME_PATTERN_PRINT = "MM dd, yy HH:mm:ss";
    public static final String DATE_TIME_PATTERN_PRINT = "MMM dd, yy HH:mm:ss";
    public static final String DATE_PATTERN_PRINT = "MMM dd, yy";
    public static final String TIME_ONLY_PATTERN_PRINT = "HH:mm:ss";
    public static final String TAG = "ConvertUtils";


    /**
     * Parses the string argument as a signed decimal {@code long}.safely
     * @param longStr longStr
     * @param safeValue safeValue
     * @return a long value
     */
    public static long parseLongSafe(String longStr, long safeValue) {
        if (longStr == null) {
            return safeValue;
        }
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return safeValue;
        }
    }

    /**
     * match specific Enum by name
     * @param cls enum type
     * @param name enum type name,for example:ETransType.REFUND.name()
     * @param <T> enum type
     * @return specific type enum
     */
    public static <T extends Enum<T>> T enumValue(Class<T> cls,String name){
        try {
            return Enum.valueOf(cls, name);
        }catch (Exception e){
            LogUtils.e(cls.getCanonicalName(),e.getLocalizedMessage());
        }
        return null;
    }

    public static <T extends Enum<T>> T getEnum(Class<T> clazz, int ordinal) {
        for (T t : clazz.getEnumConstants()) {
            if (t.ordinal() == ordinal) {
                return t;
            }
        }
        return null;
    }

    /**
     * padding num with specific digit
     * @param num origin num
     * @param digit padding digit
     * @return padded num with digit
     */
    public static String getPaddedNumber(long num, int digit) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(false);
        nf.setMaximumIntegerDigits(digit);
        nf.setMinimumIntegerDigits(digit);
        return nf.format(num);
    }

    /**
     * padding string with specific digit
     * @param text origin text
     * @param digit padding digit
     * @return padded num with digit
     */
    public static String getPaddedString(String text, int digit) {
        return String.format("%1$" + digit + "s", text).replace(' ', '0');
    }

    /**
     * convert old time pattern to new time pattern
     * @param formattedTime formattedTime
     * @param oldPattern oldPattern
     * @param newPattern newPattern
     * @return newPattern time string
     */
    public static String convert(String formattedTime, final String oldPattern, final String newPattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(oldPattern, Locale.US);
        java.util.Date date;
        try {
            date = sdf.parse(formattedTime);
        } catch (ParseException e) {
            return formattedTime;
        }
        sdf = new SimpleDateFormat(newPattern, Locale.US);
        return sdf.format(date);
    }

    /**
     * convert current time with specific pattern
     * @param pattern pattern
     * @return newPattern time string
     */
    public static String convertCurrentTime(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        return dateFormat.format(new Date());
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String binToAscii(byte[] data) {
        if(data == null || data.length <= 0) {
            return "";
        }
        byte[] hexChars = new byte[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static byte[] asciiToBin(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * The ISO 8583 message field number will be encoded in octal format in each nibble (only the
     * lower 3 bits of the nibble is being used) of the TAG byte. (EX 1: field 2 as TAG 0x02, EX 2: field 35 as TAG 0x43, EX 3: field 101 as TAG 0x81 0x45)
     * @param field ISO8583 field number. MAX is 192
     * @return TLV tag of ISO8583 field.
     */
    private static final int MAX_ISO8583_FIELD_NUMBER = 192;
    public static byte[] isoFieldNoToTlvTag(int field) {
        if(field > MAX_ISO8583_FIELD_NUMBER) {
            return "".getBytes();
        }
        String tmp = Integer.toOctalString(field);
        if(tmp.length() > 2) {
            return asciiToBin("8" + tmp);
        } else if(tmp.length() == 1) {
            return asciiToBin("0" + tmp);
        } else {
            return asciiToBin(tmp);
        }
    }

    public static byte[] isoFieldNoToTlvTag(String field) {
        try {
            return isoFieldNoToTlvTag(Integer.parseInt(field));
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, e);
        }
        return "".getBytes();
    }
}
