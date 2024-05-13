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
package com.evp.bizlib.data.model;

public class SearchMode {
    /**
     * 刷卡
     */
    public static final byte SWIPE = 0x01;
    /**
     * 插卡
     */
    public static final byte INSERT = 0x02;
    public static final byte INTERNAL_WAVE = 0x04;
    public static final byte EXTERNAL_WAVE = 0x08;
    /**
     * 挥卡
     */
    public static final byte WAVE = 0xC;
    /**
     * 支持手输
     */
    public static final byte KEYIN = 0x10;
    /**
     * 扫码
     */
    public static final byte QR = 0x20;

    private SearchMode() {

    }

    public static boolean contain(byte mode, byte mode2) {
        return (mode & mode2) == mode2;
    }

    public static boolean isWave(byte mode) {
        return ((mode & INTERNAL_WAVE) == INTERNAL_WAVE) || ((mode & EXTERNAL_WAVE) == EXTERNAL_WAVE);
    }

    /**
     * check whether support swipe card
     * @param readType reader type
     * @return status of whether support swipe card
     */
    public static boolean isSupportMag(byte readType) {
        return (readType & SWIPE) == SWIPE;
    }
    /**
     * check whether support IC card
     * @param readType reader type
     * @return status of whether support IC card
     */
    public static boolean isSupportIcc(byte readType){
        return (readType & INSERT) == INSERT;
    }
    /**
     * check whether support internal ContactLess
     * @param readType reader type
     * @return status of whether support internal ContactLess
     */
    public static boolean isSupportInternalPicc(byte readType){
        return (readType & INTERNAL_WAVE) == INTERNAL_WAVE;
    }
    /**
     * check whether support External ContactLess
     * @param readType reader type
     * @return status of whether support External ContactLess
     */
    public static boolean isSupportExternalPicc(byte readType){
        return (readType & EXTERNAL_WAVE) == EXTERNAL_WAVE;
    }
}
