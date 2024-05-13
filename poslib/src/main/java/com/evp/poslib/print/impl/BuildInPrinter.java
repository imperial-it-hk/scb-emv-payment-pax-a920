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

import android.graphics.Bitmap;

import com.evp.poslib.print.IPrinter;
import com.evp.poslib.print.exception.EPrinterException;
import com.evp.poslib.print.exception.PrinterException;
import com.pax.dal.IDAL;
import com.pax.dal.exceptions.PrinterDevException;

public class BuildInPrinter implements IPrinter {
    private IDAL idal;

    public BuildInPrinter(IDAL idal) {
        this.idal = idal;
    }
    /**
     * print bitmap
     * @param bitmap bitmap
     * @return print status
     * @throws PrinterException exception
     */
    @Override
    public int print(Bitmap bitmap) throws PrinterException {
        if (idal == null) {
            throw new PrinterException(EPrinterException.NEPTUNE_ERROR);
        }
        com.pax.dal.IPrinter printer = idal.getPrinter();
        if (printer == null) {
            throw new PrinterException(EPrinterException.PRINTER_ERROR);
        }
        try {
            printer.init();
            printer.setGray(3);
            printer.printBitmap(bitmap);
            int result = printer.start();
            //0 means success,-4 means Printer has no font installed
            if (result == 0 || result == -4){
                return 0;
            }
            throw handleError(result);
        } catch (PrinterDevException e) {
            throw new PrinterException(e.getErrCode(), e.getErrMsg());
        }
    }

    private PrinterException handleError(int result){
        switch (result){
            case 1:
                //Printer busy
                return new PrinterException(EPrinterException.PRINTER_BUSY);
            case 2:
                //Printer out of paper
                return new PrinterException(EPrinterException.PRINTER_OUT_OF_PAPER);
            case 3:
                //Print packet format error
                return new PrinterException(EPrinterException.PRINTER_PACKET_FORMAT_ERROR);
            case 4:
                //Printer failure
                return new PrinterException(EPrinterException.PRINTER_FAULT);
            case 8:
                //Printer overheats
                return new PrinterException(EPrinterException.PRINTER_OVERHEAT);
            case 9:
                //Printer voltage too low
                return new PrinterException(EPrinterException.PRINTER_VOLTAGE_LOW);
            case -16:
                //Printing is not complete
                return new PrinterException(EPrinterException.PRINTER_NOT_COMPLETE);
            case -6:
                //Printer cutter error
                return new PrinterException(EPrinterException.PRINTER_CUTTER_ERROR);
            case -5:
                //Printer cover opening error
                return new PrinterException(EPrinterException.PRINTER_COVER_OPEN);
            case -2:
                //Printer packet too long
                return new PrinterException(EPrinterException.PRINTER_PACKET_TOO_LONG);
            default:
                //Unknow Error
                return new PrinterException(EPrinterException.PRINTER_UNKNOW_ERROR);
        }
    }

    /**
     * print single line
     * @param line single line
     * @return print status
     * @throws PrinterException exception
     */
    @Override
    public int print(String line) throws PrinterException {
        return print(line, null);
    }

    public int print(String line, String charset) throws PrinterException {
        if (idal == null) {
            throw new PrinterException(EPrinterException.NEPTUNE_ERROR);
        }
        com.pax.dal.IPrinter printer = idal.getPrinter();
        if (printer == null) {
            throw new PrinterException(EPrinterException.PRINTER_ERROR);
        }
        try {
            printer.init();
            printer.setGray(3);
            printer.printStr(line, charset);
            int result = printer.start();
            //0 means success,-4 means Printer has no font installed
            if (result == 0 || result == -4){
                return 0;
            }
            throw handleError(result);
        } catch (PrinterDevException e) {
            throw new PrinterException(e.getErrCode(), e.getErrMsg());
        }
    }
}
