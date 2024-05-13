package com.evp.commonlib.utils;

public class ParseUtils {
    private ParseUtils() {}

    /**
     * Parse int safe int.
     *
     * @param intStr the int str
     * @return the int
     */
    public static int parseIntSafe(String intStr) {
        return parseIntSafe(intStr, 0);
    }

    /**
     * Parse int safe int.
     *
     * @param intStr       the int str
     * @param defaultValue the default value
     * @return the int
     */
    public static int parseIntSafe(String intStr, int defaultValue) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
