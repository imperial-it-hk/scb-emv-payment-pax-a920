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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.bizlib.onlinepacket;

import androidx.annotation.NonNull;

import com.pax.dal.exceptions.PedDevException;

/**
 * packer listener
 *
 * @author Steven.W
 */
public interface PackListener {
    /**
     * calc MAC
     *
     * @param data input data
     * @return mac value
     */
    @NonNull
    byte[] onCalcMac(byte[] data) throws PedDevException;

    /**
     * encrypt track
     *
     * @param track input track
     * @return encrypted track data
     */
    @NonNull
    byte[] onEncTrack(byte[] track);
}
