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

import android.database.Cursor;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.db.dao.TransDataDao;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.commonlib.utils.ObjectPoolHelper;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * Database operation helper of TransData
 */
public class TransDataDbHelper extends BaseDaoHelper {
    private static final String SQL_AND = " AND ";

    private static class LazyHolder {
        public static final TransDataDbHelper INSTANCE = new TransDataDbHelper(TransData.class);
    }

    public static TransDataDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public TransDataDbHelper(Class entityClass) {
        super(entityClass);
    }

    public final TransData findTransDataByTraceNo(long traceNo) {
        return (TransData) getNoSessionQuery().where(TransDataDao.Properties.TraceNo.eq(traceNo)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL))
                .unique();
    }

    public final TransData findTransDataByRef1(String ref1) {
        return (TransData) getNoSessionQuery().where(TransDataDao.Properties.BillPaymentRef1.eq(ref1)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL))
                .unique();
    }

    public final List<TransData> findTransData(List<String> types, List<TransData.ETransStatus> status) {
        return getNoSessionQuery().where(TransDataDao.Properties.TransType.in(types)
                , TransDataDao.Properties.TransState.notIn(status)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL))
                .list();
    }

    public final List<TransData> findOfflineTransData(List<TransData.OfflineStatus> status) {
        return getNoSessionQuery().where(TransDataDao.Properties.OfflineSendState.in(status)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL))
                .list();
    }

    public final List<TransData> findTransData(List<String> types, List<TransData.ETransStatus> statuses, Acquirer acq) {
        return getNoSessionQuery().where(TransDataDao.Properties.TransType.in(types)
                , TransDataDao.Properties.TransState.notIn(statuses)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)
                , TransDataDao.Properties.Acquirer_id.eq(acq.getId())).list();
    }

    public final List<TransData> findSettleTransData(List<String> types, List<TransData.ETransStatus> statuses, Acquirer acq) {
        //SELECT * FROM "trans_data" T  WHERE T."type" IN (?,?,?,?) AND T."state" NOT IN (?) AND T."REVERSAL"=? AND T."ACQUIRER_ID"=? AND (T."type"<>? OR (T."type"=? AND T."offline_state"=?))
        //As long as the adjust is done, no matter whether the upload is successful or failed, it will be regarded as a success
        QueryBuilder query = getNoSessionQuery();
        query.where(TransDataDao.Properties.TransType.in(types)
                , TransDataDao.Properties.TransState.notIn(statuses)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)
                , TransDataDao.Properties.Acquirer_id.eq(acq.getId())
                , query.or(TransDataDao.Properties.TransType.notEq(ETransType.OFFLINE_SALE),
                        query.and(TransDataDao.Properties.TransType.eq(ETransType.OFFLINE_SALE),
                                TransDataDao.Properties.OfflineSendState.eq(TransData.OfflineStatus.OFFLINE_SENT))));
        return query.list();
    }

    public final TransData findLastTransData() {
        List<String> typeList = new ArrayList<>();
        typeList.add(ETransType.SALE.name());
        typeList.add(ETransType.VOID.name());
        typeList.add(ETransType.REFUND.name());
        typeList.add(ETransType.INSTALLMENT.name());
        typeList.add(ETransType.REDEEM.name());
        typeList.add(ETransType.OFFLINE_SALE.name());

        List<TransData.ETransStatus> stateFilter = new ArrayList<>();
        stateFilter.add(TransData.ETransStatus.PENDING);

        List list = getNoSessionQuery().where(TransDataDao.Properties.TransType.in(typeList)
                , TransDataDao.Properties.TransState.notIn(stateFilter)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)).list();
        return list != null && !list.isEmpty() ? (TransData) list.get(list.size() - 1) : null;
    }

    public final TransData findLastTransData(boolean includeOfflineTrans) {
        List<String> typeList = new ArrayList<>();
        typeList.add(ETransType.SALE.name());
        typeList.add(ETransType.VOID.name());
        typeList.add(ETransType.REFUND.name());
        if(includeOfflineTrans) {
            typeList.add(ETransType.OFFLINE_SALE.name());
        }
        typeList.add(ETransType.INSTALLMENT.name());
        typeList.add(ETransType.REDEEM.name());


        List<TransData.ETransStatus> stateFilter = new ArrayList<>();
        stateFilter.add(TransData.ETransStatus.PENDING);
        stateFilter.add(TransData.ETransStatus.SUSPENDED);

        List list = getNoSessionQuery().where(TransDataDao.Properties.TransType.in(typeList)
                , TransDataDao.Properties.TransState.notIn(stateFilter)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)).list();
        return list != null && !list.isEmpty() ? (TransData) list.get(list.size() - 1) : null;
    }

    public final TransData findLastQrTransData() {
        List<String> typeList = new ArrayList<>();
        typeList.add(ETransType.SALE.name());
        typeList.add(ETransType.VOID.name());
        typeList.add(ETransType.REFUND.name());
        typeList.add(ETransType.INSTALLMENT.name());
        typeList.add(ETransType.REDEEM.name());
        typeList.add(ETransType.OFFLINE_SALE.name());

        List<TransData.ETransStatus> stateFilter = new ArrayList<>();
        stateFilter.add(TransData.ETransStatus.PENDING);

        List list = getNoSessionQuery().where(TransDataDao.Properties.TransType.in(typeList)
                , TransDataDao.Properties.TransState.notIn(stateFilter)
                , TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)
                , TransDataDao.Properties.EnterMode.eq(TransData.EnterMode.QR)).list();
        return list != null && !list.isEmpty() ? (TransData) list.get(list.size() - 1) : null;
    }

    public final List findAllTransData() {
        return this.findAllTransData(false);
    }

    public final List findAllSuspendQrCodeTransData() {
        QueryBuilder query = getNoSessionQuery();
        query.where(TransDataDao.Properties.TransType.eq(ETransType.SALE),
                TransDataDao.Properties.TransState.eq(TransData.ETransStatus.SUSPENDED),
                TransDataDao.Properties.EnterMode.eq(TransData.EnterMode.QR));
        return query.orderDesc(TransDataDao.Properties.Id).list();
    }

    public final List findAllTransData(boolean includeReversal) {
        return includeReversal ? this.loadAll() : getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)).list();
    }

    public final List findAllTransData(Acquirer acq) {
        return this.findAllTransData(acq, false, false, false);
    }

    public final List findAllTransData(Acquirer acq, boolean includeVoid, boolean includedSuspended, boolean includedPending) {
        StringBuilder stateBuilder = ObjectPoolHelper.obtainStringBuilder();
        stateBuilder.append("'");
        if (!includeVoid) {
            stateBuilder.append(TransData.ETransStatus.VOIDED.name()).append("','");
        }
        if (!includedSuspended) {
            stateBuilder.append(TransData.ETransStatus.SUSPENDED.name()).append("','");
        }
        if (!includedPending) {
            stateBuilder.append(TransData.ETransStatus.PENDING.name()).append("','");
        }
        stateBuilder.append("'");

        QueryBuilder builder = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)
                , TransDataDao.Properties.Acquirer_id.eq(acq.getId()));
        builder.where(TransDataDao.Properties.TransState.notIn(stateBuilder.toString()));
        return builder.list();
    }

    public final List findAllPendingQrsSettlementSaleTransData() {
        List list = getNoSessionQuery().where(TransDataDao.Properties.TransState.eq(TransData.ETransStatus.NORMAL),
                TransDataDao.Properties.TransType.eq(ETransType.SALE.name()),
                TransDataDao.Properties.FundingSource.notEq("")).list();
        return list;
    }

    public final boolean deleteTransDataByTraceNo(long traceNo) {
        TransData transData = this.findTransDataByTraceNo(traceNo);
        return transData == null || this.delete(transData);
    }

    public final boolean deleteTransDataByBatchNo(Acquirer acquirer, long batchNo) {
        List list = getNoSessionQuery().where(TransDataDao.Properties.BatchNo.eq(batchNo),
                TransDataDao.Properties.Acquirer_id.eq(acquirer.getId())).list();
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

    public final boolean deleteAllTransData() {
        List list = this.findAllTransData(true);
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

    public final boolean deleteAllTransData(Acquirer acq) {
        List list = this.findAllTransData(acq, true, true, true);
        return list.isEmpty() || this.deleteEntities(list);
    }

    public final long countOf() {
        return getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)).count();
    }

    /**
     * select count(*),sum(amount) from trans_data where transType = type
     * and transState = status
     * and acquirer_id = acquirer.id
     * and reversalStatus = normal
     * <p>
     * SELECT count(*),sum(trans_data.amount) FROM trans_data
     * WHERE
     * trans_data.state='NORMAL'
     * AND trans_data.type = 'SALE'
     * AND trans_data.REVERSAL ='NORMAL'
     * AND trans_data.acquirer_id = 1
     */
    public final long[] countSumOf(Acquirer acquirer, String type, TransData.ETransStatus status) {
        long[] longArray = new long[]{0L, 0L};
        StringBuilder stringBuilder = ObjectPoolHelper.obtainStringBuilder();
        stringBuilder.append("SELECT count(*),sum(")
                .append(TransDataDao.Properties.Amount.columnName)
                .append(") FROM ")
                .append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" = '").append(status.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId());
        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(stringBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(stringBuilder);
            if (!cursor.moveToFirst()) {
                return longArray;
            }
            longArray[0] = cursor.getInt(0);
            String sum = cursor.getString(1);
            if (sum == null) {
                longArray[1] = 0;
            } else {
                longArray[1] = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return longArray;
    }

    /**
     * select count(*),sum(amount) from trans_data where transType = type
     * and transState = status
     * and acquirer_id = acquirer.id
     * and reversalStatus = normal
     * <p>
     * SELECT count(*),sum(trans_data.amount) FROM trans_data
     * WHERE
     * trans_data.state='NORMAL'
     * AND trans_data.type = 'SALE'
     * AND trans_data.REVERSAL ='NORMAL'
     * AND trans_data.acquirer_id = 1
     * AND trans_data.orig_type = 'VOID'
     */
    public final long[] countSumOf(Acquirer acquirer, String type, String origType, TransData.ETransStatus status) {
        long[] longArray = new long[]{0L, 0L};
        StringBuilder stringBuilder = ObjectPoolHelper.obtainStringBuilder();
        stringBuilder.append("SELECT count(*),sum(")
                .append(TransDataDao.Properties.Amount.columnName)
                .append(") FROM ")
                .append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" = '").append(status.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId())
                .append(SQL_AND)
                .append(TransDataDao.Properties.OrigTransType.columnName).append(" = '").append(origType).append("'");
        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(stringBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(stringBuilder);
            if (!cursor.moveToFirst()) {
                return longArray;
            }
            longArray[0] = cursor.getInt(0);
            String sum = cursor.getString(1);
            if (sum == null) {
                longArray[1] = 0;
            } else {
                longArray[1] = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return longArray;
    }

    /**
     * SELECT
     * count(*),
     * sum(trans_data.amount)
     * FROM
     * trans_data
     * WHERE
     * trans_data.state IN('NORMAL','TEST')
     * AND trans_data.REVERSAL = 'NORMAL'
     * AND trans_data.acquirer_id = 1
     * AND trans_data.type = 'SALE'
     */
    public final long[] countSumOf(Acquirer acquirer, String type, List<TransData.ETransStatus> status) {
        long[] longArray = new long[]{0L, 0L};
        StringBuilder stateBuilder = ObjectPoolHelper.obtainStringBuilder();
        stateBuilder.append("'");
        for (int index = 0; index < status.size(); index++) {
            if (index != status.size() - 1) {
                stateBuilder.append(status.get(index).toString()).append("','");
            } else {
                stateBuilder.append(status.get(index).toString()).append("'");
            }
        }
        StringBuilder sqlBuilder = ObjectPoolHelper.obtainStringBuilder();
        sqlBuilder.append("SELECT count(*),sum(").append(TransDataDao.Properties.Amount.columnName).append(") FROM ").append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" IN(").append(stateBuilder.toString()).append(" )")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId());
        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(sqlBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(stateBuilder);
            ObjectPoolHelper.releaseStringBuilder(sqlBuilder);
            if (!cursor.moveToFirst()) {
                return longArray;
            }
            longArray[0] = cursor.getInt(0);
            String sum = cursor.getString(1);
            if (sum == null) {
                longArray[1] = 0;
            } else {
                longArray[1] = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return longArray;
    }


    /**
     * select count(*),sum(amount) from trans_data where transType = type
     * and transState = status
     * and acquirer_id = acquirer.id
     * and reversalStatus = normal
     * <p>
     * SELECT count(*),sum(trans_data.amount) FROM trans_data
     * WHERE
     * trans_data.state='NORMAL'
     * AND trans_data.type = 'SALE'
     * AND trans_data.REVERSAL ='NORMAL'
     * AND trans_data.acquirer_id = 1
     * GROUP BY trans_data.paymentPlan ORDER BY trans_data.paymentPlan
     */
    public final long[] countSumOf(Acquirer acquirer, String type, TransData.ETransStatus status, int paymentPlan) {
        long[] longArray = new long[]{0L, 0L, 0L};
        StringBuilder stringBuilder = ObjectPoolHelper.obtainStringBuilder();
        stringBuilder.append("SELECT count(*),sum(")
                .append(TransDataDao.Properties.Amount.columnName)
                .append(") + sum(")
                .append(TransDataDao.Properties.RedeemAmt.columnName)
                .append("), sum(")
                .append(TransDataDao.Properties.RedeemPts.columnName)
                .append(") FROM ")
                .append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" = '").append(status.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId())
        ;
                if (paymentPlan != -1)
                    stringBuilder.append(SQL_AND)
                .append(TransDataDao.Properties.PaymentPlan.columnName).append(" = ").append(paymentPlan)
        ;
        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(stringBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(stringBuilder);
            if (!cursor.moveToFirst()) {
                return longArray;
            }
            longArray[0] = cursor.getInt(0);
            String sum = cursor.getString(1);
            if (sum == null) {
                longArray[1] = 0;
            } else {
                longArray[1] = ConvertUtils.parseLongSafe(sum, 0);
            }
            sum = cursor.getString(2);
            if (sum == null) {
                longArray[2] = 0;
            } else {
                longArray[2] = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return longArray;
    }

    public final long[] countSumOfOffline(Acquirer acquirer, String type, List<TransData.ETransStatus> status) {
        long[] longArray = new long[]{0L, 0L};
        StringBuilder stateBuilder = ObjectPoolHelper.obtainStringBuilder();
        stateBuilder.append("'");
        for (int index = 0; index < status.size(); index++) {
            if (index != status.size() - 1) {
                stateBuilder.append(status.get(index).toString()).append("','");
            } else {
                stateBuilder.append(status.get(index).toString()).append("'");
            }
        }
        StringBuilder sqlBuilder = ObjectPoolHelper.obtainStringBuilder();
        sqlBuilder.append("SELECT count(*),sum(").append(TransDataDao.Properties.Amount.columnName).append(") FROM ").append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" IN(").append(stateBuilder.toString()).append(" )")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId())
                .append(SQL_AND)
                .append(TransDataDao.Properties.OfflineSendState.columnName).append(" IN('").append(TransData.OfflineStatus.OFFLINE_SENT.toString())
                .append("','").append(TransData.OfflineStatus.OFFLINE_NOT_SENT.toString()).append("')");

        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(sqlBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(stateBuilder);
            ObjectPoolHelper.releaseStringBuilder(sqlBuilder);
            if (!cursor.moveToFirst()) {
                return longArray;
            }
            longArray[0] = cursor.getInt(0);
            String sum = cursor.getString(1);
            if (sum == null) {
                longArray[1] = 0;
            } else {
                longArray[1] = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return longArray;
    }

    public final Triple<Long, String, Long> countSumOfRedeem(Acquirer acquirer, String type, TransData.ETransStatus status) {
        long count = 0L;
        String clubPoolId = "";
        long redeemAmt = 0L;

        StringBuilder stringBuilder = ObjectPoolHelper.obtainStringBuilder();
        stringBuilder.append("SELECT count(*)")
                .append("," + TransDataDao.Properties.ClubPoolId.columnName)
                .append(",sum(" + TransDataDao.Properties.RedeemAmt.columnName + ")")
                .append(" FROM ")
                .append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" = '").append(status.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId())
                .append(" GROUP BY ")
                .append(TransDataDao.Properties.ClubPoolId.columnName);
        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(stringBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(stringBuilder);
            if (!cursor.moveToFirst()) {
                return new Triple<>(0L, "", 0L);
            }
            count = cursor.getLong(0);
            clubPoolId = cursor.getString(1);
            String sum = cursor.getString(2);
            if (sum == null) {
                redeemAmt = 0L;
            } else {
                redeemAmt = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return new Triple<>(count, clubPoolId, redeemAmt);
    }

    public final long[] countSumOfQRS(Acquirer acquirer, String type, TransData.ETransStatus status, String fundingSource, String paymentChannel) {
        long[] longArray = new long[]{0L, 0L};
        StringBuilder sqlBuilder = ObjectPoolHelper.obtainStringBuilder();
        sqlBuilder.append("SELECT count(*),sum(").append(TransDataDao.Properties.Amount.columnName).append(") FROM ").append(TransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(TransDataDao.Properties.TransState.columnName).append(" = '").append(status.name()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(TransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.FundingSource.columnName).append(" = '").append(fundingSource).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(TransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId());

        if (paymentChannel != null) {
            sqlBuilder.append(SQL_AND)
                    .append(TransDataDao.Properties.PaymentChannel.columnName).append(" = '").append(paymentChannel).append("'");
        }

        Cursor cursor = (Cursor) null;
        try {
            cursor = getDatabase().rawQuery(sqlBuilder.toString(), null);
            ObjectPoolHelper.releaseStringBuilder(sqlBuilder);
            if (!cursor.moveToFirst()) {
                return longArray;
            }
            longArray[0] = cursor.getInt(0);
            String sum = cursor.getString(1);
            if (sum == null) {
                longArray[1] = 0;
            } else {
                longArray[1] = ConvertUtils.parseLongSafe(sum, 0);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return longArray;
    }

    public final TransData findFirstDupRecord() {
        List list = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.PENDING))
                .list();
        return list != null && !list.isEmpty() ? (TransData) list.get(list.size() - 1) : null;
    }

    /**
     * find limit count TransData
     *
     * @param acq         acquirer
     * @param includeVoid includeVoid
     * @param offset      offset position
     * @param limit       search item count
     * @return List<TransData>
     */
    public final List<TransData> findPagingTransData(Acquirer acq, boolean includeVoid, int offset, int limit) {
        QueryBuilder builder = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)
                , TransDataDao.Properties.Acquirer_id.eq(acq.getId()));
        if (!includeVoid) {
            builder.where(TransDataDao.Properties.TransState.notEq(TransData.ETransStatus.VOIDED));
        }
        return builder.orderDesc(TransDataDao.Properties.Id).offset(offset).limit(limit).list();
    }

    public final boolean deleteDupRecord() {
        List list = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.PENDING)).list();
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

    public final TransData findFirstDupRecord(Acquirer acquirer) {
        List list = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.PENDING), TransDataDao.Properties.Acquirer_id.eq(acquirer.getId()))
                .list();
        return list != null && !list.isEmpty() ? (TransData) list.get(list.size() - 1) : null;
    }

    public final boolean deleteDupRecord(Acquirer acquirer) {
        List list = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.PENDING), TransDataDao.Properties.Acquirer_id.eq(acquirer.getId())).list();
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

}
