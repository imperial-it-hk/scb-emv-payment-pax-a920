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

public class Capk {
    private byte[] rid;
    private byte keyID;
    private byte hashInd;
    private byte arithInd;
    private byte[] modul;
    private byte[] exponent;
    private byte[] expDate;
    private byte[] checkSum;

    public Capk() {
        this.rid = new byte[0];
        this.keyID = 0;
        this.hashInd = 0;
        this.arithInd = 0;
        this.modul = new byte[0];
        this.exponent = new byte[0];
        this.expDate = new byte[0];
        this.checkSum = new byte[0];
    }

    public byte[] getRid() {
        return this.rid;
    }

    public void setRid(byte[] rid) {
        this.rid = rid;
    }

    public byte getKeyID() {
        return this.keyID;
    }

    public void setKeyID(byte keyID) {
        this.keyID = keyID;
    }

    public byte getHashInd() {
        return this.hashInd;
    }

    public void setHashInd(byte hashInd) {
        this.hashInd = hashInd;
    }

    public byte getArithInd() {
        return this.arithInd;
    }

    public void setArithInd(byte arithInd) {
        this.arithInd = arithInd;
    }

    public byte[] getModul() {
        return this.modul;
    }

    public void setModul(byte[] modul) {
        this.modul = modul;
    }

    public byte[] getExponent() {
        return this.exponent;
    }

    public void setExponent(byte[] exponent) {
        this.exponent = exponent;
    }

    public byte[] getExpDate() {
        return this.expDate;
    }

    public void setExpDate(byte[] expDate) {
        this.expDate = expDate;
    }

    public byte[] getCheckSum() {
        return this.checkSum;
    }

    public void setCheckSum(byte[] checkSum) {
        this.checkSum = checkSum;
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.entity.Capk
 * JD-Core Version:    0.6.0
 */