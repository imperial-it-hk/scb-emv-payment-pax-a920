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
package com.evp.poslib.print;

import android.graphics.Bitmap;

import com.evp.poslib.print.exception.PrinterException;

/**
 * Interface for print
 */
public interface IPrinter {
    /**
     * print bitmap
     * @param bitmap bitmap
     * @return print status
     * @throws PrinterException exception
     */
    public int print(Bitmap bitmap) throws PrinterException;

    /**
     * print single line
     * @param line single line
     * @return print status
     * @throws PrinterException exception
     */
    public int print(String line) throws PrinterException;
}
