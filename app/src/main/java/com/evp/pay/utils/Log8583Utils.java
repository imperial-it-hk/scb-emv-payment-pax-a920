/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2018-1-15
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.evp.pay.utils;

import android.os.Environment;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.pax.gl.pack.IIso8583;
import com.pax.gl.pack.exception.Iso8583Exception;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * The type Log 8583 utils.
 */
public class Log8583Utils {

    private static final String TAG = "Log8583Utils";

    private Log8583Utils() {
        //do nothing
    }

    /**
     * Save log.
     *
     * @param fileName the file name
     * @param data     the data
     * @param isSend   the is send
     */
    public static void saveLog(String fileName, byte[] data, boolean isSend) {
        IIso8583 iso8583 = FinancialApplication.getPacker().getIso8583();
        HashMap<String, byte[]> map;
        try {
            map = iso8583.unpack(data, true);
        } catch (Iso8583Exception e) {
            LogUtils.e(TAG, "", e);
            return;
        }

        File file = openFile(fileName);
        if (file == null) {
            return;
        }

        String title;
        if (isSend) {
            title = "Send Data";
        } else {
            title = "Receive Data";
        }

        writeToFile(title, map, file);
    }

    private static File openFile(String fileName) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/logs/8583log/";
        File dir = new File(path);
        if (dir.exists() || dir.mkdirs()) {
            return new File(path, fileName);
        }

        return null;
    }

    private static void writeToFile(String title, HashMap<String, byte[]> map, File file) {
        try (BufferedWriter output = new BufferedWriter(new FileWriter(file, true))) {
            output.write(title + "\n\n");

            writeLine("H", map.get("h"), output);
            writeLine("M", map.get("m"), output);

            for (int i = 0; i <= 64; ++i) {
                String tag = Integer.toString(i);
                writeLine(tag, map.get(tag), output);
            }
            writeLine("123", map.get("123"), output);

            output.write("\n\n");

            output.flush();
        } catch (IOException e) {
            LogUtils.e(TAG, "", e);
        }
    }

    private static void writeLine(String tag, byte[] value, BufferedWriter output) throws IOException {
        if (value == null || value.length <= 0) {
            return;
        }

        String writeValue;
        String writeTag = tag;
        if ("52".equals(writeTag) || "55".equals(writeTag) || "56".equals(writeTag) || "62".equals(writeTag) || "63".equals(writeTag) || "123".equals(writeTag)) {
            writeValue = FinancialApplication.getConvert().bcdToStr(value);
        } else {
            writeValue = new String(value);
        }

        output.write("[" + writeTag + "] : " + writeValue + "\n");
    }
}
