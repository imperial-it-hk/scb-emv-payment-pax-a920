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

import com.evp.bizlib.data.local.db.dao.AcquirerDao;
import com.evp.bizlib.data.local.db.dao.DaoSession;
import com.evp.bizlib.data.local.db.dao.TransTotalDao;
import com.evp.bizlib.data.local.db.helper.DaoManager;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

import java.io.Serializable;

/**
 * 交易总计
 *
 * @author Steven.W
 */

@Entity(nameInDb = "trans_total")
public class TransTotal implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "id";
    public static final String IS_CLOSED_FIELD_NAME = "closed";
    public static final String MID_FIELD_NAME = "mid";
    public static final String TID_FIELD_NAME = "tid";
    public static final String BATCHNO_FIELD_NAME = "batch_no";
    public static final String TIME_FIELD_NAME = "batch_time";

    public static final String SALE_AMOUNT = "SALE_AMOUNT";
    public static final String SALE_NUM = "SALE_NUM";
    public static final String VOID_AMOUNT = "VOID_AMOUNT";
    public static final String VOID_NUM = "VOID_NUM";
    public static final String REFUND_AMOUNT = "REFUND_AMOUNT";
    public static final String REFUND_NUM = "REFUND_NUM";
    public static final String REFUND_VOID_AMOUNT = "REFUND_VOID_AMOUNT";
    public static final String REFUND_VOID_NUM = "REFUND_VOID_NUM";
    public static final String SALE_VOID_AMOUNT = "SALE_VOID_AMOUNT";
    public static final String SALE_VOID_NUM = "SALE_VOID_NUM";
    public static final String AUTH_AMOUNT = "AUTH_AMOUNT";
    public static final String AUTH_NUM = "AUTH_NUM";
    public static final String OFFLINE_AMOUNT = "OFFLINE_AMOUNT";
    public static final String OFFLINE_NUM = "OFFLINE_NUM";
    public static final String CLUB_POOL_ID = "CLUB_POOL_ID";
    public static final String REDEEM_AMOUNT = "REDEEM_AMOUNT";
    public static final String REDEEM_NUM = "REDEEM_NUM";


    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    protected Long id;

    /**
     * 商户号
     */
    @Property(nameInDb = MID_FIELD_NAME)
    private String merchantID;
    /**
     * 终端号
     */
    @Property(nameInDb = TID_FIELD_NAME)
    private String terminalID;
    /**
     * 批次号
     */
    @Property(nameInDb = BATCHNO_FIELD_NAME)
    private int batchNo;

    /**
     * 日期时间
     */
    @Property(nameInDb = TIME_FIELD_NAME)
    private String dateTime;

    private long acquirer_id;
    @ToOne(joinProperty = ID_FIELD_NAME)
    private Acquirer acquirer;

    @Property(nameInDb = IS_CLOSED_FIELD_NAME)
    private boolean isClosed;

    /**
     * 消费总金额
     */
    @Property(nameInDb = SALE_AMOUNT)
    private long saleTotalAmt;
    /**
     * 消费总笔数
     */
    @Property(nameInDb = SALE_NUM)
    private long saleTotalNum;

    /**
     * 撤销总金额
     */
    @Property(nameInDb = VOID_AMOUNT)
    private long voidTotalAmt;
    /**
     * 撤销总笔数
     */
    @Property(nameInDb = VOID_NUM)
    private long voidTotalNum;
    /**
     * 退货总金额
     */
    @Property(nameInDb = REFUND_AMOUNT)
    private long refundTotalAmt;
    /**
     * 退货总笔数
     */
    @Property(nameInDb = REFUND_NUM)
    private long refundTotalNum;
    /**
     * refund void total amount
     */
    @Property(nameInDb = REFUND_VOID_AMOUNT)
    private long refundVoidTotalAmt;
    /**
     * refund void total num
     */
    @Property(nameInDb = REFUND_VOID_NUM)
    private long refundVoidTotalNum;
    /**
     * sale void total amount
     */
    @Property(nameInDb = SALE_VOID_AMOUNT)
    private long saleVoidTotalAmt;
    /**
     * sale void total num
     */
    @Property(nameInDb = SALE_VOID_NUM)
    private long saleVoidTotalNum;
    /**
     * 预授权总金额
     */
    @Property(nameInDb = AUTH_AMOUNT)
    private long authTotalAmt;
    /**
     * 预授权总笔数
     */
    @Property(nameInDb = AUTH_NUM)
    private long authTotalNum;
    //AET-75
    /**
     * 脱机交易总金额
     */
    @Property(nameInDb = OFFLINE_AMOUNT)
    private long offlineTotalAmt;
    /**
     * 脱机交易总笔数
     */
    @Property(nameInDb = OFFLINE_NUM)
    private long offlineTotalNum;

    private String clubPoolId;
    private long redeemTotalAmt;
    private long redeemTotalNum;
    private long redeemVoucherAmt;
    private long redeemVoucherPts;
    private long redeemVoucherNum;
    private long redeemPointAmt;
    private long redeemPointPts;
    private long redeemPointNum;
    private long redeemDiscountAmt;
    private long redeemDiscountPts;
    private long redeemDiscountNum;
    private long redeemProductAmt;
    private long redeemProductPts;
    private long redeemProductNum;

    // QRS
    // Sales
    private long qrsAlipaySaleTotalAmt;
    private long qrsAlipaySaleTotalNum;
    private long qrsWechatSaleTotalAmt;
    private long qrsWechatSaleTotalNum;
    private long qrsTag30SaleTotalAmt;
    private long qrsTag30SaleTotalNum;
    private long qrsQrcsVisaSaleTotalAmt;
    private long qrsQrcsVisaSaleTotalNum;
    private long qrsQrcsMasterSaleTotalAmt;
    private long qrsQrcsMasterSaleTotalNum;
    private long qrsQrcsUpiSaleTotalAmt;
    private long qrsQrcsUpiSaleTotalNum;

    // refund
    private long qrsAlipayRefundTotalAmt;
    private long qrsAlipayRefundTotalNum;
    private long qrsWechatRefundTotalAmt;
    private long qrsWechatRefundTotalNum;
    private long qrsTag30RefundTotalAmt;
    private long qrsTag30RefundTotalNum;
    private long qrsQrcsVisaRefundTotalAmt;
    private long qrsQrcsVisaRefundTotalNum;
    private long qrsQrcsMasterRefundTotalAmt;
    private long qrsQrcsMasterRefundTotalNum;
    private long qrsQrcsUpiRefundTotalAmt;
    private long qrsQrcsUpiRefundTotalNum;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1507415572)
    private transient TransTotalDao myDao;


    @Generated(hash = 282280578)
    public TransTotal(Long id, String merchantID, String terminalID, int batchNo, String dateTime, long acquirer_id, boolean isClosed,
            long saleTotalAmt, long saleTotalNum, long voidTotalAmt, long voidTotalNum, long refundTotalAmt, long refundTotalNum,
            long refundVoidTotalAmt, long refundVoidTotalNum, long saleVoidTotalAmt, long saleVoidTotalNum, long authTotalAmt,
            long authTotalNum, long offlineTotalAmt, long offlineTotalNum, String clubPoolId, long redeemTotalAmt, long redeemTotalNum,
            long redeemVoucherAmt, long redeemVoucherPts, long redeemVoucherNum, long redeemPointAmt, long redeemPointPts, long redeemPointNum,
            long redeemDiscountAmt, long redeemDiscountPts, long redeemDiscountNum, long redeemProductAmt, long redeemProductPts,
            long redeemProductNum, long qrsAlipaySaleTotalAmt, long qrsAlipaySaleTotalNum, long qrsWechatSaleTotalAmt,
            long qrsWechatSaleTotalNum, long qrsTag30SaleTotalAmt, long qrsTag30SaleTotalNum, long qrsQrcsVisaSaleTotalAmt,
            long qrsQrcsVisaSaleTotalNum, long qrsQrcsMasterSaleTotalAmt, long qrsQrcsMasterSaleTotalNum, long qrsQrcsUpiSaleTotalAmt,
            long qrsQrcsUpiSaleTotalNum, long qrsAlipayRefundTotalAmt, long qrsAlipayRefundTotalNum, long qrsWechatRefundTotalAmt,
            long qrsWechatRefundTotalNum, long qrsTag30RefundTotalAmt, long qrsTag30RefundTotalNum, long qrsQrcsVisaRefundTotalAmt,
            long qrsQrcsVisaRefundTotalNum, long qrsQrcsMasterRefundTotalAmt, long qrsQrcsMasterRefundTotalNum, long qrsQrcsUpiRefundTotalAmt,
            long qrsQrcsUpiRefundTotalNum) {
        this.id = id;
        this.merchantID = merchantID;
        this.terminalID = terminalID;
        this.batchNo = batchNo;
        this.dateTime = dateTime;
        this.acquirer_id = acquirer_id;
        this.isClosed = isClosed;
        this.saleTotalAmt = saleTotalAmt;
        this.saleTotalNum = saleTotalNum;
        this.voidTotalAmt = voidTotalAmt;
        this.voidTotalNum = voidTotalNum;
        this.refundTotalAmt = refundTotalAmt;
        this.refundTotalNum = refundTotalNum;
        this.refundVoidTotalAmt = refundVoidTotalAmt;
        this.refundVoidTotalNum = refundVoidTotalNum;
        this.saleVoidTotalAmt = saleVoidTotalAmt;
        this.saleVoidTotalNum = saleVoidTotalNum;
        this.authTotalAmt = authTotalAmt;
        this.authTotalNum = authTotalNum;
        this.offlineTotalAmt = offlineTotalAmt;
        this.offlineTotalNum = offlineTotalNum;
        this.clubPoolId = clubPoolId;
        this.redeemTotalAmt = redeemTotalAmt;
        this.redeemTotalNum = redeemTotalNum;
        this.redeemVoucherAmt = redeemVoucherAmt;
        this.redeemVoucherPts = redeemVoucherPts;
        this.redeemVoucherNum = redeemVoucherNum;
        this.redeemPointAmt = redeemPointAmt;
        this.redeemPointPts = redeemPointPts;
        this.redeemPointNum = redeemPointNum;
        this.redeemDiscountAmt = redeemDiscountAmt;
        this.redeemDiscountPts = redeemDiscountPts;
        this.redeemDiscountNum = redeemDiscountNum;
        this.redeemProductAmt = redeemProductAmt;
        this.redeemProductPts = redeemProductPts;
        this.redeemProductNum = redeemProductNum;
        this.qrsAlipaySaleTotalAmt = qrsAlipaySaleTotalAmt;
        this.qrsAlipaySaleTotalNum = qrsAlipaySaleTotalNum;
        this.qrsWechatSaleTotalAmt = qrsWechatSaleTotalAmt;
        this.qrsWechatSaleTotalNum = qrsWechatSaleTotalNum;
        this.qrsTag30SaleTotalAmt = qrsTag30SaleTotalAmt;
        this.qrsTag30SaleTotalNum = qrsTag30SaleTotalNum;
        this.qrsQrcsVisaSaleTotalAmt = qrsQrcsVisaSaleTotalAmt;
        this.qrsQrcsVisaSaleTotalNum = qrsQrcsVisaSaleTotalNum;
        this.qrsQrcsMasterSaleTotalAmt = qrsQrcsMasterSaleTotalAmt;
        this.qrsQrcsMasterSaleTotalNum = qrsQrcsMasterSaleTotalNum;
        this.qrsQrcsUpiSaleTotalAmt = qrsQrcsUpiSaleTotalAmt;
        this.qrsQrcsUpiSaleTotalNum = qrsQrcsUpiSaleTotalNum;
        this.qrsAlipayRefundTotalAmt = qrsAlipayRefundTotalAmt;
        this.qrsAlipayRefundTotalNum = qrsAlipayRefundTotalNum;
        this.qrsWechatRefundTotalAmt = qrsWechatRefundTotalAmt;
        this.qrsWechatRefundTotalNum = qrsWechatRefundTotalNum;
        this.qrsTag30RefundTotalAmt = qrsTag30RefundTotalAmt;
        this.qrsTag30RefundTotalNum = qrsTag30RefundTotalNum;
        this.qrsQrcsVisaRefundTotalAmt = qrsQrcsVisaRefundTotalAmt;
        this.qrsQrcsVisaRefundTotalNum = qrsQrcsVisaRefundTotalNum;
        this.qrsQrcsMasterRefundTotalAmt = qrsQrcsMasterRefundTotalAmt;
        this.qrsQrcsMasterRefundTotalNum = qrsQrcsMasterRefundTotalNum;
        this.qrsQrcsUpiRefundTotalAmt = qrsQrcsUpiRefundTotalAmt;
        this.qrsQrcsUpiRefundTotalNum = qrsQrcsUpiRefundTotalNum;
    }

    @Generated(hash = 1642661961)
    public TransTotal() {
    }


    @Generated(hash = 86676445)
    private transient Long acquirer__resolvedKey;


    public boolean isZero() {
        return (getSaleTotalNum() + getRefundTotalNum() + getSaleVoidTotalNum() + getRefundVoidTotalNum() + getOfflineTotalNum()) == 0;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getMerchantID() {
        return this.merchantID;
    }


    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }


    public String getTerminalID() {
        return this.terminalID;
    }


    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }


    public int getBatchNo() {
        return this.batchNo;
    }


    public void setBatchNo(int batchNo) {
        this.batchNo = batchNo;
    }


    public String getDateTime() {
        return this.dateTime;
    }


    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public boolean getIsClosed() {
        return this.isClosed;
    }


    public void setIsClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }



    public long getSaleTotalAmt() {
        return this.saleTotalAmt;
    }


    public void setSaleTotalAmt(long saleTotalAmt) {
        this.saleTotalAmt = saleTotalAmt;
    }


    public long getSaleTotalNum() {
        return this.saleTotalNum;
    }


    public void setSaleTotalNum(long saleTotalNum) {
        this.saleTotalNum = saleTotalNum;
    }


    public long getVoidTotalAmt() {
        return this.voidTotalAmt;
    }


    public void setVoidTotalAmt(long voidTotalAmt) {
        this.voidTotalAmt = voidTotalAmt;
    }


    public long getVoidTotalNum() {
        return this.voidTotalNum;
    }


    public void setVoidTotalNum(long voidTotalNum) {
        this.voidTotalNum = voidTotalNum;
    }


    public long getRefundTotalAmt() {
        return this.refundTotalAmt;
    }


    public void setRefundTotalAmt(long refundTotalAmt) {
        this.refundTotalAmt = refundTotalAmt;
    }


    public long getRefundTotalNum() {
        return this.refundTotalNum;
    }


    public void setRefundTotalNum(long refundTotalNum) {
        this.refundTotalNum = refundTotalNum;
    }



    public long getRefundVoidTotalAmt() {
        return this.refundVoidTotalAmt;
    }


    public void setRefundVoidTotalAmt(long refundVoidTotalAmt) {
        this.refundVoidTotalAmt = refundVoidTotalAmt;
    }


    public long getRefundVoidTotalNum() {
        return this.refundVoidTotalNum;
    }


    public void setRefundVoidTotalNum(long refundVoidTotalNum) {
        this.refundVoidTotalNum = refundVoidTotalNum;
    }


    public long getSaleVoidTotalAmt() {
        return this.saleVoidTotalAmt;
    }


    public void setSaleVoidTotalAmt(long saleVoidTotalAmt) {
        this.saleVoidTotalAmt = saleVoidTotalAmt;
    }


    public long getSaleVoidTotalNum() {
        return this.saleVoidTotalNum;
    }


    public void setSaleVoidTotalNum(long saleVoidTotalNum) {
        this.saleVoidTotalNum = saleVoidTotalNum;
    }



    public long getAuthTotalAmt() {
        return this.authTotalAmt;
    }


    public void setAuthTotalAmt(long authTotalAmt) {
        this.authTotalAmt = authTotalAmt;
    }


    public long getAuthTotalNum() {
        return this.authTotalNum;
    }


    public void setAuthTotalNum(long authTotalNum) {
        this.authTotalNum = authTotalNum;
    }



    public long getOfflineTotalAmt() {
        return this.offlineTotalAmt;
    }


    public void setOfflineTotalAmt(long offlineTotalAmt) {
        this.offlineTotalAmt = offlineTotalAmt;
    }


    public long getOfflineTotalNum() {
        return this.offlineTotalNum;
    }


    public void setOfflineTotalNum(long offlineTotalNum) {
        this.offlineTotalNum = offlineTotalNum;
    }


    @Keep
    public Acquirer getAcquirer() {
        Long __key = this.id;
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


    public long getAcquirer_id() {
        return this.acquirer_id;
    }


    public void setAcquirer_id(long acquirer_id) {
        this.acquirer_id = acquirer_id;
    }


    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 328964330)
    public void setAcquirer(Acquirer acquirer) {
        synchronized (this) {
            this.acquirer = acquirer;
            id = acquirer == null ? null : acquirer.getId();
            acquirer__resolvedKey = id;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 918122530)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransTotalDao() : null;
    }

    public String getClubPoolId() {
        return this.clubPoolId;
    }

    public void setClubPoolId(String clubPoolId) {
        this.clubPoolId = clubPoolId;
    }

    public long getRedeemTotalAmt() {
        return this.redeemTotalAmt;
    }

    public void setRedeemTotalAmt(long redeemTotalAmt) {
        this.redeemTotalAmt = redeemTotalAmt;
    }

    public long getRedeemTotalNum() {
        return this.redeemTotalNum;
    }

    public void setRedeemTotalNum(long redeemTotalNum) {
        this.redeemTotalNum = redeemTotalNum;
    }

    public long getRedeemVoucherAmt() {
        return this.redeemVoucherAmt;
    }

    public void setRedeemVoucherAmt(long redeemVoucherAmt) {
        this.redeemVoucherAmt = redeemVoucherAmt;
    }

    public long getRedeemVoucherPts() {
        return this.redeemVoucherPts;
    }

    public void setRedeemVoucherPts(long redeemVoucherPts) {
        this.redeemVoucherPts = redeemVoucherPts;
    }

    public long getRedeemVoucherNum() {
        return this.redeemVoucherNum;
    }

    public void setRedeemVoucherNum(long redeemVoucherNum) {
        this.redeemVoucherNum = redeemVoucherNum;
    }

    public long getRedeemPointAmt() {
        return this.redeemPointAmt;
    }

    public void setRedeemPointAmt(long redeemPointAmt) {
        this.redeemPointAmt = redeemPointAmt;
    }

    public long getRedeemPointPts() {
        return this.redeemPointPts;
    }

    public void setRedeemPointPts(long redeemPointPts) {
        this.redeemPointPts = redeemPointPts;
    }

    public long getRedeemPointNum() {
        return this.redeemPointNum;
    }

    public void setRedeemPointNum(long redeemPointNum) {
        this.redeemPointNum = redeemPointNum;
    }

    public long getRedeemDiscountAmt() {
        return this.redeemDiscountAmt;
    }

    public void setRedeemDiscountAmt(long redeemDiscountAmt) {
        this.redeemDiscountAmt = redeemDiscountAmt;
    }

    public long getRedeemDiscountPts() {
        return this.redeemDiscountPts;
    }

    public void setRedeemDiscountPts(long redeemDiscountPts) {
        this.redeemDiscountPts = redeemDiscountPts;
    }

    public long getRedeemDiscountNum() {
        return this.redeemDiscountNum;
    }

    public void setRedeemDiscountNum(long redeemDiscountNum) {
        this.redeemDiscountNum = redeemDiscountNum;
    }

    public long getRedeemProductAmt() {
        return this.redeemProductAmt;
    }

    public void setRedeemProductAmt(long redeemProductAmt) {
        this.redeemProductAmt = redeemProductAmt;
    }

    public long getRedeemProductPts() {
        return this.redeemProductPts;
    }

    public void setRedeemProductPts(long redeemProductPts) {
        this.redeemProductPts = redeemProductPts;
    }

    public long getRedeemProductNum() {
        return this.redeemProductNum;
    }

    public void setRedeemProductNum(long redeemProductNum) {
        this.redeemProductNum = redeemProductNum;
    }

    public long getQrsAlipaySaleTotalAmt() {
        return this.qrsAlipaySaleTotalAmt;
    }

    public void setQrsAlipaySaleTotalAmt(long qrsAlipaySaleTotalAmt) {
        this.qrsAlipaySaleTotalAmt = qrsAlipaySaleTotalAmt;
    }

    public long getQrsAlipaySaleTotalNum() {
        return this.qrsAlipaySaleTotalNum;
    }

    public void setQrsAlipaySaleTotalNum(long qrsAlipaySaleTotalNum) {
        this.qrsAlipaySaleTotalNum = qrsAlipaySaleTotalNum;
    }

    public long getQrsWechatSaleTotalAmt() {
        return this.qrsWechatSaleTotalAmt;
    }

    public void setQrsWechatSaleTotalAmt(long qrsWechatSaleTotalAmt) {
        this.qrsWechatSaleTotalAmt = qrsWechatSaleTotalAmt;
    }

    public long getQrsWechatSaleTotalNum() {
        return this.qrsWechatSaleTotalNum;
    }

    public void setQrsWechatSaleTotalNum(long qrsWechatSaleTotalNum) {
        this.qrsWechatSaleTotalNum = qrsWechatSaleTotalNum;
    }

    public long getQrsTag30SaleTotalAmt() {
        return this.qrsTag30SaleTotalAmt;
    }

    public void setQrsTag30SaleTotalAmt(long qrsTag30SaleTotalAmt) {
        this.qrsTag30SaleTotalAmt = qrsTag30SaleTotalAmt;
    }

    public long getQrsTag30SaleTotalNum() {
        return this.qrsTag30SaleTotalNum;
    }

    public void setQrsTag30SaleTotalNum(long qrsTag30SaleTotalNum) {
        this.qrsTag30SaleTotalNum = qrsTag30SaleTotalNum;
    }

    public long getQrsAlipayRefundTotalAmt() {
        return this.qrsAlipayRefundTotalAmt;
    }

    public void setQrsAlipayRefundTotalAmt(long qrsAlipayRefundTotalAmt) {
        this.qrsAlipayRefundTotalAmt = qrsAlipayRefundTotalAmt;
    }

    public long getQrsAlipayRefundTotalNum() {
        return this.qrsAlipayRefundTotalNum;
    }

    public void setQrsAlipayRefundTotalNum(long qrsAlipayRefundTotalNum) {
        this.qrsAlipayRefundTotalNum = qrsAlipayRefundTotalNum;
    }

    public long getQrsWechatRefundTotalAmt() {
        return this.qrsWechatRefundTotalAmt;
    }

    public void setQrsWechatRefundTotalAmt(long qrsWechatRefundTotalAmt) {
        this.qrsWechatRefundTotalAmt = qrsWechatRefundTotalAmt;
    }

    public long getQrsWechatRefundTotalNum() {
        return this.qrsWechatRefundTotalNum;
    }

    public void setQrsWechatRefundTotalNum(long qrsWechatRefundTotalNum) {
        this.qrsWechatRefundTotalNum = qrsWechatRefundTotalNum;
    }

    public long getQrsTag30RefundTotalAmt() {
        return this.qrsTag30RefundTotalAmt;
    }

    public void setQrsTag30RefundTotalAmt(long qrsTag30RefundTotalAmt) {
        this.qrsTag30RefundTotalAmt = qrsTag30RefundTotalAmt;
    }

    public long getQrsTag30RefundTotalNum() {
        return this.qrsTag30RefundTotalNum;
    }

    public void setQrsTag30RefundTotalNum(long qrsTag30RefundTotalNum) {
        this.qrsTag30RefundTotalNum = qrsTag30RefundTotalNum;
    }

    public long getQrsQrcsVisaSaleTotalAmt() {
        return this.qrsQrcsVisaSaleTotalAmt;
    }

    public void setQrsQrcsVisaSaleTotalAmt(long qrsQrcsVisaSaleTotalAmt) {
        this.qrsQrcsVisaSaleTotalAmt = qrsQrcsVisaSaleTotalAmt;
    }

    public long getQrsQrcsVisaSaleTotalNum() {
        return this.qrsQrcsVisaSaleTotalNum;
    }

    public void setQrsQrcsVisaSaleTotalNum(long qrsQrcsVisaSaleTotalNum) {
        this.qrsQrcsVisaSaleTotalNum = qrsQrcsVisaSaleTotalNum;
    }

    public long getQrsQrcsMasterSaleTotalAmt() {
        return this.qrsQrcsMasterSaleTotalAmt;
    }

    public void setQrsQrcsMasterSaleTotalAmt(long qrsQrcsMasterSaleTotalAmt) {
        this.qrsQrcsMasterSaleTotalAmt = qrsQrcsMasterSaleTotalAmt;
    }

    public long getQrsQrcsMasterSaleTotalNum() {
        return this.qrsQrcsMasterSaleTotalNum;
    }

    public void setQrsQrcsMasterSaleTotalNum(long qrsQrcsMasterSaleTotalNum) {
        this.qrsQrcsMasterSaleTotalNum = qrsQrcsMasterSaleTotalNum;
    }

    public long getQrsQrcsUpiSaleTotalAmt() {
        return this.qrsQrcsUpiSaleTotalAmt;
    }

    public void setQrsQrcsUpiSaleTotalAmt(long qrsQrcsUpiSaleTotalAmt) {
        this.qrsQrcsUpiSaleTotalAmt = qrsQrcsUpiSaleTotalAmt;
    }

    public long getQrsQrcsUpiSaleTotalNum() {
        return this.qrsQrcsUpiSaleTotalNum;
    }

    public void setQrsQrcsUpiSaleTotalNum(long qrsQrcsUpiSaleTotalNum) {
        this.qrsQrcsUpiSaleTotalNum = qrsQrcsUpiSaleTotalNum;
    }

    public long getQrsQrcsVisaRefundTotalAmt() {
        return this.qrsQrcsVisaRefundTotalAmt;
    }

    public void setQrsQrcsVisaRefundTotalAmt(long qrsQrcsVisaRefundTotalAmt) {
        this.qrsQrcsVisaRefundTotalAmt = qrsQrcsVisaRefundTotalAmt;
    }

    public long getQrsQrcsVisaRefundTotalNum() {
        return this.qrsQrcsVisaRefundTotalNum;
    }

    public void setQrsQrcsVisaRefundTotalNum(long qrsQrcsVisaRefundTotalNum) {
        this.qrsQrcsVisaRefundTotalNum = qrsQrcsVisaRefundTotalNum;
    }

    public long getQrsQrcsMasterRefundTotalAmt() {
        return this.qrsQrcsMasterRefundTotalAmt;
    }

    public void setQrsQrcsMasterRefundTotalAmt(long qrsQrcsMasterRefundTotalAmt) {
        this.qrsQrcsMasterRefundTotalAmt = qrsQrcsMasterRefundTotalAmt;
    }

    public long getQrsQrcsMasterRefundTotalNum() {
        return this.qrsQrcsMasterRefundTotalNum;
    }

    public void setQrsQrcsMasterRefundTotalNum(long qrsQrcsMasterRefundTotalNum) {
        this.qrsQrcsMasterRefundTotalNum = qrsQrcsMasterRefundTotalNum;
    }

    public long getQrsQrcsUpiRefundTotalAmt() {
        return this.qrsQrcsUpiRefundTotalAmt;
    }

    public void setQrsQrcsUpiRefundTotalAmt(long qrsQrcsUpiRefundTotalAmt) {
        this.qrsQrcsUpiRefundTotalAmt = qrsQrcsUpiRefundTotalAmt;
    }

    public long getQrsQrcsUpiRefundTotalNum() {
        return this.qrsQrcsUpiRefundTotalNum;
    }

    public void setQrsQrcsUpiRefundTotalNum(long qrsQrcsUpiRefundTotalNum) {
        this.qrsQrcsUpiRefundTotalNum = qrsQrcsUpiRefundTotalNum;
    }

}
