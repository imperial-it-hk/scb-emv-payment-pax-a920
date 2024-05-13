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
package com.evp.eemv.entity;

import com.evp.eemv.utils.Tools;
import com.pax.jemv.clcommon.CLSS_TORN_LOG_RECORD;

/**
 * emv contactless tornlog record
 */
public class ClssTornLogRecord {

    private CLSS_TORN_LOG_RECORD clssTornLogRecord;

    public ClssTornLogRecord() {
        clssTornLogRecord = new CLSS_TORN_LOG_RECORD();
    }

    public ClssTornLogRecord(CLSS_TORN_LOG_RECORD record) {
        clssTornLogRecord = record;
    }

    public ClssTornLogRecord(String pan, boolean panSeqFlg, byte panSeq, byte[] tornData, int tornDataLen) {
        byte[] bcdPan = Tools.str2Bcd(pan);
        clssTornLogRecord = new CLSS_TORN_LOG_RECORD(bcdPan, (byte) bcdPan.length, Tools.boolean2Byte(panSeqFlg), panSeq, tornData, tornDataLen);
    }

    /**
     * get pan
     * @return pan
     */
    public String getPan() {
        return Tools.bcd2Str(clssTornLogRecord.aucPAN, clssTornLogRecord.ucPANLen);
    }

    /**
     * set pan
     * @param pan pan
     */
    public void setPan(String pan) {
        clssTornLogRecord.aucPAN = Tools.str2Bcd(pan);
        clssTornLogRecord.ucPANLen = (byte) clssTornLogRecord.aucPAN.length;
    }

    /**
     * get pan sequence flag
     * @return a boolean value
     */
    public boolean getPanSeqFlg() {
        return Tools.byte2Boolean(clssTornLogRecord.ucPANSeqFlg);
    }

    /**
     * set pan sequence flag
     * @param panSeqFlg panSeqFlg
     */
    public void setPanSeqFlg(boolean panSeqFlg) {
        clssTornLogRecord.ucPANSeqFlg = Tools.boolean2Byte(panSeqFlg);
    }

    /**
     * get pan sequence
     * @return pan sequence
     */
    public byte getPanSeq() {
        return clssTornLogRecord.ucPANSeq;
    }

    /**
     * set pan sequence
     * @param panSeq pan sequence
     */
    public void setPanSeq(byte panSeq) {
        clssTornLogRecord.ucPANSeq = panSeq;
    }

    /**
     * get torn data
     * @return torn data
     */
    public byte[] getTornData() {
        return clssTornLogRecord.aucTornData;
    }

    /**
     * set torn data
     * @param tornData torn data
     */
    public void setTornData(byte[] tornData) {
        clssTornLogRecord.aucTornData = tornData;
    }

    /**
     * get torn data length
     * @return torn data length
     */
    public int getTornDataLen() {
        return clssTornLogRecord.unTornDataLen;
    }

    /**
     * set torn data length
     * @param tornDataLen torn data length
     */
    public void setTornDataLen(int tornDataLen) {
        clssTornLogRecord.unTornDataLen = tornDataLen;
    }
}
