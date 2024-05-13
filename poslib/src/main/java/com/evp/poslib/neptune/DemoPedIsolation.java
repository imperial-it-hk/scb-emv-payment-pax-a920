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

import com.pax.dal.entity.AllocatedKeyInfo;
import com.pax.dal.entity.EPedKeySort;
import com.pax.dal.exceptions.PedDevException;
import com.pax.dal.pedkeyisolation.IPedKeyIsolation;

import java.util.List;
/**
 * neptune IPedKeyIsolation
 */
class DemoPedIsolation extends DemoPed implements IPedKeyIsolation {

    @Override
    public List<AllocatedKeyInfo> getAllocatedKeys(EPedKeySort ePedKeySort) {
        return null;
    }

    @Override
    public void changeRSAKeyOwner(int i, String s) throws PedDevException {

    }

    @Override
    public void changeTDKOwner(int i, String s) throws PedDevException {

    }

    @Override
    public void genRSAKey(byte b, byte b1, short i, byte b2) throws PedDevException {

    }
}
