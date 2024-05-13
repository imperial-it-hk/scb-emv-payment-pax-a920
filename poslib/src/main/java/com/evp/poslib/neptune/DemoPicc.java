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

import com.pax.dal.IPicc;
import com.pax.dal.entity.ApduRespInfo;
import com.pax.dal.entity.ApduSendInfo;
import com.pax.dal.entity.EDetectMode;
import com.pax.dal.entity.EM1KeyType;
import com.pax.dal.entity.EM1OperateType;
import com.pax.dal.entity.EPiccRemoveMode;
import com.pax.dal.entity.EUartPort;
import com.pax.dal.entity.PiccCardInfo;
import com.pax.dal.entity.PiccPara;
import com.pax.dal.exceptions.PiccDevException;
/**
 * neptune IPicc
 */
class DemoPicc implements IPicc {

    DemoPicc() {
        //do nothing
    }

    @Override
    public void open() {
        //do nothing
    }

    @Override
    public PiccPara readParam() {
        return null;
    }

    @Override
    public void setParam(PiccPara var1) {
        //do nothing
    }

    @Override
    public void setFelicaTimeOut(long var1) {
        //do nothing
    }

    @Override
    public PiccCardInfo detect(EDetectMode var1) {
        return new PiccCardInfo("A".getBytes()[0], new byte[]{0x00}, (byte) 0, null);
    }

    @Override
    public PiccCardInfo detect(byte b) throws PiccDevException {
        return new PiccCardInfo("A".getBytes()[0], new byte[]{0x00}, (byte) 0, null);
    }

    @Override
    public byte[] isoCommand(byte var1, byte[] var2) {
        return new byte[]{0x00};
    }

    @Override
    public void remove(EPiccRemoveMode var1, byte var2) {
        //do nothing
    }

    @Override
    public void close() {
        //do nothing
    }

    @Override
    public void m1Auth(EM1KeyType var1, byte var2, byte[] var3, byte[] var4) {
        //do nothing
    }

    @Override
    public byte[] m1Read(byte var1) {
        return new byte[]{0x00};
    }

    @Override
    public void m1Write(byte var1, byte[] var2) {
        //do nothing
    }

    @Override
    public void m1Operate(EM1OperateType var1, byte var2, byte[] var3, byte var4) {
        //do nothing
    }

    @Override
    public void initFelica(byte var1, byte var2) {
        //do nothing
    }

    @Override
    public void setLed(byte var1) {
        //do nothing
    }

    @Override
    public ApduRespInfo isoCommandByApdu(byte var1, ApduSendInfo var2) {
        return new ApduRespInfo((byte) 0x90, (byte) 0x00, null, 0);
    }

    @Override
    public byte[] cmdExchange(byte[] var1, int var2) {
        return new byte[]{0x00};
    }

    @Override
    public void setPort(EUartPort eUartPort) {

    }

    @Override
    public void setFelicaTimeout(int i) throws PiccDevException {

    }

    @Override
    public PiccCardInfo detect(byte b, byte[] bytes) throws PiccDevException {
        return null;
    }
}
