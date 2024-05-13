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
package com.evp.bizlib.data.entity;

import androidx.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * acquirer table
 */
@Entity(nameInDb = "acquirer")
public class Acquirer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "acquirer_id";
    public static final String NAME_FIELD_NAME = "acquirer_name";
    public static final String SSL_TYPE_FIELD_NAME = "ssl_type";
    /**
     * id
     */
    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    private Long id;

    /**
     * name
     */
    @Property(nameInDb = NAME_FIELD_NAME)
    @Unique
    private String name;

    @NotNull
    private String nii;

    @NotNull
    private String terminalId;

    @NotNull
    private String merchantId;

    private int currBatchNo;

    private String ip;

    private int port;

    private String ipBak1;

    private short portBak1;

    private String ipBak2;

    private short portBak2;

    private int tcpTimeOut;

    private int wirelessTimeOut;

    @Property(nameInDb = SSL_TYPE_FIELD_NAME)
    private String sslType = "NO SSL";

    private boolean tleEnabled = false;
    private String tleVersion;
    private String tleNii;
    private String tleVendorId;
    private String tleAcquirerId;
    private String tleKeySetId;
    private String tleTeId;
    private String tleTePin;
    private byte[] tleCurrentTmkId;
    private byte[] tleCurrentTwkId;
    private String tleSensitiveFields;
    private String tleKmsNii;
    private String digioTmkKeyIndex;
    private String digioTwkKeyIndex;
    private String alipayTerminalId;
    private String alipayMerchantId;
    private String alipayAcquirer;
    private String wechatTerminalId;
    private String wechatMerchantId;
    private String wechatAcquirer;
    private String tag30TerminalId;
    private String tag30MerchantId;
    private String tag30BillerId;
    private String tag30MerchantName;
    private String tag30PartnerCode;
    private String tag30Ref2;
    private String qrcsTerminalId;
    private String qrcsMerchantId;
    private String qrcsPartnerCode;
    private int inquiryTimeout;
    private int inquiryRetries;
    private String apiPublicKey;
    private String apiUrl;
    private int hostTimeout;

    public Acquirer() {
    }

    public Acquirer(String name) {
        this.setName(name);
    }

    public Acquirer(Long id, String acquirerName) {
        this.setId(id);
        this.setName(acquirerName);
    }

    @Generated(hash = 1580965261)
    public Acquirer(Long id, String name, @NotNull String nii, @NotNull String terminalId,
            @NotNull String merchantId, int currBatchNo, String ip, int port, String ipBak1,
            short portBak1, String ipBak2, short portBak2, int tcpTimeOut,
            int wirelessTimeOut, String sslType, boolean tleEnabled, String tleVersion,
            String tleNii, String tleVendorId, String tleAcquirerId, String tleKeySetId,
            String tleTeId, String tleTePin, byte[] tleCurrentTmkId, byte[] tleCurrentTwkId,
            String tleSensitiveFields, String tleKmsNii, String digioTmkKeyIndex,
            String digioTwkKeyIndex, String alipayTerminalId, String alipayMerchantId,
            String alipayAcquirer, String wechatTerminalId, String wechatMerchantId,
            String wechatAcquirer, String tag30TerminalId, String tag30MerchantId,
            String tag30BillerId, String tag30MerchantName, String tag30PartnerCode,
            String tag30Ref2, String qrcsTerminalId, String qrcsMerchantId,
            String qrcsPartnerCode, int inquiryTimeout, int inquiryRetries,
            String apiPublicKey, String apiUrl, int hostTimeout) {
        this.id = id;
        this.name = name;
        this.nii = nii;
        this.terminalId = terminalId;
        this.merchantId = merchantId;
        this.currBatchNo = currBatchNo;
        this.ip = ip;
        this.port = port;
        this.ipBak1 = ipBak1;
        this.portBak1 = portBak1;
        this.ipBak2 = ipBak2;
        this.portBak2 = portBak2;
        this.tcpTimeOut = tcpTimeOut;
        this.wirelessTimeOut = wirelessTimeOut;
        this.sslType = sslType;
        this.tleEnabled = tleEnabled;
        this.tleVersion = tleVersion;
        this.tleNii = tleNii;
        this.tleVendorId = tleVendorId;
        this.tleAcquirerId = tleAcquirerId;
        this.tleKeySetId = tleKeySetId;
        this.tleTeId = tleTeId;
        this.tleTePin = tleTePin;
        this.tleCurrentTmkId = tleCurrentTmkId;
        this.tleCurrentTwkId = tleCurrentTwkId;
        this.tleSensitiveFields = tleSensitiveFields;
        this.tleKmsNii = tleKmsNii;
        this.digioTmkKeyIndex = digioTmkKeyIndex;
        this.digioTwkKeyIndex = digioTwkKeyIndex;
        this.alipayTerminalId = alipayTerminalId;
        this.alipayMerchantId = alipayMerchantId;
        this.alipayAcquirer = alipayAcquirer;
        this.wechatTerminalId = wechatTerminalId;
        this.wechatMerchantId = wechatMerchantId;
        this.wechatAcquirer = wechatAcquirer;
        this.tag30TerminalId = tag30TerminalId;
        this.tag30MerchantId = tag30MerchantId;
        this.tag30BillerId = tag30BillerId;
        this.tag30MerchantName = tag30MerchantName;
        this.tag30PartnerCode = tag30PartnerCode;
        this.tag30Ref2 = tag30Ref2;
        this.qrcsTerminalId = qrcsTerminalId;
        this.qrcsMerchantId = qrcsMerchantId;
        this.qrcsPartnerCode = qrcsPartnerCode;
        this.inquiryTimeout = inquiryTimeout;
        this.inquiryRetries = inquiryRetries;
        this.apiPublicKey = apiPublicKey;
        this.apiUrl = apiUrl;
        this.hostTimeout = hostTimeout;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getCurrBatchNo() {
        return currBatchNo;
    }

    public void setCurrBatchNo(int currBatchNo) {
        this.currBatchNo = currBatchNo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpBak1() {
        return ipBak1;
    }

    public void setIpBak1(String ipBak1) {
        this.ipBak1 = ipBak1;
    }

    public short getPortBak1() {
        return portBak1;
    }

    public void setPortBak1(short portBak1) {
        this.portBak1 = portBak1;
    }

    public String getIpBak2() {
        return ipBak2;
    }

    public void setIpBak2(String ipBak2) {
        this.ipBak2 = ipBak2;
    }

    public short getPortBak2() {
        return portBak2;
    }

    public void setPortBak2(short portBak2) {
        this.portBak2 = portBak2;
    }

    public int getTcpTimeOut() {
        return tcpTimeOut;
    }

    public void setTcpTimeOut(int tcpTimeOut) {
        this.tcpTimeOut = tcpTimeOut;
    }

    public int getWirelessTimeOut() {
        return wirelessTimeOut;
    }

    public void setWirelessTimeOut(int wirelessTimeOut) {
        this.wirelessTimeOut = wirelessTimeOut;
    }

    public String getSslType() {
        return sslType;
    }

    public void setSslType(String sslType) {
        this.sslType = sslType;
    }

    /**
     * copy constructor
     * update data with specified acquirer
     *
     * @param acquirer the specified acquirer to be copied
     * @return true if update successfully or false otherwise
     */
    public boolean update(@NonNull Acquirer acquirer) {
        if (null == acquirer) {
            return false;
        }
        nii = acquirer.getNii();
        merchantId = acquirer.getMerchantId();
        terminalId = acquirer.getTerminalId();
        currBatchNo = acquirer.getCurrBatchNo();
        sslType = acquirer.getSslType();
        ip = acquirer.getIp();
        port = acquirer.getPort();
        tcpTimeOut = acquirer.getTcpTimeOut();
        wirelessTimeOut = acquirer.getWirelessTimeOut();

        //optional data
        String ipBak = acquirer.getIpBak1();
        if (ipBak != null && !ipBak.isEmpty()) {
            ipBak1 = ipBak;
        }
        ipBak = acquirer.getIpBak2();
        if (ipBak != null && !ipBak.isEmpty()) {
            ipBak2 = ipBak;
        }
        short portBak = acquirer.getPortBak1();
        if (portBak != 0) {
            portBak1 = portBak;
        }
        portBak = acquirer.getPortBak2();
        if (portBak != 0) {
            portBak2 = portBak;
        }
        return true;
    }

    public String getTleVersion() {
        return this.tleVersion;
    }

    public void setTleVersion(String tleVersion) {
        this.tleVersion = tleVersion;
    }

    public String getTleNii() {
        return this.tleNii;
    }

    public void setTleNii(String tleNii) {
        this.tleNii = tleNii;
    }

    public String getTleVendorId() {
        return this.tleVendorId;
    }

    public void setTleVendorId(String tleVendorId) {
        this.tleVendorId = tleVendorId;
    }

    public boolean getTleEnabled() {
        return this.tleEnabled;
    }

    public void setTleEnabled(boolean tleEnabled) {
        this.tleEnabled = tleEnabled;
    }

    public String getTleTeId() {
        return this.tleTeId;
    }

    public void setTleTeId(String tleTeId) {
        this.tleTeId = tleTeId;
    }

    public String getTleTePin() {
        return this.tleTePin;
    }

    public void setTleTePin(String tleTePin) {
        this.tleTePin = tleTePin;
    }

    public String getTleAcquirerlId() {
        return this.tleAcquirerId;
    }

    public void setTleAcquirerlId(String tleAcquirerId) {
        this.tleAcquirerId = tleAcquirerId;
    }

    public byte[] getTleCurrentTmkId() {
        return this.tleCurrentTmkId;
    }

    public void setTleCurrentTmkId(byte[] tleCurrentTmkId) {
        this.tleCurrentTmkId = tleCurrentTmkId;
    }

    public byte[] getTleCurrentTwkId() {
        return this.tleCurrentTwkId;
    }

    public void setTleCurrentTwkId(byte[] tleCurrentTwkId) {
        this.tleCurrentTwkId = tleCurrentTwkId;
    }

    public String getTleSensitiveFields() {
        return this.tleSensitiveFields;
    }

    public void setTleSensitiveFields(String tleSensitiveFields) {
        this.tleSensitiveFields = tleSensitiveFields;
    }

    public String getTleKeySetId() {
        return this.tleKeySetId;
    }

    public void setTleKeySetId(String tleKeySetId) {
        this.tleKeySetId = tleKeySetId;
    }

    public String getTleAcquirerId() {
        return this.tleAcquirerId;
    }

    public void setTleAcquirerId(String tleAcquirerId) {
        this.tleAcquirerId = tleAcquirerId;
    }

    public String getTleKmsNii() {
        return this.tleKmsNii;
    }

    public void setTleKmsNii(String tleKmsNii) {
        this.tleKmsNii = tleKmsNii;
    }

    public String getDigioTmkKeyIndex() {
        return this.digioTmkKeyIndex;
    }

    public void setDigioTmkKeyIndex(String digioTmkKeyIndex) {
        this.digioTmkKeyIndex = digioTmkKeyIndex;
    }

    public String getDigioTwkKeyIndex() {
        return this.digioTwkKeyIndex;
    }

    public void setDigioDekKeyIndex(String digioTwkKeyIndex) {
        this.digioTwkKeyIndex = digioTwkKeyIndex;
    }

    public void setDigioTwkKeyIndex(String digioTwkKeyIndex) {
        this.digioTwkKeyIndex = digioTwkKeyIndex;
    }

    public String getQrcsTerminalId() {
        return this.qrcsTerminalId;
    }

    public void setQrcsTerminalId(String qrcsTerminalId) {
        this.qrcsTerminalId = qrcsTerminalId;
    }

    public String getQrcsMerchantId() {
        return this.qrcsMerchantId;
    }

    public void setQrcsMerchantId(String qrcsMerchantId) {
        this.qrcsMerchantId = qrcsMerchantId;
    }

    public String getQrcsPartnerCode() {
        return this.qrcsPartnerCode;
    }

    public void setQrcsPartnerCode(String qrcsPartnerCode) {
        this.qrcsPartnerCode = qrcsPartnerCode;
    }

    public String getTag30TerminalId() {
        return this.tag30TerminalId;
    }

    public void setTag30TerminalId(String tag30TerminalId) {
        this.tag30TerminalId = tag30TerminalId;
    }

    public String getTag30MerchantId() {
        return this.tag30MerchantId;
    }

    public void setTag30MerchantId(String tag30MerchantId) {
        this.tag30MerchantId = tag30MerchantId;
    }

    public String getTag30BillerId() {
        return this.tag30BillerId;
    }

    public void setTag30BillerId(String tag30BillerId) {
        this.tag30BillerId = tag30BillerId;
    }

    public String getAlipayTerminalId() {
        return this.alipayTerminalId;
    }

    public void setAlipayTerminalId(String alipayTerminalId) {
        this.alipayTerminalId = alipayTerminalId;
    }

    public String getAlipayMerchantId() {
        return this.alipayMerchantId;
    }

    public void setAlipayMerchantId(String alipayMerchantId) {
        this.alipayMerchantId = alipayMerchantId;
    }

    public String getWechatTerminalId() {
        return this.wechatTerminalId;
    }

    public void setWechatTerminalId(String wechatTerminalId) {
        this.wechatTerminalId = wechatTerminalId;
    }

    public String getWechatMerchantId() {
        return this.wechatMerchantId;
    }

    public void setWechatMerchantId(String wechatMerchantId) {
        this.wechatMerchantId = wechatMerchantId;
    }

    public int getInquiryTimeout() {
        return this.inquiryTimeout;
    }

    public void setInquiryTimeout(int inquiryTimeout) {
        this.inquiryTimeout = inquiryTimeout;
    }

    public int getInquiryRetries() {
        return this.inquiryRetries;
    }

    public void setInquiryRetries(int inquiryRetries) {
        this.inquiryRetries = inquiryRetries;
    }

    public String getTag30MerchantName() {
        return this.tag30MerchantName;
    }

    public void setTag30MerchantName(String tag30MerchantName) {
        this.tag30MerchantName = tag30MerchantName;
    }

    public String getTag30PartnerCode() {
        return this.tag30PartnerCode;
    }

    public void setTag30PartnerCode(String tag30PartnerCode) {
        this.tag30PartnerCode = tag30PartnerCode;
    }

    public String getAlipayAcquirer() {
        return this.alipayAcquirer;
    }

    public void setAlipayAcquirer(String alipayAcquirer) {
        this.alipayAcquirer = alipayAcquirer;
    }

    public String getWechatAcquirer() {
        return this.wechatAcquirer;
    }

    public void setWechatAcquirer(String wechatAcquirer) {
        this.wechatAcquirer = wechatAcquirer;
    }

    public String getTag30Ref2() {
        return this.tag30Ref2;
    }

    public void setTag30Ref2(String tag30Ref2) {
        this.tag30Ref2 = tag30Ref2;
    }

    public String getApiPublicKey() {
        return this.apiPublicKey;
    }

    public void setApiPublicKey(String apiPublicKey) {
        this.apiPublicKey = apiPublicKey;
    }

    public String getApiUrl() {
        return this.apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int getHostTimeout() {
        return this.hostTimeout;
    }

    public void setHostTimeout(int hostTimeout) {
        this.hostTimeout = hostTimeout;
    }
}
