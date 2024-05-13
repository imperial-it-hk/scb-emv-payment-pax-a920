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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.bizlib.data.local.db.helper;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.db.dao.TransTotalDao;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.impl.PackRedeem;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * Database operation helper of TransTotal
 */
public class TransTotalDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final TransTotalDbHelper INSTANCE = new TransTotalDbHelper(TransTotal.class);
    }

    public static TransTotalDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public TransTotalDbHelper(Class entityClass) {
        super(entityClass);
    }

    public final TransTotal findLastTransTotal(Acquirer acquirer, boolean isClosed) {
        List<TransTotal> list = this.findAllTransTotal(acquirer, isClosed);
        return list != null && !list.isEmpty() ? (TransTotal) list.get(list.size() - 1) : null;
    }

    public final List<TransTotal> findAllTransTotal(Acquirer acquirer, boolean isClosed) {
        QueryBuilder builder = getNoSessionQuery().where(TransTotalDao.Properties.IsClosed.eq(isClosed));
        if (acquirer != null) {
            builder.where(TransTotalDao.Properties.Acquirer_id.eq(acquirer.getId()));
        }
        return builder.list();
    }

    public final TransTotal calcTotal(Acquirer acquirer) {
        ArrayList<TransData.ETransStatus> filter = new ArrayList<TransData.ETransStatus>();
        filter.add(TransData.ETransStatus.NORMAL);
        filter.add(TransData.ETransStatus.ADJUSTED);
        TransTotal total = new TransTotal();

        total.setBatchNo(acquirer.getCurrBatchNo());//AET-208

        // 消费
        long[] obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.SALE.name(), filter);
        total.setSaleTotalNum(obj[0]);
        total.setSaleTotalAmt(obj[1]);

        // 撤销
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.VOID.name(), TransData.ETransStatus.NORMAL);
        total.setVoidTotalNum(obj[0]);
        total.setVoidTotalAmt(obj[1]);

        // 退货
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL);
        total.setRefundTotalNum(obj[0]);
        total.setRefundTotalAmt(obj[1]);

        //sale void total
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.VOID.name(), ETransType.SALE.name(), TransData.ETransStatus.NORMAL);
        total.setSaleVoidTotalNum(obj[0]);
        total.setSaleVoidTotalAmt(obj[1]);

        //refund void total
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.VOID.name(), ETransType.REFUND.name(), TransData.ETransStatus.NORMAL);
        total.setRefundVoidTotalNum(obj[0]);
        total.setRefundVoidTotalAmt(obj[1]);

        // 预授权
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.PREAUTH.name(), TransData.ETransStatus.NORMAL);
        total.setAuthTotalNum(obj[0]);
        total.setAuthTotalAmt(obj[1]);

        // 脱机 AET-75
        obj = TransDataDbHelper.getInstance().countSumOfOffline(acquirer, ETransType.OFFLINE_SALE.name(), filter);
        total.setOfflineTotalNum(obj[0]);
        total.setOfflineTotalAmt(obj[1]);

//region IPP
        // installment
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.INSTALLMENT.name(), TransData.ETransStatus.NORMAL);
        total.setSaleTotalNum(total.getSaleTotalNum() + obj[0]);
        total.setSaleTotalAmt(total.getSaleTotalAmt() + obj[1]);

        // void installment
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.INSTALLMENT.name(), TransData.ETransStatus.VOIDED);
        total.setSaleVoidTotalNum(total.getSaleVoidTotalNum() + obj[0]);
        total.setSaleVoidTotalAmt(total.getSaleVoidTotalAmt() + obj[1]);
