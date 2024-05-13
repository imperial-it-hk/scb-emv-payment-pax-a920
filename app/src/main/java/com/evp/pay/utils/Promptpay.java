package com.evp.pay.utils;

import java.nio.charset.StandardCharsets;

public class Promptpay {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static byte[] crc16(byte[] data) {
        int crc = 0xFFFF;
        for (int j = 0; j < data.length; j++) {
            crc = ((crc >>> 8) | (crc << 8)) & 0xffff;
            crc ^= (data[j] & 0xff);// byte to int, trunc sign
            crc ^= ((crc & 0xff) >> 4);
            crc ^= (crc << 12) & 0xffff;
            crc ^= ((crc & 0xFF) << 5) & 0xffff;
        }
        crc &= 0xffff;
        return new byte[] { (byte) (crc >>> 8), (byte) crc };
    }

    public static String formatObject(String id, String value) {
        return id + String.format("%02d", value.length()) + value;
    }

    public static String generate(String merchantIdentifier, String billerId, String terminalId, String traceNo, String amount, String merchantName, String ref1) {
        return generate(merchantIdentifier, billerId, terminalId, traceNo, amount, merchantName, ref1, null);
    }

    public static String generate(String merchantIdentifier, String billerId, String terminalId, String traceNo, String amount, String merchantName, String ref1, String ref2) {
        String data30 = formatObject("00", merchantIdentifier)
                + formatObject("01", billerId)
                + formatObject("02", ref1);

        if (ref2 != null) {
            data30 = data30 + formatObject("03", ref2);
        }

        System.out.println(data30);

        String result = formatObject("00", "01")
                + formatObject("01", "11")
                + formatObject("30", data30)
                + formatObject("53", "764")
                + formatObject("54", amount)
                + formatObject("58", "TH")
                + formatObject("59", merchantName)
                + formatObject("62", "0711EVO" + terminalId)
                + "6304";
        byte[] crc = crc16(result.getBytes());

        return result + bytesToHex(crc);
    }
}
