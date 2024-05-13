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
package com.evp.poslib.print.exception;

import com.pax.dal.exceptions.AGeneralException;

/**
 * all printer relative error will convert to PrinterException
 */
public class PrinterException extends AGeneralException {
    private static final long serialVersionUID = 1L;
    private static final String MODULE = "PRINTER";

    /**
     * init exception
     * @param errCode errCode
     * @param errMsg errMsg
     */
    public PrinterException(int errCode, String errMsg) {
        super(MODULE, errCode, errMsg);
    }

    /**
     * init exception
     * @param error error
     */
    public PrinterException(EPrinterException error) {
        this(error.getErrCodeFromBasement(), error.getErrMsg());
    }
}
