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

import com.pax.dal.IChannel;
import com.pax.dal.IComm;
import com.pax.dal.IDalCommManager;
import com.pax.dal.entity.ApnInfo;
import com.pax.dal.entity.EChannelType;
import com.pax.dal.entity.ERoute;
import com.pax.dal.entity.EUartPort;
import com.pax.dal.entity.EWifiSleepPolicy;
import com.pax.dal.entity.LanParam;
import com.pax.dal.entity.MobileParam;
import com.pax.dal.entity.ModemParam;
import com.pax.dal.entity.UartParam;

import java.util.List;
/**
 * neptune IDalCommManager
 */
class DemoCommManager implements IDalCommManager {
    DemoCommManager() {
        //do nothing
    }

    @Override
    public IChannel getChannel(EChannelType eChannelType) {
        return null;
    }

    @Override
    public int enableChannelExclusive(EChannelType eChannelType, int i) {
        return 0;
    }

    @Override
    public void setLanParam(LanParam lanParam) {
        //do nothing
    }

    @Override
    public void setMobileParam(MobileParam mobileParam) {
        //do nothing
    }

    @Override
    public boolean disableMultiPath() {
        return false;
    }

    @Override
    public IComm getModemComm(ModemParam modemParam) {
        return null;
    }

    @Override
    public IComm getUartComm(UartParam uartParam) {
        return null;
    }

    @Override
    public List<EUartPort> getUartPortList() {
        return null;
    }

    @Override
    public boolean enableMultiPath() {
        return false;
    }

    @Override
    public boolean setRoute(String s, ERoute eRoute) {
        return false;
    }

    @Override
    public void setWifiSleepPolicy(EWifiSleepPolicy eWifiSleepPolicy) {
        //do nothing
    }

    @Override
    public byte getModemStatus() {
        return 0;
    }

    @Override
    public int switchAPN(String s, String s1, String s2, String s3, int i) {
        return 0;
    }

    @Override
    public void disableWifiHotspotAndHideSettings() {

    }

    @Override
    public void showWifiHotspotSettings() {

    }

    @Override
    public LanParam getLanConfig() {
        return null;
    }

    @Override
    public boolean addApns(byte[] bytes) {
        return false;
    }

    @Override
    public List<ApnInfo> getApnList() {
        return null;
    }

    @Override
    public ApnInfo getCurrentApn() {
        return null;
    }

    @Override
    public boolean removeApn(String s) {
        return false;
    }

    @Override
    public boolean removeRoute(String s, ERoute eRoute) {
        return false;
    }

    @Override
    public List<String> getRouteList() {
        return null;
    }

    @Override
    public IComm createUartComm(UartParam uartParam) {
        return null;
    }
}
