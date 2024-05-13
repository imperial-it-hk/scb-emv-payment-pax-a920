package com.evp.commonlib.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

public class DateUtils {
    public static final String UPI_REFUND_DATE_FORMAT = "ddMMyyyy";

    @SuppressLint("SimpleDateFormat")
    public static boolean isDateValid(String date, String pattern) {
        boolean ret = true;
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        try {
            dateFormat.setLenient(false);
            dateFormat.parse(date);
        } catch (Exception var4) {
            ret = false;
        }

        return ret;
    }
}
