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
 * 20190108  	         guanjw                  Create
 * ===========================================================================================
 */

package com.evp.commonlib.utils;
/**
 * Created by guanjw on 2018/6/27.
 */

import android.os.Environment;
import android.util.Log;

import com.evp.commonlib.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {
    private LogUtils() {
    }

    private static final boolean IS_DEBUG = BuildConfig.DEBUG;

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        if (trace == null || trace.length == 0) {
            return "";
        }
        return trace[2].getClassName() + "." + trace[2].getMethodName() + "(line:" + trace[2].getLineNumber() + ")";
    }

    /**
     * output ERROR level logs
     * @param tag tag
     * @param msg msg
     */
    public static void e(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.e(tag, "" + msg);
        }
    }

    /**
     * output WARN level logs
     * @param tag tag
     * @param msg msg
     */
    public static void w(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.w(tag, "" + msg);
        }
    }

    /**
     *  output INFO level logs
     * @param tag tag
     * @param msg msg
     */
    public static void i(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.i(tag, "" + msg);
        }
    }

    /**
     * output DEBUG level logs
     * @param tag tag
     * @param msg msg
     */
    public static void d(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.d(tag, "" + msg);
        }
    }

    /**
     * output Verbose level logs
     * @param tag tag
     * @param msg msg
     */
    public static void v(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.v(tag, "" + msg);
        }
    }

    /**
     * output ERROR level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void e(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.e(tag, msg, th);
        }
    }

    /**
     * output WARN level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void w(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.w(tag, msg, th);
        }
    }
    /**
     *  output INFO level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void i(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.i(tag, msg, th);
        }
    }
    /**
     * output DEBUG level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void d(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.d(tag, msg, th);
        }
    }
    /**
     * output Verbose level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void v(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.v(tag, msg, th);
        }
    }

    /**
     * output DEBUG level logs
     * @param content msg
     */
    public static void d(Object content) {
        d(getTag(), content);
    }

    /**
     * output ERROR level logs
     * @param e error
     */
    public static void e(Exception e) {
        e(getTag(), e);
    }

    /**
     * output INFO level logs
     * @param content msg
     */
    public static void i(Object content) {
        i(getTag(), content);
    }

    /**
     * log to file
     * @param tag tag
     * @param msg msg
     */
    public static void fd(String tag, String msg) {
        LogUtils.d(tag, msg);
        DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String time = formatter.format(new Date());
        String content = time + ": " + tag + ":\t" + msg + "\n";
        try(FileWriter writer = new FileWriter(getFile(),true)){
            writer.write(content);
        } catch (IOException e) {
            e(tag,e);
        }
    }

    /**
     * get log file location
     * @return
     */
    public static String getFile() {
        File sdDir = null;
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        File cacheDir = new File(sdDir + File.separator + "appLog");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        File filePath = new File(cacheDir + File.separator + "edc_log.txt");
        return filePath.toString();
    }

    /**
     *  output INFO level logs
     * @param tag tag
     * @param msg msg
     * @param data binary data
     */
    public static void hex(String tag, String msg, byte[] data) {
        if (IS_DEBUG) {
            StringBuilder sb = new StringBuilder(msg);
            sb.append(ConvertUtils.binToAscii(data));
            Log.i(tag, sb.toString());
        }
    }
}

