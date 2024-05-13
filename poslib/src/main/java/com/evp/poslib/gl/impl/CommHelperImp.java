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
package com.evp.poslib.gl.impl;

import android.content.Context;

import com.evp.poslib.gl.comm.ICommHelper;
import com.pax.gl.commhelper.IBtScanner;
import com.pax.gl.commhelper.IBtServer;
import com.pax.gl.commhelper.ICommBt;
import com.pax.gl.commhelper.ICommSslClient;
import com.pax.gl.commhelper.ICommTcpClient;
import com.pax.gl.commhelper.IHttpClient;
import com.pax.gl.commhelper.IHttpsClient;
import com.pax.gl.commhelper.ISslKeyStore;
import com.pax.gl.commhelper.ITcpServer;
import com.pax.gl.commhelper.impl.PaxGLComm;

/**
 *
 */
class CommHelperImp implements ICommHelper {

    private PaxGLComm comm;

    CommHelperImp(Context context) {
        comm = PaxGLComm.getInstance(context);
    }

    public PaxGLComm getComm() {
        return comm;
    }

    /**
     * Bluetooth scanner
     *
     * @return IBtScanner
     */
    @Override
    public IBtScanner getBtScanner() {
        return comm.getBtScanner();
    }

    /**
     * ble scanner
     *
     * @return IBtScanner
     */
    @Override
    public IBtScanner getBtLeScanner() {
        return comm.getBleScanner();
    }

    /**
     * Bluetooth commHelper
     *
     * @param identifier identifier
     * @return ICommBt
     */
    @Override
    public ICommBt createBt(String identifier) {
        return comm.createBt(identifier);
    }

    /**
     * Bluetooth commHelper
     *
     * @param identifier identifier
     * @param useBle     useBle
     * @return ICommBt
     */
    @Override
    public ICommBt createBt(String identifier, boolean useBle) {
        return comm.createBt(identifier, useBle);
    }

    /**
     * create SSL Keystore
     *
     * @return ISslKeyStore
     */
    @Override
    public ISslKeyStore createSslKeyStore() {
        return comm.createSslKeyStore();
    }

    /**
     * create SSL Client
     *
     * @param host     host
     * @param port     port
     * @param keystore keystore
     * @return ICommSslClient
     */
    @Override
    public ICommSslClient createSslClient(String host, int port, ISslKeyStore keystore) {
        return comm.createSslClient(host, port, keystore);
    }

    /**
     * create Tcp Client
     *
     * @param host host
     * @param port port
     * @return ICommTcpClient
     */
    @Override
    public ICommTcpClient createTcpClient(String host, int port) {
        return comm.createTcpClient(host, port);
    }

    /**
     * create http client
     *
     * @return IHttpClient
     */
    @Override
    public IHttpClient createHttpClient() {
        return comm.createHttpClient();
    }

    /**
     * create https client
     *
     * @param keyStore keyStore
     * @return IHttpsClient
     */
    @Override
    public IHttpsClient createHttpsClient(ISslKeyStore keyStore) {
        return comm.createHttpsClient(keyStore);
    }

    /**
     * create tcp server
     *
     * @param port       port
     * @param maxTaskNum maxTaskNum
     * @param listener   listener
     * @return ITcpServer
     */
    @Override
    public ITcpServer createTcpServer(int port, int maxTaskNum, ITcpServer.IListener listener) {
        return comm.createTcpServer(port, maxTaskNum, listener);
    }

    /**
     * create bluetooth server
     *
     * @param maxTaskNum maxTaskNum
     * @param listener   listener
     * @return IBtServer
     */
    @Override
    public IBtServer createBtServer(int maxTaskNum, IBtServer.IListener listener) {
        return comm.createBtServer(maxTaskNum, listener);
    }
}
