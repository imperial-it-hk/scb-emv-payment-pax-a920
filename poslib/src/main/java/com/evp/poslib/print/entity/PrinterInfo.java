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
package com.evp.poslib.print.entity;

/**
 * Bluetooth relative entity for both BE and BP printer
 */
public class PrinterInfo {
    //Device Name
    private String name;
    //Device MAC
    private String identifier;

    public PrinterInfo() {
    }

    /**
     * init printer info
     * @param name printer name
     * @param identifier printer identifier
     */
    public PrinterInfo(String name, String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    /**
     * get printer name
     * @return printer name
     */
    public String getName() {
        return name;
    }

    /**
     * get identifier
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * get printer info
     * @return printer info
     */
    @Override
    public String toString() {
        return name + "\n" + identifier;
    }
}
