/*
 *  * ===========================================================================================
 *  * = COPYRIGHT
 *  *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or nondisclosure
 *  *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *  *   disclosed except in accordance with the terms in that agreement.
 *  *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 *  * Description: // Detail description about the voidction of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date                  Author	                 Action
 *  * 20200713  	         xieYb                   Modify
 *  * ===========================================================================================
 *
 */
package com.evp.paxprinter.impl;

import android.content.Context;
import android.graphics.Bitmap;

import com.evp.poslib.print.entity.PrinterInfo;
import com.evp.poslib.print.exception.PrinterException;
import com.evp.poslib.print.impl.ABtPrinter;
import com.evp.poslib.print.impl.BpPrinter;

/**
 * print service for devices that use BP60A-C series docker
 */
public class PrintBP60A_C2Service extends APrintBluetoothService {
    /**
     * @param bitmap  bitmap
     * @param context application context
     * @return success code
     * @throws PrinterException failed code and message
     */
    @Override
    public int print(final Bitmap bitmap, final Context context) throws PrinterException {
        this.bitmap = bitmap;
        this.context = context;
        return print();
    }

    /**
     * @param line    single line text
     * @param context application context
     * @return success code
     * @throws PrinterException failed code and message
     */
    @Override
    public int print(String line, Context context) throws PrinterException {
        this.context = context;
        exception = null;
        bitmap = null;
        this.line = line;
        return print();
    }

    @Override
    protected ABtPrinter getBtPrinter(Context context, PrinterInfo printerInfo) {
        return new BpPrinter(context,printerInfo);
    }

    @Override
    protected int printByType() throws PrinterException {
        if (bitmap != null){
            return btPrinter.print(bitmap);
        }else {
            return btPrinter.print(line);
        }
    }
}
