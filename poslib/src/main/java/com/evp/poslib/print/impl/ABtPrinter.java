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
 * Date	                 Author	                Action
 * 20200109  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.poslib.print.impl;

import android.content.Context;

import com.evp.poslib.print.IPrinter;
import com.evp.poslib.print.entity.PrinterInfo;

/**
 * Bluetooth workflow method for both BE and BP printer
 */
public abstract class ABtPrinter implements IPrinter {
    protected static final String TAG = "ABtPrinter";
    //printer info
    PrinterInfo printerInfo;
    //application context
    protected Context context;

    /**
     * bluetooth connect
     * @return a boolean value
     */
    abstract public boolean connect();

    /**
     * bluetooth disconnect
     */
    abstract public void disConnect();

    /**
     * get printer info
     * @return printer info
     */
    public PrinterInfo getPrinterInfo() {
        return printerInfo;
    }

    /**
     * set printer info
     * @param printerInfo printer info
     */
    public void setPrinterInfo(PrinterInfo printerInfo) {
        this.printerInfo = printerInfo;
    }
}
