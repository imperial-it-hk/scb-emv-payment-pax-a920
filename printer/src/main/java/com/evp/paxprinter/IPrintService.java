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
package com.evp.paxprinter;

import android.content.Context;
import android.graphics.Bitmap;

import com.evp.poslib.print.exception.PrinterException;

/**
 * Interface for Print Service
 */
public interface IPrintService {
    /**
     *
     * @param bitmap bitmap
     * @param context application context
     * @return success code
     * @throws PrinterException failed code and message
     */
    public int print(final Bitmap bitmap, final Context context) throws PrinterException;

    /**
     *
     * @param line single line text
     * @param context application context
     * @return success code
     * @throws PrinterException failed code and message
     */
    public int print(final String line, final Context context) throws PrinterException;
}
