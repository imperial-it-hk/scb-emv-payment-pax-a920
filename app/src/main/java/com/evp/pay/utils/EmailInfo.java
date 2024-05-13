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
package com.evp.pay.utils;

import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

/**
 * The type Email info.
 */
public class EmailInfo {
    //host name
    private String hostName;
    //host port
    private int port;
    //user name
    private String userName;
    //password
    private String password;
    //is SSL
    private boolean isSsl;
    //SSL port
    private int sslPort;
    //email from address
    private String from;

    private EmailInfo() {

    }

    /**
     * Generate smtp info email info.
     *
     * @return the email info
     */
    public static EmailInfo generateSmtpInfo() {
        SysParam sysParam = SysParam.getInstance();

        EmailInfo info = new EmailInfo();
        info.setHostName(sysParam.getString(R.string.EDC_SMTP_HOST));
        info.setPort(sysParam.getInt(R.string.EDC_SMTP_PORT));
        info.setUserName(sysParam.getString(R.string.EDC_SMTP_USERNAME));
        info.setPassword(sysParam.getString(R.string.EDC_SMTP_PASSWORD));
        info.setSsl(sysParam.getBoolean(R.string.EDC_SMTP_ENABLE_SSL));
        info.setSslPort(sysParam.getInt(R.string.EDC_SMTP_SSL_PORT));
        info.setFrom(sysParam.getString(R.string.EDC_SMTP_FROM));

        return info;
    }

    /**
     * get host name
     *
     * @return a string value
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * set host name
     *
     * @param hostName hostName
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * get port
     *
     * @return a int value
     */
    public int getPort() {
        return port;
    }

    /**
     * set port
     *
     * @param port port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * get user name
     *
     * @return a string value
     */
    public String getUserName() {
        return userName;
    }

    /**
     * set user name
     *
     * @param userName userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * get password
     *
     * @return password password
     */
    public String getPassword() {
        return password;
    }

    /**
     * set password
     *
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * check is SSL
     *
     * @return a boolean value
     */
    public boolean isSsl() {
        return isSsl;
    }

    /**
     * set ssl
     *
     * @param ssl ssl
     */
    public void setSsl(boolean ssl) {
        isSsl = ssl;
    }

    /**
     * get ssl port
     *
     * @return a int value
     */
    public int getSslPort() {
        return sslPort;
    }

    /**
     * set ssl port
     *
     * @param sslPort sslPort
     */
    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    /**
     * get from address
     *
     * @return a string value
     */
    public String getFrom() {
        return from;
    }

    /**
     * set from address
     *
     * @param from from
     */
    public void setFrom(String from) {
        this.from = from;
    }
}
