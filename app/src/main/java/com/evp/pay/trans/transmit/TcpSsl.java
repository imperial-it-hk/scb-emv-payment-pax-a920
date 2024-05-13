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

import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.comm.ICommHelper;
import com.pax.gl.commhelper.ISslKeyStore;

import java.io.IOException;
import java.io.InputStream;

/**
 * Tcp with SSL
 */
class TcpSsl extends TcpNoSsl {

    private InputStream keyStoreStream;

    /**
     * Instantiates a new Tcp ssl.
     *
     * @param keyStoreStream the key store stream
     */
    TcpSsl(InputStream keyStoreStream) {
        this.keyStoreStream = keyStoreStream;
    }

    @Override
    protected void connectSub(ICommHelper commHelper, String hostIp, int port) {
        ISslKeyStore keyStore = commHelper.createSslKeyStore();
        if (keyStoreStream != null) {
            try {
                keyStoreStream.reset();
            } catch (IOException e) {
                LogUtils.e(TAG, "", e);
            }
        }
        keyStore.setTrustStore(keyStoreStream);
        client = commHelper.createSslClient(hostIp, port, keyStore);
    }
}
