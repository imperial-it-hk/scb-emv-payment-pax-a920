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
 * Packer interface
 *
 * @param <T>
 * @param <O>
 * @author Steven.W
 */
public interface IPacker<T, O> {
    /**
     * pack
     *
     * @param t input transaction data structure
     * @return output data
     */
    @NonNull
    O pack(@NonNull T t) throws PedDevException;

    /**
     * unpack
     *
     * @param t transaction data structure
     * @param o input data
     * @return result
     */
    int unpack(@NonNull T t, O o) throws PedDevException;
}
