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

import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.comm.ICommHelper;
import com.evp.poslib.gl.impl.GL;
import com.evp.settings.SysParam;
import com.pax.gl.commhelper.exception.CommException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tcp with no ssl
 */
class TcpNoSsl extends ATcp {

    @Override
    public int onConnect() {
        int ret = setCommParam();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        int timeout = SysParam.getInstance().getInt(R.string.COMM_TIMEOUT) * 1000;
        hostIp = getMainHostIp();
        hostPort = getMainHostPort();
        onShowMsg(Utils.getString(R.string.wait_connect));
        ret = connect(hostIp, hostPort, timeout);
        onHideMsg();
        return ret;
    }

    @Override
    public int onSend(byte[] data) {
        try {
            onShowMsg(Utils.getString(R.string.wait_send));
            client.send(data);
            return TransResult.SUCC;
        } catch (CommException e) {
            LogUtils.e(TAG, "", e);
        }
        return TransResult.ERR_SEND;
    }

    @Override
    public TcpResponse onRecv() {
        try {
            onShowMsg(Utils.getString(R.string.wait_recv));
            byte[] lenBuf = client.recv(2);
            if (lenBuf == null || lenBuf.length != 2) {
                return new TcpResponse(TransResult.ERR_RECV, null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = (((lenBuf[0] << 8) & 0xff00) | (lenBuf[1] & 0xff));
            byte[] rsp = client.recv(len);
            if (rsp == null || rsp.length != len) {
                return new TcpResponse(TransResult.ERR_RECV, null);
            }
            baos.write(rsp);
            rsp = baos.toByteArray();
            return new TcpResponse(TransResult.SUCC, rsp);
        } catch (IOException | CommException e) {
            LogUtils.e(TAG, "", e);
        }

        return new TcpResponse(TransResult.ERR_RECV, null);
    }

    @Override
    public void onClose() {
        try {
            client.disconnect();
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
        }
    }

    private int connect(String hostIp, int port, int timeout) {
        if (hostIp == null || !Utils.checkIp(hostIp)) {
            return TransResult.ERR_CONNECT;
        }

        ICommHelper commHelper = GL.getGL().getCommHelper();
        connectSub(commHelper, hostIp, port);
        client.setConnectTimeout(timeout);
        client.setRecvTimeout(timeout);
        try {
            client.connect();
            return TransResult.SUCC;
        } catch (CommException e) {
            LogUtils.e(TAG, "", e);
        }
        return TransResult.ERR_CONNECT;
    }

    /**
     * Connect sub.
     *
     * @param commHelper the comm helper
     * @param hostIp     the host ip
     * @param port       the port
     */
    protected void connectSub(ICommHelper commHelper, String hostIp, int port) {
        client = commHelper.createTcpClient(hostIp, port);
    }

}
