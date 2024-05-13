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
package com.evp.pay.trans.transmit;

import android.os.SystemClock;

import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;


/**
 * The type Tcp demo mode.
 */
class TcpDemoMode extends ATcp {

    @Override
    public int onConnect() {
        int ret = setCommParam();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        onShowMsg(Utils.getString(R.string.wait_connect));
        ret = connectDemo();
        return ret;
    }

    @Override
    public int onSend(byte[] data) {
        onShowMsg(Utils.getString(R.string.wait_send));
        SystemClock.sleep(1000);
        return TransResult.SUCC;
    }

    @Override
    public TcpResponse onRecv() {
        return new TcpResponse(TransResult.SUCC, null);
    }

    @Override
    public void onClose() {
        //do nothing
    }

    private int connectDemo() {
        SystemClock.sleep(1000);
        return TransResult.SUCC;
    }

}
