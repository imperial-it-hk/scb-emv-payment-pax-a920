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

import com.pax.dal.IIcc;
import com.pax.dal.entity.ApduRespInfo;
import com.pax.dal.entity.ApduSendInfo;
import com.pax.dal.entity.IccPara;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.memorycard.ICardAT24Cxx;
import com.pax.dal.memorycard.ICardAT88SC102;
import com.pax.dal.memorycard.ICardAT88SC153;
import com.pax.dal.memorycard.ICardAT88SC1608;
import com.pax.dal.memorycard.ICardSle4428;
import com.pax.dal.memorycard.ICardSle4442;
/**
 * neptune IIcc
 */
class DemoIcc implements IIcc {

    DemoIcc() {
        //do nothing
    }

    @Override
    public byte[] init(byte var1) {
        return new byte[]{0x00};
    }

    @Override
    public void close(byte var1) {
        //do nothing
    }

    @Override
    public void autoResp(byte var1, boolean var2) {
        //do nothing
    }

    @Override
    public byte[] isoCommand(byte var1, byte[] var2) {
        return new byte[]{0x00};
    }

    @Override
    public boolean detect(byte var1) {
        return true;
    }

    @Override
    public void light(boolean var1) {
        //do nothing
    }

    @Override
    public ApduRespInfo isoCommandByApdu(byte var1, ApduSendInfo var2) {
        return null;
    }

    @Override
    public ICardAT24Cxx getCardAT24Cxx() {
        return null;
    }

    @Override
    public ICardAT88SC102 getCardAT88SC102() {
        return null;
    }

    @Override
    public ICardAT88SC153 getCardAT88SC153() {
        return null;
    }

    @Override
    public ICardSle4428 getCardSle4428() {
        return null;
    }

    @Override
    public ICardAT88SC1608 getCardAT88SC1608() {
        return null;
    }

    @Override
    public ICardSle4442 getCardSle4442() {
        return null;
    }

    @Override
    public IccPara readParam(byte b) throws IccDevException {
        return null;
    }

    @Override
    public void setParam(byte b, IccPara iccPara) throws IccDevException {

    }

}