//endregion
//region OLS
        // redemption
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.REDEEM.name(), TransData.ETransStatus.NORMAL, PackRedeem.Plan.VOUCHER.ordinal());
        total.setRedeemVoucherNum(obj[0]);
        total.setRedeemVoucherAmt(obj[1]);
        total.setRedeemVoucherPts(obj[2]);

        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.REDEEM.name(), TransData.ETransStatus.NORMAL, PackRedeem.Plan.DISCOUNT.ordinal());
        total.setRedeemDiscountNum(obj[0]);
        total.setRedeemDiscountAmt(obj[1]);
        total.setRedeemDiscountPts(obj[2]);

        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.REDEEM.name(), TransData.ETransStatus.NORMAL, PackRedeem.Plan.POINT_CREDIT.ordinal());
        total.setRedeemPointNum(obj[0]);
        total.setRedeemPointAmt(obj[1]);
        total.setRedeemPointPts(obj[2]);

        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.REDEEM.name(), TransData.ETransStatus.NORMAL, PackRedeem.Plan.SPECIAL_REDEEM.ordinal());
        total.setRedeemProductNum(obj[0]);
        total.setRedeemProductAmt(obj[1]);
        total.setRedeemProductPts(obj[2]);

        // redemption
        Triple<Long, String, Long> redeem = TransDataDbHelper.getInstance().countSumOfRedeem(acquirer, ETransType.REDEEM.name(), TransData.ETransStatus.NORMAL);
        total.setRedeemTotalNum(redeem.getFirst());
        total.setClubPoolId(redeem.getSecond());
        total.setRedeemTotalAmt(redeem.getThird());

        long redeemCnt = total.getRedeemVoucherNum() + total.getRedeemDiscountNum() +  total.getRedeemPointNum() + total.getRedeemProductNum();
        long redeemAmt = total.getRedeemVoucherAmt() + total.getRedeemDiscountAmt() +  total.getRedeemPointAmt() + total.getRedeemProductAmt();
        total.setSaleTotalNum(total.getSaleTotalNum() + redeemCnt);
        total.setSaleTotalAmt(total.getSaleTotalAmt() + redeemAmt);

        // void redemption
        obj = TransDataDbHelper.getInstance().countSumOf(acquirer, ETransType.REDEEM.name(), TransData.ETransStatus.VOIDED, -1);
        total.setSaleVoidTotalNum(total.getSaleVoidTotalNum() + obj[0]);
        total.setSaleVoidTotalAmt(total.getSaleVoidTotalAmt() + obj[1]);
//endregion

        // QRS
        // alipay
        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.SALE.name(), TransData.ETransStatus.NORMAL, "alipay",null);
        total.setQrsAlipaySaleTotalNum(obj[0]);
        total.setQrsAlipaySaleTotalAmt(obj[1]);

        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL, "alipay",null);
        total.setQrsAlipayRefundTotalNum(obj[0]);
        total.setQrsAlipayRefundTotalAmt(obj[1]);

        // wechat
        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.SALE.name(), TransData.ETransStatus.NORMAL, "wechat",null);
        total.setQrsWechatSaleTotalNum(obj[0]);
        total.setQrsWechatSaleTotalAmt(obj[1]);

        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL, "wechat",null);
        total.setQrsWechatRefundTotalNum(obj[0]);
        total.setQrsWechatRefundTotalAmt(obj[1]);

        // promptpay
        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.SALE.name(), TransData.ETransStatus.NORMAL, "promptpay",null);
        total.setQrsTag30SaleTotalNum(obj[0]);
        total.setQrsTag30SaleTotalAmt(obj[1]);

        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL, "promptpay",null);
        total.setQrsTag30RefundTotalNum(obj[0]);
        total.setQrsTag30RefundTotalAmt(obj[1]);

        // qrcs Visa
        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.SALE.name(), TransData.ETransStatus.NORMAL, "qrcs","VISA");
        total.setQrsQrcsVisaSaleTotalNum(obj[0]);
        total.setQrsQrcsVisaSaleTotalAmt(obj[1]);

        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL, "qrcs","VISA");
        total.setQrsQrcsVisaRefundTotalNum(obj[0]);
        total.setQrsQrcsVisaRefundTotalAmt(obj[1]);

        // qrcs Master
        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.SALE.name(), TransData.ETransStatus.NORMAL, "qrcs","Mastercard");
        total.setQrsQrcsMasterSaleTotalNum(obj[0]);
        total.setQrsQrcsMasterSaleTotalAmt(obj[1]);

        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL, "qrcs","Mastercard");
        total.setQrsQrcsMasterRefundTotalNum(obj[0]);
        total.setQrsQrcsMasterRefundTotalAmt(obj[1]);

        // qrcs UPI
        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.SALE.name(), TransData.ETransStatus.NORMAL, "qrcs","UnionPay");
        total.setQrsQrcsUpiSaleTotalNum(obj[0]);
        total.setQrsQrcsUpiSaleTotalAmt(obj[1]);

        obj = TransDataDbHelper.getInstance().countSumOfQRS(acquirer, ETransType.REFUND.name(), TransData.ETransStatus.NORMAL, "qrcs","UnionPay");
        total.setQrsQrcsUpiRefundTotalNum(obj[0]);
        total.setQrsQrcsUpiRefundTotalAmt(obj[1]);

        total.setAcquirer(acquirer);

        return total;
    }
}
