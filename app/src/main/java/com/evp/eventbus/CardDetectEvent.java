/*-------------------------------------------------------------------------------------------------
 -                                                                                                -
 - Copyright (c)                                                                                  -
 - PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION                             -
 - This software is supplied under the terms of a license agreement or nondisclosure              -
 - agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or             -
 - disclosed except in accordance with the terms in that agreement.                               -
 - Copyright (c) 2020. PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.            -
 -                                                                                                -
 -                                                                                                -
 -                                                                                                -
 -------------------------------------------------------------------------------------------------*/
package com.evp.eventbus;

import com.evp.bizlib.data.model.SearchMode;

public class CardDetectEvent {

    /**
     * The Type.
     */
    public Byte type;
    /**
     * The Track data 1.
     */
    public String trackData1;
    /**
     * The Track data 2.
     */
    public String trackData2;
    /**
     * The Track data 3.
     */
    public String trackData3;
    /**
     * The Serial info.
     */
    public byte[] serialInfo;
    /**
     * Fallback info
     */
    public boolean isThisFallback;

    private CardDetectEvent(Byte type, String trackData1, String trackData2, String trackData3, byte[] serialInfo, boolean isThisFallback) {
        this.type = type;
        this.trackData1 = trackData1;
        this.trackData2 = trackData2;
        this.trackData3 = trackData3;
        this.serialInfo = serialInfo;
        this.isThisFallback = isThisFallback;
    }

    /**
     * On mag detected card detect event.
     *
     * @param trackData1 the track data 1
     * @param trackData2 the track data 2
     * @param trackData3 the track data 3
     * @return the card detect event
     */
    public static CardDetectEvent onMagDetected(String trackData1, String trackData2, String trackData3, boolean isThisFallback) {
        return new CardDetectEvent(SearchMode.SWIPE, trackData1, trackData2, trackData3, null, isThisFallback);
    }

    /**
     * On ic detected card detect event.
     *
     * @return the card detect event
     */
    public static CardDetectEvent onIcDetected() {
        return new CardDetectEvent(SearchMode.INSERT, null, null, null, null, false);
    }

    /**
     * On internal picc detected card detect event.
     *
     * @param serialInfo the serial info
     * @return the card detect event
     */
    public static CardDetectEvent onInternalPiccDetected(byte[] serialInfo) {
        return new CardDetectEvent(SearchMode.INTERNAL_WAVE, null, null, null, serialInfo, false);
    }

    /**
     * On external picc detected card detect event.
     *
     * @param serialInfo the serial info
     * @return the card detect event
     */
    public static CardDetectEvent onExPiccDetected(byte[] serialInfo) {
        return new CardDetectEvent(SearchMode.EXTERNAL_WAVE, null, null, null, serialInfo, false);
    }

}