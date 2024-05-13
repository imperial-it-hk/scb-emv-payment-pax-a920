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
package com.evp.bizlib.data.entity;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.evp.bizlib.data.local.db.dao.AcquirerDao;
import com.evp.bizlib.data.local.db.dao.DaoSession;
import com.evp.bizlib.data.local.db.dao.IssuerDao;
import com.evp.bizlib.data.local.db.dao.TransDataDao;
import com.evp.bizlib.data.local.db.helper.DaoManager;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.Serializable;
import java.security.KeyPair;
import java.text.NumberFormat;
import java.util.Locale;

@Entity(nameInDb = "trans_data")
public class TransData implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "id";
    public static final String TRACENO_FIELD_NAME = "trace_no";
    public static final String STANNO_FIELD_NAME = "stan_no";
    public static final String BATCHNO_FIELD_NAME = "batch_no";
    public static final String TYPE_FIELD_NAME = "type";
    public static final String STATE_FIELD_NAME = "state";
    public static final String OFFLINE_STATE_FIELD_NAME = "offline_state";
    public static final String AMOUNT_FIELD_NAME = "amount";
    public static final String REVERSAL_FIELD_NAME = "REVERSAL";
    public static final String SIGN_PATH = "sign_path";
    public static final String RETRIEVAL_REF_NO = "retrieval_ref_no";
    public static final String TRX_DATE_TIME = "transaction_date_time";

    public enum ETransStatus {
        NORMAL,
        VOIDED,
        ADJUSTED,
        REFUNDED,
        SUSPENDED,
        PENDING
    }

    public enum OfflineStatus {
        OFFLINE_NOT_SENT(0),
        OFFLINE_SENT(1),
        OFFLINE_ERR_SEND(2),
        OFFLINE_ERR_RESP(3),
        OFFLINE_ERR_UNKNOWN(0xff),
        ;

        private final int value;

        OfflineStatus(int value) {
            this.value = value;
        }
    }

    public enum SignSendStatus {
        SEND_SIG_NO,
        SEND_SIG_SUCC,
        SEND_SIG_ERR,
    }

    //Card entry mode
    public enum EnterMode {
        MANUAL("M", "keyin"),
        SWIPE("S", "magnetic"),
        INSERT("I", "contact"),
        FALLBACK("F", "fallback"),
        CLSS("C", "contactless"),
        QR("Q", "qr"),
        ;

        private String str;

        private String mediaType;

        private EnterMode(String str, String mediaType) {
            this.str = str;
            this.mediaType = mediaType;
        }

        @Override
        public String toString() {
            return str;
        }

        public String mediaType() { return mediaType; }
    }

    //Status of reversal
    public enum ReversalStatus {
        NORMAL,
        PENDING,
        REVERSAL,
    }

    //Reversal reason
    public static final String DUP_REASON_NO_RECV = "98";
    public static final String DUP_REASON_MACWRONG = "A0";
    public static final String DUP_REASON_OTHERS = "06";

    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    protected Long id;

    @Property(nameInDb = TRACENO_FIELD_NAME)
    @Unique
    @NotNull
    protected long traceNo;

    @Property(nameInDb = STANNO_FIELD_NAME)
    @Unique
    @NotNull
    protected long stanNo;

    @Property(nameInDb = TYPE_FIELD_NAME)
    @NotNull
    protected String transType;

    @Property(nameInDb = STATE_FIELD_NAME)
    @NotNull
    @Convert(converter = ETransStatusConverter.class, columnType = String.class)
    protected ETransStatus transState = ETransStatus.NORMAL;

    @Property(nameInDb = OFFLINE_STATE_FIELD_NAME)
    @Convert(converter = OfflineStatusConverter.class, columnType = String.class)
    protected OfflineStatus offlineSendState = null;

    @Property(nameInDb = AMOUNT_FIELD_NAME)
    protected String amount;

    @Property(nameInDb = BATCHNO_FIELD_NAME)
    @NotNull
    protected long batchNo;

    @Property(nameInDb = TRX_DATE_TIME)
    protected String dateTime;

    @Property(nameInDb = RETRIEVAL_REF_NO)
    protected String refNo;

    @Property(nameInDb = SIGN_PATH)
    private byte[] signPath;

    private long issuer_id;
    @ToOne(joinProperty = "issuer_id")
    protected Issuer issuer;

    private long acquirer_id;
    @ToOne(joinProperty = "acquirer_id")
    protected Acquirer acquirer;

    @Property(nameInDb = REVERSAL_FIELD_NAME)
    @NotNull
    @Convert(converter = ReversalStatusConverter.class, columnType = String.class)
    protected ReversalStatus reversalStatus = ReversalStatus.NORMAL;

    protected long origTransNo;
    protected long origStanNo;
    protected String origTransType = null;
    @NotNull
    protected boolean isUpload = false;
    @NotNull
    protected int sendTimes;
    protected String procCode;
    protected String tipAmount;
    @Convert(converter = LocaleConverter.class, columnType = String.class)
    protected Locale currency;
    protected long origBatchNo;
    protected String pan;
    protected String origDateTime;
    protected String settleDateTime;
    protected String expDate;
    @Convert(converter = EnterModeConverter.class, columnType = String.class)
    protected EnterMode enterMode;
    protected String nii;
    protected String origRefNo;
    protected String authCode;
    protected String origAuthCode;
    protected String issuerCode;
    protected String acqCode;
    @NotNull
    protected boolean hasPin;
    protected String track1;
    protected String track2;
    protected String track3;
    protected String dupReason;
    protected String reserved;
    @NotNull
    protected boolean pinFree = false;
    @NotNull
    protected boolean signFree = false;
    @NotNull
    protected boolean isCDCVM = false;
    @NotNull
    protected boolean isOnlineTrans = false;
    protected byte[] signData;
    protected String emvResult = null;
    protected String cardSerialNo;
    protected String sendIccData;
    protected String dupIccData;
    protected String tc;
    protected String arqc;
    protected String arpc;
    protected String tvr;
    protected String aid;
    protected String emvAppLabel;
    protected String emvAppName;
    protected String tsi;
    protected String atc;
    private String phoneNum;
    private String email;
    protected String cardholderName;
    protected String fundingSource;
    protected String dccForeignAmount;
    protected String dccExchangeRate;
    protected String dccCurrencyCode;
    protected String qrCode;
    protected String billPaymentRef1;
    protected String billPaymentRef2;
    protected String billPaymentRef3;
    protected String tmkKeyIndex;
    protected String dekKeyIndex;
    protected boolean isBSC;
    protected String amountCNY;
    protected String exchangeRate;
    protected String paymentId;
    protected String paymentChannel;
    protected String bankCode;
    protected int paymentPlan;
    protected String paymentTerm;
    protected String productCode;
    protected String productSN;
    protected String redeemQty;
    protected String redeemType;
    protected String redeemAmt;
    protected String redeemPts;
    protected String clubPoolId;
    protected byte[] origField63;
    protected String billerId;
    protected String consumerPan;
    protected String merchantPan;
    protected String transactionId;
    protected String qrCodeId;
    protected String currencyCode;
    protected String sendingBankCode;
    protected String payeeProxyId;
    protected String payeeProxyType;
    protected String payeeAccountNumber;
    protected String payerProxyId;
    protected String payerProxyType;
    protected String payerAccountNumber;
    protected String receivingBankCode;
    protected String thaiQRTag;
    protected boolean isPullSlip;
    protected String qrcsTraceNo;
    protected String saleType;
    protected byte[] field63;

    @Transient
    private String pin;
    @Transient
    protected String responseCode;
    @Transient
    protected String header;
    @Transient
    protected String tpdu;
    @Transient
    protected String field48;
    @Transient
    protected String field60;
    @Transient
    protected byte[] field62;
    @Transient
    protected byte[] recvIccData;
    @Transient
    protected String field3;
    @Transient
    protected KeyPair rsaKeyPair;
    @Transient
    protected byte[] tmkKey;
    @Transient
    protected byte[] makKey;
    @Transient
    protected byte[] dekKey;
    @Transient
    protected byte[] pinKey;
    @Transient
    protected byte[] field64;
    @Transient
    protected String tmkKcv;
    @Transient
    protected String tdkKcv;
    @Transient
    protected String takKcv;
    @Transient
    protected String tpkKcv;
    @Transient
    protected String dekKcv;
    @Transient
    protected String qrTransStatus;
    @Transient
    protected String qrBuyerUserId;
    @Transient
    protected String qrBuyerLoginId;
    @Transient
    protected String qrRefTransId;
    @Transient
    protected String qrSlipNo;
    @Transient
    protected String qrTrxDateTime;
    @Transient
    protected String qrPaymentMethod;
    @Transient
    protected String qrTransactionType;
    @Transient
    protected String qrRefReference;
    @Transient
    protected String qrRefTrxId;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1906158038)
    private transient TransDataDao myDao;

    @Generated(hash = 1724583466)
    private transient Long issuer__resolvedKey;

    @Generated(hash = 86676445)
    private transient Long acquirer__resolvedKey;


    public TransData() {

    }

    //copy constructor, replace clone
    public TransData(TransData other) {
        this.id = other.id;
        this.traceNo = other.traceNo;
        this.stanNo = other.stanNo;
        this.origTransNo = other.origTransNo;
        this.transType = other.transType;
        this.origTransType = other.origTransType;
        this.transState = other.transState;
        this.isUpload = other.isUpload;
        this.offlineSendState = other.offlineSendState;
        this.sendTimes = other.sendTimes;
        this.procCode = other.procCode;
        this.amount = other.amount;
        this.tipAmount = other.tipAmount;
        this.batchNo = other.batchNo;
        this.origBatchNo = other.origBatchNo;
        this.pan = other.pan;
        this.dateTime = other.dateTime;
        this.origDateTime = other.origDateTime;
        this.settleDateTime = other.settleDateTime;
        this.expDate = other.expDate;
        this.nii = other.nii;
        this.refNo = other.refNo;
        this.origRefNo = other.origRefNo;
        this.authCode = other.authCode;
        this.origAuthCode = other.origAuthCode;
        this.issuerCode = other.issuerCode;
        this.acqCode = other.acqCode;
        this.hasPin = other.hasPin;
        this.track1 = other.track1;
        this.track2 = other.track2;
        this.track3 = other.track3;
        this.dupReason = other.dupReason;
        this.reserved = other.reserved;
        this.pinFree = other.pinFree;
        this.signFree = other.signFree;
        this.isCDCVM = other.isCDCVM;
        this.isOnlineTrans = other.isOnlineTrans;
        this.signData = other.signData;
        this.signPath = other.signPath;
        this.cardSerialNo = other.cardSerialNo;
        this.sendIccData = other.sendIccData;
        this.dupIccData = other.dupIccData;
        this.tc = other.tc;
        this.arqc = other.arqc;
        this.arpc = other.arpc;
        this.tvr = other.tvr;
        this.aid = other.aid;
        this.emvAppLabel = other.emvAppLabel;
        this.emvAppName = other.emvAppName;
        this.tsi = other.tsi;
        this.atc = other.atc;
        this.reversalStatus = other.reversalStatus;
        this.phoneNum = other.phoneNum;
        this.email = other.email;
        this.pin = other.pin;
        this.header = other.header;
        this.tpdu = other.tpdu;
        this.field48 = other.field48;
        this.field60 = other.field60;
        this.field62 = other.field62;
        this.field63 = other.field63;
        this.recvIccData = other.recvIccData;
        this.field3 = other.field3;
        this.rsaKeyPair = other.rsaKeyPair;
        this.tmkKey = other.tmkKey;
        this.makKey = other.makKey;
        this.dekKey = other.dekKey;
        this.field64 = other.field64;
        this.emvResult = other.emvResult;
        this.enterMode = other.enterMode;
        this.issuer = other.issuer;
        this.paymentPlan = other.paymentPlan;
        this.paymentTerm = other.paymentTerm;
        this.productCode = other.productCode;
        this.productSN = other.productSN;
        this.redeemQty = other.redeemQty;
        this.redeemType = other.redeemType;
        this.redeemAmt = other.redeemAmt;
        this.redeemPts = other.redeemPts;
        this.clubPoolId = other.clubPoolId;
        this.origField63 = other.origField63;
    }

    @Generated(hash = 1728368166)
    public TransData(Long id, long traceNo, long stanNo, @NotNull String transType, @NotNull ETransStatus transState, OfflineStatus offlineSendState,
            String amount, long batchNo, String dateTime, String refNo, byte[] signPath, long issuer_id, long acquirer_id,
            @NotNull ReversalStatus reversalStatus, long origTransNo, long origStanNo, String origTransType, boolean isUpload, int sendTimes,
            String procCode, String tipAmount, Locale currency, long origBatchNo, String pan, String origDateTime, String settleDateTime, String expDate,
            EnterMode enterMode, String nii, String origRefNo, String authCode, String origAuthCode, String issuerCode, String acqCode, boolean hasPin,
            String track1, String track2, String track3, String dupReason, String reserved, boolean pinFree, boolean signFree, boolean isCDCVM,
            boolean isOnlineTrans, byte[] signData, String emvResult, String cardSerialNo, String sendIccData, String dupIccData, String tc, String arqc,
            String arpc, String tvr, String aid, String emvAppLabel, String emvAppName, String tsi, String atc, String phoneNum, String email,
            String cardholderName, String fundingSource, String dccForeignAmount, String dccExchangeRate, String dccCurrencyCode, String qrCode,
            String billPaymentRef1, String billPaymentRef2, String billPaymentRef3, String tmkKeyIndex, String dekKeyIndex, boolean isBSC,
            String amountCNY, String exchangeRate, String paymentId, String paymentChannel, String bankCode, int paymentPlan, String paymentTerm,
            String productCode, String productSN, String redeemQty, String redeemType, String redeemAmt, String redeemPts, String clubPoolId,
            byte[] origField63, String billerId, String consumerPan, String merchantPan, String transactionId, String qrCodeId, String currencyCode,
            String sendingBankCode, String payeeProxyId, String payeeProxyType, String payeeAccountNumber, String payerProxyId, String payerProxyType,
            String payerAccountNumber, String receivingBankCode, String thaiQRTag, boolean isPullSlip, String qrcsTraceNo, String saleType,
            byte[] field63) {
        this.id = id;
        this.traceNo = traceNo;
        this.stanNo = stanNo;
        this.transType = transType;
        this.transState = transState;
        this.offlineSendState = offlineSendState;
        this.amount = amount;
        this.batchNo = batchNo;
        this.dateTime = dateTime;
        this.refNo = refNo;
        this.signPath = signPath;
        this.issuer_id = issuer_id;
        this.acquirer_id = acquirer_id;
        this.reversalStatus = reversalStatus;
        this.origTransNo = origTransNo;
        this.origStanNo = origStanNo;
        this.origTransType = origTransType;
        this.isUpload = isUpload;
        this.sendTimes = sendTimes;
        this.procCode = procCode;
        this.tipAmount = tipAmount;
        this.currency = currency;
        this.origBatchNo = origBatchNo;
        this.pan = pan;
        this.origDateTime = origDateTime;
        this.settleDateTime = settleDateTime;
        this.expDate = expDate;
        this.enterMode = enterMode;
        this.nii = nii;
        this.origRefNo = origRefNo;
        this.authCode = authCode;
        this.origAuthCode = origAuthCode;
        this.issuerCode = issuerCode;
        this.acqCode = acqCode;
        this.hasPin = hasPin;
        this.track1 = track1;
        this.track2 = track2;
        this.track3 = track3;
        this.dupReason = dupReason;
        this.reserved = reserved;
        this.pinFree = pinFree;
        this.signFree = signFree;
        this.isCDCVM = isCDCVM;
        this.isOnlineTrans = isOnlineTrans;
        this.signData = signData;
        this.emvResult = emvResult;
        this.cardSerialNo = cardSerialNo;
        this.sendIccData = sendIccData;
        this.dupIccData = dupIccData;
        this.tc = tc;
        this.arqc = arqc;
        this.arpc = arpc;
        this.tvr = tvr;
        this.aid = aid;
        this.emvAppLabel = emvAppLabel;
        this.emvAppName = emvAppName;
        this.tsi = tsi;
        this.atc = atc;
        this.phoneNum = phoneNum;
        this.email = email;
        this.cardholderName = cardholderName;
        this.fundingSource = fundingSource;
        this.dccForeignAmount = dccForeignAmount;
        this.dccExchangeRate = dccExchangeRate;
        this.dccCurrencyCode = dccCurrencyCode;
        this.qrCode = qrCode;
        this.billPaymentRef1 = billPaymentRef1;
        this.billPaymentRef2 = billPaymentRef2;
        this.billPaymentRef3 = billPaymentRef3;
        this.tmkKeyIndex = tmkKeyIndex;
        this.dekKeyIndex = dekKeyIndex;
        this.isBSC = isBSC;
        this.amountCNY = amountCNY;
        this.exchangeRate = exchangeRate;
        this.paymentId = paymentId;
        this.paymentChannel = paymentChannel;
        this.bankCode = bankCode;
        this.paymentPlan = paymentPlan;
        this.paymentTerm = paymentTerm;
        this.productCode = productCode;
        this.productSN = productSN;
        this.redeemQty = redeemQty;
        this.redeemType = redeemType;
        this.redeemAmt = redeemAmt;
        this.redeemPts = redeemPts;
        this.clubPoolId = clubPoolId;
        this.origField63 = origField63;
        this.billerId = billerId;
        this.consumerPan = consumerPan;
        this.merchantPan = merchantPan;
        this.transactionId = transactionId;
        this.qrCodeId = qrCodeId;
        this.currencyCode = currencyCode;
        this.sendingBankCode = sendingBankCode;
        this.payeeProxyId = payeeProxyId;
        this.payeeProxyType = payeeProxyType;
        this.payeeAccountNumber = payeeAccountNumber;
        this.payerProxyId = payerProxyId;
        this.payerProxyType = payerProxyType;
        this.payerAccountNumber = payerAccountNumber;
        this.receivingBankCode = receivingBankCode;
        this.thaiQRTag = thaiQRTag;
        this.isPullSlip = isPullSlip;
        this.qrcsTraceNo = qrcsTraceNo;
        this.saleType = saleType;
        this.field63 = field63;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public boolean isHasPin() {
        return hasPin;
    }

    public boolean isPinFree() {
        return pinFree;
    }

    public boolean isSignFree() {
        return signFree;
    }

    public boolean isCDCVM() {
        return isCDCVM;
    }

    public void setCDCVM(boolean CDCVM) {
        isCDCVM = CDCVM;
    }

    public boolean isOnlineTrans() {
        return isOnlineTrans;
    }

    public void setOnlineTrans(boolean onlineTrans) {
        isOnlineTrans = onlineTrans;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getField48() {
        return field48;
    }

    public void setField48(String field48) {
        this.field48 = field48;
    }

    public String getField60() {
        return field60;
    }

    public void setField60(String field60) {
        this.field60 = field60;
    }

    public byte[] getField62() {
        return field62;
    }

    public void setField62(byte[] field62) {
        this.field62 = field62;
    }

    public byte[] getField63() {
        return field63;
    }

    public void setField63(byte[] field63) {
        this.field63 = field63;
    }

    public byte[] getRecvIccData() {
        return recvIccData;
    }

    public void setRecvIccData(byte[] recvIccData) {
        this.recvIccData = recvIccData;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTraceNo() {
        return this.traceNo;
    }

    public void setTraceNo(long traceNo) {
        this.traceNo = traceNo;
    }

    public long getOrigTransNo() {
        return this.origTransNo;
    }

    public void setOrigTransNo(long origTransNo) {
        this.origTransNo = origTransNo;
    }

    public String getTransType() {
        return this.transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getOrigTransType() {
        return this.origTransType;
    }

    public void setOrigTransType(String origTransType) {
        this.origTransType = origTransType;
    }

    public ETransStatus getTransState() {
        return this.transState;
    }

    public void setTransState(ETransStatus transState) {
        this.transState = transState;
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public OfflineStatus getOfflineSendState() {
        return this.offlineSendState;
    }

    public void setOfflineSendState(OfflineStatus offlineSendState) {
        this.offlineSendState = offlineSendState;
    }

    public int getSendTimes() {
        return this.sendTimes;
    }

    public void setSendTimes(int sendTimes) {
        this.sendTimes = sendTimes;
    }

    public String getProcCode() {
        return this.procCode;
    }

    public void setProcCode(String procCode) {
        this.procCode = procCode;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTipAmount() {
        return this.tipAmount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public Locale getCurrency() {
        return this.currency;
    }

    public void setCurrency(Locale currency) {
        this.currency = currency;
    }

    public long getBatchNo() {
        return this.batchNo;
    }

    public void setBatchNo(long batchNo) {
        this.batchNo = batchNo;
    }

    public long getOrigBatchNo() {
        return this.origBatchNo;
    }

    public void setOrigBatchNo(long origBatchNo) {
        this.origBatchNo = origBatchNo;
    }

    public String getPan() {
        return this.pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getOrigDateTime() {
        return this.origDateTime;
    }

    public void setOrigDateTime(String origDateTime) {
        this.origDateTime = origDateTime;
    }

    public String getSettleDateTime() {
        return this.settleDateTime;
    }

    public void setSettleDateTime(String settleDateTime) {
        this.settleDateTime = settleDateTime;
    }

    public String getExpDate() {
        return this.expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public EnterMode getEnterMode() {
        return this.enterMode;
    }

    public void setEnterMode(EnterMode enterMode) {
        this.enterMode = enterMode;
    }

    public String getNii() {
        return this.nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getRefNo() {
        return this.refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getOrigRefNo() {
        return this.origRefNo;
    }

    public void setOrigRefNo(String origRefNo) {
        this.origRefNo = origRefNo;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getOrigAuthCode() {
        return this.origAuthCode;
    }

    public void setOrigAuthCode(String origAuthCode) {
        this.origAuthCode = origAuthCode;
    }

    public String getIssuerCode() {
        return this.issuerCode;
    }

    public void setIssuerCode(String issuerCode) {
        this.issuerCode = issuerCode;
    }

    public String getAcqCode() {
        return this.acqCode;
    }

    public void setAcqCode(String acqCode) {
        this.acqCode = acqCode;
    }

    public boolean getHasPin() {
        return this.hasPin;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
    }

    public String getTrack1() {
        return this.track1;
    }

    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    public String getTrack2() {
        return this.track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getTrack3() {
        return this.track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }

    public String getDupReason() {
        return this.dupReason;
    }

    public void setDupReason(String dupReason) {
        this.dupReason = dupReason;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public boolean getPinFree() {
        return this.pinFree;
    }

    public void setPinFree(boolean pinFree) {
        this.pinFree = pinFree;
    }

    public boolean getSignFree() {
        return this.signFree;
    }

    public void setSignFree(boolean signFree) {
        this.signFree = signFree;
    }

    public boolean getIsCDCVM() {
        return this.isCDCVM;
    }

    public void setIsCDCVM(boolean isCDCVM) {
        this.isCDCVM = isCDCVM;
    }

    public boolean getIsOnlineTrans() {
        return this.isOnlineTrans;
    }

    public void setIsOnlineTrans(boolean isOnlineTrans) {
        this.isOnlineTrans = isOnlineTrans;
    }

    public byte[] getSignData() {
        return this.signData;
    }

    public void setSignData(byte[] signData) {
        this.signData = signData;
    }

    public byte[] getSignPath() {
        return this.signPath;
    }

    public void setSignPath(byte[] signPath) {
        this.signPath = signPath;
    }

    public long getIssuer_id() {
        return this.issuer_id;
    }

    public void setIssuer_id(long issuer_id) {
        this.issuer_id = issuer_id;
    }

    public long getAcquirer_id() {
        return this.acquirer_id;
    }

    public void setAcquirer_id(long acquirer_id) {
        this.acquirer_id = acquirer_id;
    }

    public String getEmvResult() {
        return this.emvResult;
    }

    public void setEmvResult(String emvResult) {
        this.emvResult = emvResult;
    }

    public String getCardSerialNo() {
        return this.cardSerialNo;
    }

    public void setCardSerialNo(String cardSerialNo) {
        this.cardSerialNo = cardSerialNo;
    }

    public String getSendIccData() {
        return this.sendIccData;
    }

    public void setSendIccData(String sendIccData) {
        this.sendIccData = sendIccData;
    }

    public String getDupIccData() {
        return this.dupIccData;
    }

    public void setDupIccData(String dupIccData) {
        this.dupIccData = dupIccData;
    }

    public String getTc() {
        return this.tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getArqc() {
        return this.arqc;
    }

    public void setArqc(String arqc) {
        this.arqc = arqc;
    }

    public String getArpc() {
        return this.arpc;
    }

    public void setArpc(String arpc) {
        this.arpc = arpc;
    }

    public String getTvr() {
        return this.tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getEmvAppLabel() {
        return this.emvAppLabel;
    }

    public void setEmvAppLabel(String emvAppLabel) {
        this.emvAppLabel = emvAppLabel;
    }

    public String getEmvAppName() {
        return this.emvAppName;
    }

    public void setEmvAppName(String emvAppName) {
        this.emvAppName = emvAppName;
    }

    public String getTsi() {
        return this.tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getAtc() {
        return this.atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public ReversalStatus getReversalStatus() {
        return this.reversalStatus;
    }

    public void setReversalStatus(ReversalStatus reversalStatus) {
        this.reversalStatus = reversalStatus;
    }

    public String getPhoneNum() {
        return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFundingSource() {
        return this.fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1331604593)
    public void setIssuer(@NotNull Issuer issuer) {
        if (issuer == null) {
            throw new DaoException(
                    "To-one property 'issuer_id' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.issuer = issuer;
            issuer_id = issuer.getId();
            issuer__resolvedKey = issuer_id;
        }
    }


    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 346746627)
    public void setAcquirer(@NotNull Acquirer acquirer) {
        if (acquirer == null) {
            throw new DaoException(
                    "To-one property 'acquirer_id' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.acquirer = acquirer;
            acquirer_id = acquirer.getId();
            acquirer__resolvedKey = acquirer_id;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    @Keep
    public Issuer getIssuer() {
        long __key = this.issuer_id;
        if (issuer__resolvedKey == null || !issuer__resolvedKey.equals(__key)) {
            DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                daoSession = DaoManager.getInstance().getDaoSession();
            }
            IssuerDao targetDao = daoSession.getIssuerDao();
            Issuer issuerNew = targetDao.load(__key);
            synchronized (this) {
                issuer = issuerNew;
                issuer__resolvedKey = __key;
            }
        }
        return issuer;
    }


    @Keep
    public Acquirer getAcquirer() {
        long __key = this.acquirer_id;
        if (acquirer__resolvedKey == null || !acquirer__resolvedKey.equals(__key)) {
            DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                daoSession = DaoManager.getInstance().getDaoSession();
            }
            AcquirerDao targetDao = daoSession.getAcquirerDao();
            Acquirer acquirerNew = targetDao.load(__key);
            synchronized (this) {
                acquirer = acquirerNew;
                acquirer__resolvedKey = __key;
            }
        }
        return acquirer;
    }


    public static class ETransStatusConverter implements PropertyConverter<ETransStatus, String> {

        @Override
        public ETransStatus convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (ETransStatus item : ETransStatus.values()) {
                if (item.toString().equals(databaseValue)) {
                    return item;
                }
            }
            return ETransStatus.NORMAL;
        }

        @Override
        public String convertToDatabaseValue(ETransStatus entityProperty) {
            return entityProperty == null ? null : entityProperty.toString();
        }
    }

    public static class OfflineStatusConverter implements PropertyConverter<OfflineStatus, String> {

        @Override
        public OfflineStatus convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (OfflineStatus item : OfflineStatus.values()) {
                if (item.name().equals(databaseValue)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public String convertToDatabaseValue(OfflineStatus entityProperty) {
            return entityProperty == null ? null : entityProperty.name();
        }
    }

    public static class LocaleConverter implements PropertyConverter<Locale, String> {

        @Override
        public Locale convertToEntityProperty(String databaseValue) {
            if (databaseValue == null || databaseValue.isEmpty()) {
                return null;
            }
            return JSON.parseObject(databaseValue, Locale.class);
        }

        @Override
        public String convertToDatabaseValue(Locale entityProperty) {
            if (entityProperty == null) {
                return "";
            }
            return JSON.toJSONString(entityProperty);
        }
    }

    public static class EnterModeConverter implements PropertyConverter<EnterMode, String> {

        @Override
        public EnterMode convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (EnterMode item : EnterMode.values()) {
                if (item.toString().equals(databaseValue)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public String convertToDatabaseValue(EnterMode entityProperty) {
            return entityProperty == null ? null : entityProperty.toString();
        }
    }


    public static class ReversalStatusConverter implements PropertyConverter<ReversalStatus, String> {

        @Override
        public ReversalStatus convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (ReversalStatus item : ReversalStatus.values()) {
                if (item.toString().equals(databaseValue)) {
                    return item;
                }
            }
            return ReversalStatus.NORMAL;
        }

        @Override
        public String convertToDatabaseValue(ReversalStatus entityProperty) {
            return entityProperty == null ? null : entityProperty.toString();
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public long getStanNo() {
        return this.stanNo;
    }

    public void setStanNo(long stanNo) {
        this.stanNo = stanNo;
    }

    public long getOrigStanNo() {
        return this.origStanNo;
    }

    public void setOrigStanNo(long origStanNo) {
        this.origStanNo = origStanNo;
    }

    public KeyPair getRsaKeyPair() {
        return this.rsaKeyPair;
    }

    public void setRsaKeyPair(KeyPair rsaKeyPair) {
        this.rsaKeyPair = rsaKeyPair;
    }

    public byte[] getTmkKey() {
        return this.tmkKey;
    }

    public void setTmkKey(byte[] tmkKey) {
        this.tmkKey = tmkKey;
    }

    public byte[] getMakKey() {
        return this.makKey;
    }

    public void setMakKey(byte[] makKey) {
        this.makKey = makKey;
    }

    public byte[] getDekKey() {
        return this.dekKey;
    }

    public void setDekKey(byte[] dekKey) {
        this.dekKey = dekKey;
    }

    public byte[] getPinKey() { return this.pinKey; }

    public void setPinKey(byte[] pinKey) {
        this.pinKey = pinKey;
    }

    public byte[] getField64() { return this.field64; }

    public void setField64(byte[] field64) {
        this.field64 = field64;
    }

    public String getTmkKcv() {
        return this.tmkKcv;
    }

    public void setTmkKcv(String tmkKcv) {
        this.tmkKcv = tmkKcv;
    }

    public String getTakKcv() {
        return this.takKcv;
    }

    public void setTakKcv(String takKcv) {
        this.takKcv = takKcv;
    }

    public String getTdkKcv() {
        return this.tdkKcv;
    }

    public void setTdkKcv(String tdkKcv) {
        this.tdkKcv = tdkKcv;
    }

    public String getTpkKcv() { return this.tpkKcv; }

    public void setTpkKcv(String tpkKcv) { this.tpkKcv = tpkKcv; }

    public String getDekKcv() { return this.dekKcv; }

    public void setDekKcv(String dekKcv) { this.dekKcv = dekKcv; }

    /** called by internal mechanisms, do not call yourself. */

    @Generated(hash = 718476260)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransDataDao() : null;
    }

    public String getCardholderName() {
        return this.cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName.trim();
    }

    public String getDccForeignAmount() {
        return this.dccForeignAmount;
    }

    public void setDccForeignAmount(String dccForeignAmount) {
        this.dccForeignAmount = dccForeignAmount;
    }

    public String getDccExchangeRate() {
        return this.dccExchangeRate;
    }

    public void setDccExchangeRate(String dccExchangeRate) {
        this.dccExchangeRate = dccExchangeRate;
    }

    public String getDccCurrencyCode() {
        return this.dccCurrencyCode;
    }

    public void setDccCurrencyCode(String dccCurrencyCode) {
        this.dccCurrencyCode = dccCurrencyCode;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getBillPaymentRef1() {
        return this.billPaymentRef1;
    }

    public void setBillPaymentRef1(String billPaymentRef1) {
        this.billPaymentRef1 = billPaymentRef1;
    }

    public String getBillPaymentRef2() {
        return this.billPaymentRef2;
    }

    public void setBillPaymentRef2(String billPaymentRef2) {
        this.billPaymentRef2 = billPaymentRef2;
    }

    public String getBillPaymentRef3() {
        return this.billPaymentRef3;
    }

    public void setBillPaymentRef3(String billPaymentRef3) {
        this.billPaymentRef3 = billPaymentRef3;
    }

    public String getTmkKeyIndex() {
        return this.tmkKeyIndex;
    }

    public void setTmkKeyIndex(String tmkKeyIndex) {
        this.tmkKeyIndex = tmkKeyIndex;
    }

    public String getDekKeyIndex() {
        return this.dekKeyIndex;
    }

    public void setDekKeyIndex(String dekKeyIndex) {
        this.dekKeyIndex = dekKeyIndex;
    }

    public boolean getIsBSC() {
        return this.isBSC;
    }

    public void setIsBSC(boolean isBSC) {
        this.isBSC = isBSC;
    }

    public int getPaymentPlan() {
        return this.paymentPlan;
    }

    public void setPaymentPlan(int paymentPlan) {
        this.paymentPlan = paymentPlan;
    }

    public String getPaymentTerm() {
        return this.paymentTerm;
    }

    public void setPaymentTerm(String paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public String getProductCode() {
        return this.productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductSN() {
        return this.productSN;
    }

    public void setProductSN(String productSN) {
        this.productSN = productSN;
    }

    public String getRedeemQty() {
        return this.redeemQty;
    }

    public void setRedeemQty(String redeemQty) {
        this.redeemQty = redeemQty;
    }

    public String getRedeemType() {
        return this.redeemType;
    }

    public void setRedeemType(String redeemType) {
        this.redeemType = redeemType;
    }

    public String getRedeemAmt() {
        return this.redeemAmt;
    }

    public void setRedeemAmt(String redeemAmt) {
        this.redeemAmt = redeemAmt;
    }

    public String getRedeemPts() {
        return this.redeemPts;
    }

    public void setRedeemPts(String redeemPts) {
        this.redeemPts = redeemPts;
    }

    public String getClubPoolId() {
        return this.clubPoolId;
    }

    public void setClubPoolId(String clubPoolId) {
        this.clubPoolId = clubPoolId;
    }

    public byte[] getOrigField63() {
        return this.origField63;
    }

    public void setOrigField63(byte[] origField63) {
        this.origField63 = origField63;
    }

    public String getAmountCNY() {
        return this.amountCNY;
    }

    public void setAmountCNY(String amountCNY) {
        this.amountCNY = amountCNY;
    }

    public String getExchangeRate() {
        return this.exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getPaymentId() {
        return this.paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getBillerId() {
        return this.billerId;
    }

    public void setBillerId(String billerId) {
        this.billerId = billerId;
    }

    public String getMerchantPan() {
        return this.merchantPan;
    }

    public void setMerchantPan(String merchantPan) {
        this.merchantPan = merchantPan;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getQrCodeId() {
        return this.qrCodeId;
    }

    public void setQrCodeId(String qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getPaymentChannel() {
        return this.paymentChannel;
    }

    public void setPaymentChannel(String paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public String getBankCode() {
        return this.bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getConsumerPan() {
        return this.consumerPan;
    }

    public void setConsumerPan(String consumerPan) {
        this.consumerPan = consumerPan;
    }

    public String getSendingBankCode() {
        return this.sendingBankCode;
    }

    public void setSendingBankCode(String sendingBankCode) {
        this.sendingBankCode = sendingBankCode;
    }

    public String getFormattedNetSaleAmt() {
        return CurrencyConverter.convert(ConvertUtils.parseLongSafe(this.amount, 0) + ConvertUtils.parseLongSafe(this.redeemAmt, 0), this.currency);
    }

    public String getFormattedRedeemPts() {
        return NumberFormat.getIntegerInstance().format(ConvertUtils.parseLongSafe(this.redeemPts, 0) / 100);
    }

    public String getPayeeProxyId() {
        return this.payeeProxyId;
    }

    public void setPayeeProxyId(String payeeProxyId) {
        this.payeeProxyId = payeeProxyId;
    }

    public String getPayeeProxyType() {
        return this.payeeProxyType;
    }

    public void setPayeeProxyType(String payeeProxyType) {
        this.payeeProxyType = payeeProxyType;
    }

    public String getPayeeAccountNumber() {
        return this.payeeAccountNumber;
    }

    public void setPayeeAccountNumber(String payeeAccountNumber) {
        this.payeeAccountNumber = payeeAccountNumber;
    }

    public String getPayerProxyId() {
        return this.payerProxyId;
    }

    public void setPayerProxyId(String payerProxyId) {
        this.payerProxyId = payerProxyId;
    }

    public String getPayerProxyType() {
        return this.payerProxyType;
    }

    public void setPayerProxyType(String payerProxyType) {
        this.payerProxyType = payerProxyType;
    }

    public String getPayerAccountNumber() {
        return this.payerAccountNumber;
    }

    public void setPayerAccountNumber(String payerAccountNumber) {
        this.payerAccountNumber = payerAccountNumber;
    }

    public String getReceivingBankCode() {
        return this.receivingBankCode;
    }

    public void setReceivingBankCode(String receivingBankCode) {
        this.receivingBankCode = receivingBankCode;
    }

    public String getThaiQRTag() {
        return this.thaiQRTag;
    }

    public void setThaiQRTag(String thaiQRTag) {
        this.thaiQRTag = thaiQRTag;
    }

    public boolean getIsPullSlip() {
        return this.isPullSlip;
    }

    public void setIsPullSlip(boolean isPullSlip) {
        this.isPullSlip = isPullSlip;
    }

    public String getQrcsTraceNo() {
        return this.qrcsTraceNo;
    }

    public void setQrcsTraceNo(String qrcsTraceNo) {
        this.qrcsTraceNo = qrcsTraceNo;
    }

    public String getSaleType() {
        return this.saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public String getQrTransStatus() {
        return this.qrTransStatus;
    }

    public void setQrTransStatus(String qrTransStatus) {
        this.qrTransStatus = qrTransStatus;
    }

    public String getQrBuyerUserId() {
        return this.qrBuyerUserId;
    }

    public void setQrBuyerUserId(String qrBuyerUserId) {
        this.qrBuyerUserId = qrBuyerUserId;
    }

    public String getQrBuyerLoginId() {
        return this.qrBuyerLoginId;
    }

    public void setQrBuyerLoginId(String qrBuyerLoginId) {
        this.qrBuyerLoginId = qrBuyerLoginId;
    }

    public String getQrRefTransId() {
        return this.qrRefTransId;
    }

    public void setQrRefTransId(String qrRefTransId) {
        this.qrRefTransId = qrRefTransId;
    }

    public String getQrSlipNo() {
        return this.qrSlipNo;
    }

    public void setQrSlipNo(String qrSlipNo) {
        this.qrSlipNo = qrSlipNo;
    }

    public String getQrTrxDateTime() {
        return this.qrTrxDateTime;
    }

    public void setQrTrxDateTime(String qrTrxDateTime) {
        this.qrTrxDateTime = qrTrxDateTime;
    }

    public String getQrPaymentMethod() {
        return this.qrPaymentMethod;
    }

    public void setQrPaymentMethod(String qrPaymentMethod) {
        this.qrPaymentMethod = qrPaymentMethod;
    }

    public String getQrTransactionType() {
        return this.qrTransactionType;
    }

    public void setQrTransactionType(String qrTransactionType) {
        this.qrTransactionType = qrTransactionType;
    }

    public String getQrRefReference() {
        return this.qrRefReference;
    }

    public void setQrRefReference(String qrRefReference) {
        this.qrRefReference = qrRefReference;
    }

    public String getQrRefTrxId() {
        return this.qrRefTrxId;
    }

    public void setQrRefTrxId(String qrRefTrxId) {
        this.qrRefTrxId = qrRefTrxId;
    }


}
