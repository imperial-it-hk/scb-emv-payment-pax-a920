package com.evp.commonlib.utils;

public class KeyUtils {
    //Terminal master key
    private static final int[] TMK_INDEXES    = { 1,  2,  3,  4,  5,  6,  7,  8,  9, 10};
    //Terminal DES key
    private static final int[] TDK_INDEXES    = { 1, 11, 21, 31, 41, 51, 61, 71, 81, 91};
    //Terminal MAC key
    private static final int[] TAK_INDEXES    = { 2, 12, 22, 32, 42, 52, 62, 72, 82, 92};
    //Terminal PIN key
    private static final int[] TPK_INDEXES    = { 3, 13, 23, 33, 43, 53, 63, 73, 83, 93};
    //Terminal second DES key
    private static final int[] TSDK_INDEXES   = { 4, 14, 24, 34, 44, 54, 64, 74, 84, 94};
    //Terminal AES key - special indexes from 1 to 40. Only for AES
    private static final int[] TAESK_INDEXES  = { 1,  2,  3,  4,  5,  6,  7,  8,  9, 10};
    //Terminal second AES key - special indexes from 1 to 40. Only for AES
    private static final int[] TSAESK_INDEXES = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

    private static final String KEY_SET_PREFIX = "Set ";


    public static int getTmkIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TMK_INDEXES.length) {
            return TMK_INDEXES[position - 1];
        }
        return TMK_INDEXES[0];
    }

    public static int getTdkIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TDK_INDEXES.length) {
            return TDK_INDEXES[position - 1];
        }
        return TDK_INDEXES[0];
    }

    public static int getTakIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TAK_INDEXES.length) {
            return TAK_INDEXES[position - 1];
        }
        return TAK_INDEXES[0];
    }

    public static int getTpkIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TPK_INDEXES.length) {
            return TPK_INDEXES[position - 1];
        }
        return TPK_INDEXES[0];
    }

    public static int getTsdkIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TSDK_INDEXES.length) {
            return TSDK_INDEXES[position - 1];
        }
        return TSDK_INDEXES[0];
    }

    public static int getTaeskIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TAESK_INDEXES.length) {
            return TAESK_INDEXES[position - 1];
        }
        return TAESK_INDEXES[0];
    }

    public static int getTsaeskIndex(String setId) {
        int position = ParseUtils.parseIntSafe(setId.replace(KEY_SET_PREFIX, ""));
        if(position <= TSAESK_INDEXES.length) {
            return TSAESK_INDEXES[position - 1];
        }
        return TSAESK_INDEXES[0];
    }
}
