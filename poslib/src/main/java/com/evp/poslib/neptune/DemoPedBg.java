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

import com.pax.dal.IPedBg;
import com.pax.dal.entity.RSAKeyInfo;
import com.pax.dal.exceptions.PedDevException;
/**
 * neptune IPedBg
 */
public class DemoPedBg implements IPedBg {
    @Override
    public byte[] des(byte b, byte[] bytes, byte b1, byte[] bytes1) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] generateKia(byte b, byte b1, byte[] bytes) throws PedDevException {
        return new byte[0];
    }

    @Override
    public void generateKPE(byte b, byte b1, byte[] bytes, byte[] bytes1) throws PedDevException {
        //do nothing
    }

    @Override
    public byte[] getKeyKvc(byte b, byte b1) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] getKi(RSAKeyInfo rsaKeyInfo, byte[] bytes, byte[] bytes1, byte[] bytes2) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] getMac(byte b, byte[] bytes, byte b1) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] getPinblock(byte b, String s, byte[] bytes, byte b1, int i) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] loadKca(byte b, byte[] bytes) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] loadKEK(byte b, byte b1, byte b2, byte[] bytes, byte[] bytes1, byte[] bytes2) throws PedDevException {
        return new byte[0];
    }

    @Override
    public void loadSessionKeys(byte[] bytes, byte[][] bytes1, byte[] bytes2) throws PedDevException {
        //do nothing
    }

    @Override
    public byte[] readCipherPKtcu(byte b) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] readPpasn(byte b, byte b1) throws PedDevException {
        return new byte[0];
    }

    @Override
    public byte[] readPpid(byte b) throws PedDevException {
        return new byte[0];
    }

    @Override
    public RSAKeyInfo readRsaKey(byte b) throws PedDevException {
        return null;
    }

    @Override
    public void rollKeys(byte b, byte b1, byte b2, byte[] bytes) throws PedDevException {
        //do nothing
    }

    @Override
    public void verifyMac(byte b, byte[] bytes, byte b1, byte[] bytes1) throws PedDevException {
        //do nothing
    }

    @Override
    public void writeCipherPKtcu(byte b, byte[] bytes) throws PedDevException {
        //do nothing
    }

    @Override
    public void writePpasn(byte b, byte[] bytes) throws PedDevException {
        //do nothing
    }

    @Override
    public void writePpid(byte b, byte[] bytes) throws PedDevException {
        //do nothing
    }
}
