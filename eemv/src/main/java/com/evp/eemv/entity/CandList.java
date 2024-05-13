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
 * 20190108  	         linhb                   Create
 * ===========================================================================================
 */

package com.evp.eemv.entity;

import com.evp.eemv.utils.Tools;

public class CandList {
    private byte[] appPreName;
    private byte[] appLabel;
    private byte[] issDiscrData;
    private byte[] aid;
    private byte aidLen;
    private byte priority;
    private byte[] appName;
    private byte[] reserve;

    public CandList() {
        this.appPreName = new byte[17];
        this.appLabel = new byte[17];
        this.issDiscrData = new byte['ô'];
        this.aid = new byte[17];
        this.aidLen = 0;
        this.priority = 0;
        this.appName = new byte[33];
        this.reserve = new byte[2];
    }

    public String getAppPreName() {
        return Tools.bytes2String(this.appPreName);
    }

    public void setAppPreName(String appPreName) {
        this.appPreName = Tools.string2Bytes(appPreName);
    }

    public String getAppLabel() {
        return Tools.bytes2String(this.appLabel);
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = Tools.string2Bytes(appLabel);
    }

    public byte[] getIssDiscrData() {
        return this.issDiscrData;
    }

    public void setIssDiscrData(byte[] issDiscrData) {
        this.issDiscrData = issDiscrData;
    }

    public byte[] getAid() {
        return this.aid;
    }

    public void setAid(byte[] aid) {
        this.aid = aid;
    }

    public byte getAidLen() {
        return this.aidLen;
    }

    public void setAidLen(byte aidLen) {
        this.aidLen = aidLen;
    }

    public byte getPriority() {
        return this.priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public String getAppName() {
        return Tools.bytes2String(this.appName);
    }

    public void setAppName(String appName) {
        this.appName = Tools.string2Bytes(appName);
    }

    public byte[] getReserve() {
        return this.reserve;
    }

    public void setReserve(byte[] reserve) {
        this.reserve = reserve;
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.entity.CandList
 * JD-Core Version:    0.6.0
 */