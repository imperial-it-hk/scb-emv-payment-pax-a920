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

import com.evp.paxprinter.IPrintService;
import com.evp.poslib.print.IPrinter;
import com.evp.poslib.print.exception.EPrinterException;
import com.evp.poslib.print.exception.PrinterException;
import com.evp.poslib.print.impl.BuildInPrinter;
import com.pax.dal.IDAL;
import com.pax.neptunelite.api.NeptuneLiteUser;

/**
 * print service for devices that Bring their own printer
 */
public class PrintBuildInService implements IPrintService {
    @Override
    public int print(Bitmap bitmap, Context context) throws PrinterException {
        IDAL dal = null;
        try {
            dal = NeptuneLiteUser.getInstance().getDal(context);
        } catch (Exception e) {
            throw new PrinterException(EPrinterException.NEPTUNE_ERROR);
        }
        IPrinter printer = new BuildInPrinter(dal);
        return printer.print(bitmap);
    }

    @Override
    public int print(String line, Context context) throws PrinterException {
        IDAL dal = null;
        try {
            dal = dal = NeptuneLiteUser.getInstance().getDal(context);
        } catch (Exception e) {
            throw new PrinterException(EPrinterException.NEPTUNE_ERROR);
        }
        IPrinter printer = new BuildInPrinter(dal);
        return printer.print(line);
    }
}
