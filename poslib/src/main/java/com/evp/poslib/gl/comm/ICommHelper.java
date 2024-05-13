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
package com.evp.poslib.gl.comm;

import com.pax.gl.commhelper.IBtScanner;
import com.pax.gl.commhelper.IBtServer;
import com.pax.gl.commhelper.ICommBt;
import com.pax.gl.commhelper.ICommSslClient;
import com.pax.gl.commhelper.ICommTcpClient;
import com.pax.gl.commhelper.IHttpClient;
import com.pax.gl.commhelper.IHttpsClient;
import com.pax.gl.commhelper.ISslKeyStore;
import com.pax.gl.commhelper.ITcpServer;

public interface ICommHelper {
    /**
     * Bluetooth scanner
     * @return IBtScanner
     */
    IBtScanner getBtScanner();

    /**
     * ble scanner
     * @return IBtScanner
     */
    IBtScanner getBtLeScanner();

    /**
     * Bluetooth commHelper
     * @param identifier identifier
     * @return ICommBt
     */
    ICommBt createBt(String identifier);

    /**
     * Bluetooth commHelper
     * @param identifier identifier
     * @param useBle useBle
     * @return ICommBt
     */
    ICommBt createBt(String identifier, boolean useBle);

    /**
     * create SSL Keystore
     * @return ISslKeyStore
     */
    ISslKeyStore createSslKeyStore();

    /**
     * create SSL Client
     * @param host host
     * @param port port
     * @param keystore keystore
     * @return ICommSslClient
     */
    ICommSslClient createSslClient(String host, int port, ISslKeyStore keystore);

    /**
     * create Tcp Client
     * @param host host
     * @param port port
     * @return ICommTcpClient
     */
    ICommTcpClient createTcpClient(String host, int port);

    /**
     * create http client
     * @return IHttpClient
     */
    IHttpClient createHttpClient();

    /**
     * create https client
     * @param keyStore keyStore
     * @return IHttpsClient
     */
    IHttpsClient createHttpsClient(ISslKeyStore keyStore);

    /**
     * create tcp server
     * @param port port
     * @param maxTaskNum maxTaskNum
     * @param listener listener
     * @return ITcpServer
     */
    ITcpServer createTcpServer(int port, int maxTaskNum, ITcpServer.IListener listener);

    /**
     * create bluetooth server
     * @param maxTaskNum maxTaskNum
     * @param listener listener
     * @return IBtServer
     */
    IBtServer createBtServer(int maxTaskNum, IBtServer.IListener listener);
}

/* Location:           D:\Android逆向助手_v2.2\PaxGL_V1.00.04_20170303.jar
 * Qualified Name:     com.pax.gl.comm.ICommHelper
 * JD-Core Version:    0.6.0
 */