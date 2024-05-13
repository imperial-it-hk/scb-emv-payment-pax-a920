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

import com.pax.dal.IDeviceInfo;
import com.pax.dal.entity.BatterySipper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * neptune IDeviceInfo
 */
class DemoDeviceInfo implements IDeviceInfo {

    private Map<Integer, ESupported> supportedMap = new HashMap<>();

    DemoDeviceInfo() {
        //do nothing
        supportedMap.put(MODULE_MAG, ESupported.YES);
        supportedMap.put(MODULE_ICC, ESupported.YES);
        supportedMap.put(MODULE_PICC, ESupported.YES);
        supportedMap.put(MODULE_PED, ESupported.NO);
        supportedMap.put(MODULE_KEYBOARD, ESupported.NO);
        supportedMap.put(MODULE_PRINTER, ESupported.NO);
        supportedMap.put(MODULE_BT, ESupported.YES);
        supportedMap.put(MODULE_CASH_BOX, ESupported.NO);
        supportedMap.put(MODULE_CUSTOMER_DISPLAY, ESupported.NO);
        supportedMap.put(MODULE_ETHERNET, ESupported.NO);
        supportedMap.put(MODULE_FINGERPRINT_READER, ESupported.NO);
        supportedMap.put(MODULE_G_SENSOR, ESupported.YES);
        supportedMap.put(MODULE_HDMI, ESupported.NO);
        supportedMap.put(MODULE_ID_CARD_READER, ESupported.NO);
        supportedMap.put(MODULE_SM, ESupported.NO);

    }

    @Override
    public Map<Integer, ESupported> getModuleSupported() {
        return supportedMap;
    }

    @Override
    public ESupported getModuleSupported(int i) {
        return supportedMap.get(i);
    }

    @Override
    public long getUsageCount(int i) {
        return 0;
    }

    @Override
    public List<BatterySipper> getBatteryUsages() {
        return null;
    }

    @Override
    public long getFailCount(int i) {
        return 0;
    }

    @Override
    public int getPrinterStatus() {
        return 0;
    }

    @Override
    public Map<String, Long> getTrafficOfEachApp(int i, long l, long l1) {
        return null;
    }

    @Override
    public long getTrafficTotal(int i, long l, long l1) {
        return 0;
    }
}
