package com.evp.bizlib.onlinepacket.impl;


import androidx.annotation.NonNull;

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.commonlib.utils.LogUtils;
import com.pax.dal.exceptions.PedDevException;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.annotation.RouterService;

/**
 * pack DCC get rate
 */
@RouterService(interfaces = IPacker.class,key = OnlinePacketConst.PACKET_DCC_GET_RATE)
public class PackDccGetRate extends PackIso8583 {

    private static final String TAG = "PackDccGetRate";

    public PackDccGetRate() {
        super();
    }

    @Override
    @NonNull
    public byte[] pack(@NonNull TransData transData) throws PedDevException {

        LogUtils.i(TAG, "DccGetRate ISO pack START");

        try {
            setHeader(transData);
            setBitData3(transData);
            setBitData2(transData);
            setBitData4(transData);
            setBitData11(transData);
            setBitData14(transData);
            setBitData22(transData);
            setBitData24(transData);
            setBitData25(transData);
            setBitData35(transData);
            setBitData41(transData);
            setBitData42(transData);
            setBitData63(transData);
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
            return "".getBytes();
        }

        LogUtils.i(TAG, "DccGetRate ISO pack END");

        return pack(transData, false);
    }

    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        byte[] data = new byte[]{0x00, 0x05, 0x44, 0x49, 0x30, 0x30, 0x32};
        setBitData("63", data);
    }
}
