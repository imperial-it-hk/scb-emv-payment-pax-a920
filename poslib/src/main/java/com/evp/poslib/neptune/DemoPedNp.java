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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */

package com.evp.poslib.neptune;

import com.pax.dal.IPedNp;
import com.pax.dal.entity.ECheckMode;
import com.pax.dal.entity.RSAKeyInfo;
import com.pax.dal.exceptions.PedDevException;
/**
 * neptune IPedNp
 */
public class DemoPedNp implements IPedNp {
    @Override
    public byte[] exportKey(RSAKeyInfo rsaKeyInfo, String s, int i, int i1) throws PedDevException {
        return new byte[0];
    }

    @Override
    public void genRandomKey(int i, int i1, int i2) throws PedDevException {
        //do nothing
    }

    @Override
    public void writeKey(byte b, byte b1, byte b2, byte b3, byte b4, byte[] bytes, byte b5, byte[] bytes1) throws PedDevException {
        //do nothing
    }

    @Override
    public void writeKeyVar(int i, int i1, int i2, int i3, byte[] bytes) throws PedDevException {
        //do nothing
    }

    @Override
    public byte[] exportKeyEncByRsa(int i, int i1, int i2, byte b) throws PedDevException {
        return new byte[0];
    }

    @Override
    public void genRsaKey(byte b, byte b1, int i, int i1) throws PedDevException {

    }

    @Override
    public void writeKeyEncByRsa(byte[] bytes, byte b, byte b1, byte b2, byte[] bytes1, ECheckMode eCheckMode, byte[] bytes2) throws PedDevException {

    }

    @Override
    public boolean erase() throws PedDevException {
        return false;
    }

    @Override
    public byte[] exportKeyOAEP(RSAKeyInfo rsaKeyInfo, byte[] bytes, int i, int i1) throws PedDevException {
        return new byte[0];
    }

    @Override
    public void asDeriveKey(byte b, byte b1, byte b2, byte b3, byte[] bytes, byte[] bytes1, byte[] bytes2, byte b4) throws PedDevException {

    }

    @Override
    public byte[] asLoadKeyAsym(byte b, byte b1, byte[] bytes, byte b2) throws PedDevException {
        return new byte[0];
    }

    @Override
    public void asLoadKEK(byte b, byte b1, byte b2, byte b3, byte[] bytes, byte[] bytes1, byte[] bytes2, byte b4) throws PedDevException {

    }

    @Override
    public void asRollKeys(byte b, byte b1, byte b2, byte b3) throws PedDevException {

    }
}
