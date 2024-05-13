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
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.dal.IChannel;
import com.pax.dal.IDalCommManager;
import com.pax.dal.entity.EChannelType;
import com.pax.dal.entity.ERoute;
import com.pax.dal.entity.LanParam;
import com.pax.dal.entity.MobileParam;
import com.pax.gl.commhelper.IComm;

/**
 * The type A tcp.
 */
@SuppressWarnings({"WeakerAccess", "JavaDoc"})
public abstract class ATcp {

    /**
     * The constant TAG.
     */
    protected static final String TAG = "A TCP";

    /**
     * The Client.
     */
    protected IComm client;
    /**
     * The Host ip.
     */
    protected String hostIp;
    /**
     * The Host port.
     */
    protected int hostPort;

    /**
     * The Trans process listener.
     */
    protected TransProcessListener transProcessListener;

    /**
     * 建立连接
     *
     * @return int
     */
    public abstract int onConnect();

    /**
     * 发送数据
     *
     * @param data the data
     * @return int
     */
    public abstract int onSend(byte[] data);

    /**
     * 接收数据
     *
     * @return tcp response
     */
    public abstract TcpResponse onRecv();

    /**
     * 关闭连接
     */
    public abstract void onClose();

    /**
     * 设置监听器
     *
     * @param listener the listener
     */
    protected void setTransProcessListener(TransProcessListener listener) {
        this.transProcessListener = listener;
    }

    /**
     * On show msg.
     *
     * @param msg the msg
     */
    protected void onShowMsg(String msg) {
        if (transProcessListener != null) {
            transProcessListener.onShowProgress(msg, SysParam.getInstance().getInt(R.string.COMM_TIMEOUT));
        }
    }

    /**
     * On show msg.
     *
     * @param msg        the msg
     * @param timeoutSec the timeout sec
     */
    @SuppressWarnings("SameParameterValue")
    protected void onShowMsg(String msg, int timeoutSec) {
        if (transProcessListener != null) {
            transProcessListener.onShowProgress(msg, timeoutSec);
        }
    }

    /**
     * On hide msg.
     */
    protected void onHideMsg() {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
    }

    /**
     * 参数设置和路由选择
     *
     * @return comm param
     */
    protected int setCommParam() {
        IDalCommManager commManager = FinancialApplication.getDal().getCommManager();
        String commType = SysParam.getInstance().getString(R.string.COMM_TYPE);

        if (!isNetworkEnable(commType)) {
            return TransResult.ERR_CONNECT;
        }

        switch (commType) {
            case SysParam.CommType.LAN:
                commManager.setLanParam(loadLanParam());
                commManager.setRoute(getMainHostIp(), ERoute.ETHERNET);
                break;
            case SysParam.CommType.MOBILE:
                // mobile参数设置
                commManager.setMobileParam(loadMobileParam());
                commManager.setRoute(getMainHostIp(), ERoute.MOBILE);
                break;
            case SysParam.CommType.DEMO:
                onShowMsg(Utils.getString(R.string.wait_demo_mode), 5);
                return TransResult.SUCC;
            case SysParam.CommType.WIFI:
                commManager.setRoute(getMainHostIp(), ERoute.WIFI);
                break;
            default:
                return TransResult.ERR_CONNECT;

        }
        onShowMsg(Utils.getString(R.string.wait_initialize_net));
        return TransResult.SUCC;
    }

    private MobileParam loadMobileParam() {
        SysParam sysParam = SysParam.getInstance();
        MobileParam param = new MobileParam();
        param.setApn(sysParam.getString(R.string.MOBILE_APN));
        param.setPassword(sysParam.getString(R.string.MOBILE_PWD));
        param.setUsername(sysParam.getString(R.string.MOBILE_USER));
        return param;
    }

    private LanParam loadLanParam() {
        SysParam sysParam = SysParam.getInstance();
        LanParam lanParam = new LanParam();
        lanParam.setDhcp(sysParam.getBoolean(R.string.LAN_DHCP));
        lanParam.setDns1(sysParam.getString(R.string.LAN_DNS1));
        lanParam.setDns2(sysParam.getString(R.string.LAN_DNS2));
        lanParam.setGateway(sysParam.getString(R.string.LAN_GATEWAY));
        lanParam.setLocalIp(sysParam.getString(R.string.LAN_LOCAL_IP));
        lanParam.setSubnetMask(sysParam.getString(R.string.LAN_NETMASK));
        return lanParam;
    }

    /**
     * 获取主机地址
     *
     * @return main host ip
     */
    protected String getMainHostIp() {
        return FinancialApplication.getAcqManager().getCurAcq().getIp();
    }

    /**
     * 获取主机端口
     *
     * @return main host port
     */
    protected int getMainHostPort() {
        return FinancialApplication.getAcqManager().getCurAcq().getPort();
    }

    /**
     * 获取备份主机地址
     *
     * @return back host ip
     */
    protected String getBackHostIp() {
        return FinancialApplication.getAcqManager().getCurAcq().getIpBak1();
    }

    /**
     * 获取备份主机端口
     *
     * @return back host port
     */
    protected int getBackHostPort() {
        return FinancialApplication.getAcqManager().getCurAcq().getPortBak1();
    }

    private EChannelType toChannelType(String commType) {
        switch (commType) {
            case SysParam.CommType.LAN:
                return EChannelType.LAN;
            case SysParam.CommType.MOBILE:
                return EChannelType.MOBILE;
            case SysParam.CommType.WIFI:
                return EChannelType.WIFI;
            default:
                throw new IllegalArgumentException("unsupport commtype !");
        }
    }

    private boolean isNetworkEnable(String commType) {
        if (Utils.getString(R.string.demo).equals(commType)) {
            return true;
        }
        IDalCommManager commManager = FinancialApplication.getDal().getCommManager();
        IChannel channel = commManager.getChannel(toChannelType(commType));
        if (channel != null) {
            return channel.isEnabled();
        }
        return false;
    }
}
