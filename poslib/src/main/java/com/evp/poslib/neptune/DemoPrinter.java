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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.poslib.neptune;

import android.graphics.Bitmap;

import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.dal.exceptions.PrinterDevException;
/**
 * neptune IPrinter
 */
class DemoPrinter implements IPrinter {

    DemoPrinter() {
        //do nothing
    }

    @Override
    public void init() throws PrinterDevException {
        //do nothing
    }

    @Override
    public void fontSet(EFontTypeAscii eFontTypeAscii, EFontTypeExtCode eFontTypeExtCode) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void spaceSet(byte b, byte b1) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void step(int i) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void printStr(String s, String s1) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void printBitmap(Bitmap bitmap) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void print(Bitmap bitmap, IPinterListener iPinterListener) {
        //do nothing
    }

    @Override
    public void print(Bitmap bitmap, int i, IPinterListener iPinterListener) {
        //do nothing
    }

    @Override
    public void setFontPath(String s) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void printBitmapWithMonoThreshold(Bitmap bitmap, int i) throws PrinterDevException {
        //do nothing
    }

    @Override
    public int start() throws PrinterDevException {
        return 0;
    }

    @Override
    public int getStatus() throws PrinterDevException {
        return 0;
    }

    @Override
    public void leftIndent(int i) throws PrinterDevException {
        //do nothing
    }

    @Override
    public int getDotLine() throws PrinterDevException {
        return 0;
    }

    @Override
    public void setGray(int i) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void doubleWidth(boolean b, boolean b1) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void doubleHeight(boolean b, boolean b1) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void invert(boolean b) throws PrinterDevException {
        //do nothing
    }

    @Override
    public void cutPaper(int i) throws PrinterDevException {
        //do nothing
    }

    @Override
    public int getCutMode() throws PrinterDevException {
        return -1;
    }
}
