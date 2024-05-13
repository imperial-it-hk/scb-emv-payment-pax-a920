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
import com.evp.bizlib.data.entity.SettledTransData;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.db.dao.SettledTransDataDao;
import com.evp.bizlib.data.local.db.dao.TransDataDao;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.commonlib.utils.ObjectPoolHelper;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Database operation helper of TransData
 */
public class SettledTransDataDbHelper extends BaseDaoHelper {
    private static final String SQL_AND = " AND ";
    private static class LazyHolder {
        public static final SettledTransDataDbHelper INSTANCE = new SettledTransDataDbHelper(SettledTransData.class);
    }

    public static SettledTransDataDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public SettledTransDataDbHelper(Class entityClass) {
        super(entityClass);
    }

    public final SettledTransData findSettledTransDataByTraceNo(long traceNo) {
        return (SettledTransData) getNoSessionQuery().where(SettledTransDataDao.Properties.TraceNo.eq(traceNo)
                , SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL))
                .unique();
    }

    public final List<SettledTransData> findSettledTransData(List<String> types, List<SettledTransData.ETransStatus> status) {
        return getNoSessionQuery().where(SettledTransDataDao.Properties.TransType.in(types)
                , SettledTransDataDao.Properties.TransState.notIn(status)
                , SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL))
                .list();
    }

    public final List<SettledTransData> findOfflineSettledTransData(List<SettledTransData.OfflineStatus> status) {
        return getNoSessionQuery().where(SettledTransDataDao.Properties.OfflineSendState.in(status)
                , SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL))
                .list();
    }

    public final List<SettledTransData> findSettledTransData(List<String> types, List<SettledTransData.ETransStatus> statuses, Acquirer acq) {
        return getNoSessionQuery().where(SettledTransDataDao.Properties.TransType.in(types)
                , SettledTransDataDao.Properties.TransState.notIn(statuses)
                , SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL)
                , SettledTransDataDao.Properties.Acquirer_id.eq(acq.getId())).list();
    }

    public final List<SettledTransData> findSettleSettledTransData(List<String> types, List<SettledTransData.ETransStatus> statuses, Acquirer acq) {
        //SELECT * FROM "trans_data" T  WHERE T."type" IN (?,?,?,?) AND T."state" NOT IN (?) AND T."REVERSAL"=? AND T."ACQUIRER_ID"=? AND (T."type"<>? OR (T."type"=? AND T."offline_state"=?))
        //As long as the adjust is done, no matter whether the upload is successful or failed, it will be regarded as a success
        QueryBuilder query = getNoSessionQuery();
        query.where(SettledTransDataDao.Properties.TransType.in(types)
                , SettledTransDataDao.Properties.TransState.notIn(statuses)
                , SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL)
                , SettledTransDataDao.Properties.Acquirer_id.eq(acq.getId())
                , query.or(SettledTransDataDao.Properties.TransType.notEq(ETransType.OFFLINE_SALE),
                        query.and(SettledTransDataDao.Properties.TransType.eq(ETransType.OFFLINE_SALE),
                                SettledTransDataDao.Properties.OfflineSendState.eq(SettledTransData.OfflineStatus.OFFLINE_SENT))));
        return query.list();
    }

    public final SettledTransData findLastSettledTransData() {
        List list = this.loadAll();
        return list != null && !list.isEmpty() ? (SettledTransData) list.get(list.size() - 1) : null;
    }

    public final List findAllSettledSettledTransData() {
        return this.findAllSettledTransData(false);
    }

    public final List findAllSettledTransData(boolean includeReversal) {
        return includeReversal ? this.loadAll() : getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(TransData.ReversalStatus.NORMAL)).list();
    }

    public final List findAllSettledTransData(Acquirer acq) {
        return this.findAllSettledTransData(acq, false);
    }

    public final List findAllSettledTransData(Acquirer acq, boolean includeVoid) {
        QueryBuilder builder = getNoSessionQuery().where(SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL)
                , SettledTransDataDao.Properties.Acquirer_id.eq(acq.getId()));
        if (!includeVoid) {
            builder.where(SettledTransDataDao.Properties.TransState.notEq(SettledTransData.ETransStatus.VOIDED));
        }
        return builder.list();
    }

    public final boolean deleteSettledTransDataByTraceNo(long traceNo) {
        SettledTransData settledTransData = this.findSettledTransDataByTraceNo(traceNo);
        return settledTransData == null || this.delete(settledTransData);
    }

    public final boolean deleteSettledTransDataByBatchNo(Acquirer acquirer, long batchNo) {
        List list = getNoSessionQuery().where(SettledTransDataDao.Properties.BatchNo.eq(batchNo),
                SettledTransDataDao.Properties.Acquirer_id.eq(acquirer.getId())).list();
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

    public final boolean deleteAllSettledTransData() {
        List list = this.findAllSettledTransData(true);
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

    public final boolean deleteAllSettledTransData(Acquirer acq) {
        List list = this.findAllSettledTransData(acq, true);
        return list.isEmpty() || this.deleteEntities(list);
    }

    public final long countOf() {
        return getNoSessionQuery().where(SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL)).count();
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
    public final long[] countSumOf(Acquirer acquirer, String type, SettledTransData.ETransStatus status) {
        long[] longArray = new long[]{0L, 0L};
        StringBuilder stringBuilder = ObjectPoolHelper.obtainStringBuilder();
        stringBuilder.append("SELECT count(*),sum(")
                .append(SettledTransDataDao.Properties.Amount.columnName)
                .append(") FROM ")
                .append(SettledTransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(SettledTransDataDao.Properties.TransState.columnName).append(" = '").append(status.toString()).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(SettledTransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId());
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
    public final long[] countSumOf(Acquirer acquirer, String type, List<SettledTransData.ETransStatus> status) {
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
        sqlBuilder.append("SELECT count(*),sum(").append(SettledTransDataDao.Properties.Amount.columnName).append(") FROM ").append(SettledTransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(SettledTransDataDao.Properties.TransState.columnName ).append(" IN(").append(stateBuilder.toString()).append(" )")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(SettledTransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId());
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
    public final long[] countSumOf(Acquirer acquirer, String type, SettledTransData.ETransStatus status, int paymentPlan) {
        long[] longArray = new long[]{0L, 0L, 0L};
        StringBuilder stringBuilder = ObjectPoolHelper.obtainStringBuilder();
        stringBuilder.append("SELECT count(*),sum(")
                .append(SettledTransDataDao.Properties.RedeemAmt.columnName)
                .append("), sum(")
                .append(SettledTransDataDao.Properties.RedeemPts.columnName)
                .append(") FROM ")
                .append(SettledTransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(SettledTransDataDao.Properties.TransState.columnName).append(" = '").append(status.toString()).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(SettledTransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId())
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.PaymentPlan.columnName).append(" = ").append(paymentPlan)
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

    public final long[] countSumOfOffline(Acquirer acquirer, String type, List<SettledTransData.ETransStatus> status) {
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
        sqlBuilder.append("SELECT count(*),sum(").append(SettledTransDataDao.Properties.Amount.columnName).append(") FROM ").append(SettledTransDataDao.TABLENAME)
                .append(" WHERE ")
                .append(SettledTransDataDao.Properties.TransState.columnName ).append(" IN(").append(stateBuilder.toString()).append(" )")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.ReversalStatus.columnName).append(" = '").append(SettledTransData.ReversalStatus.NORMAL.toString()).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.TransType.columnName).append(" = '").append(type).append("'")
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.Acquirer_id.columnName).append(" = ").append(acquirer.getId())
                .append(SQL_AND)
                .append(SettledTransDataDao.Properties.OfflineSendState.columnName).append(" IN('").append(SettledTransData.OfflineStatus.OFFLINE_SENT.toString())
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

    public final SettledTransData findFirstDupRecord() {
        List list = getNoSessionQuery().where(TransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.PENDING))
                .list();
        return list != null && !list.isEmpty() ? (SettledTransData) list.get(list.size() - 1) : null;
    }

    /**
     * find limit count TransData
     * @param acq acquirer
     * @param includeVoid includeVoid
     * @param offset offset position
     * @param limit search item count
     * @return List<TransData>
     */
    public final List<SettledTransData> findPagingSettledTransData(Acquirer acq,boolean includeVoid,int offset,int limit) {
        QueryBuilder builder = getNoSessionQuery().where(SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.NORMAL)
                , TransDataDao.Properties.Acquirer_id.eq(acq.getId()));
        if (!includeVoid) {
            builder.where(SettledTransDataDao.Properties.TransState.notEq(SettledTransData.ETransStatus.VOIDED));
        }
        return builder.orderDesc(SettledTransDataDao.Properties.Id).offset(offset).limit(limit).list();
    }

    public final boolean deleteDupRecord() {
        List list = getNoSessionQuery().where(SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.PENDING)).list();
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

    public final SettledTransData findFirstDupRecord(Acquirer acquirer) {
        List list = getNoSessionQuery().where(SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.PENDING), SettledTransDataDao.Properties.Acquirer_id.eq(acquirer.getId()))
                .list();
        return list != null && !list.isEmpty() ? (SettledTransData) list.get(list.size() - 1) : null;
    }

    public final boolean deleteDupRecord(Acquirer acquirer) {
        List list = getNoSessionQuery().where(SettledTransDataDao.Properties.ReversalStatus.eq(SettledTransData.ReversalStatus.PENDING), SettledTransDataDao.Properties.Acquirer_id.eq(acquirer.getId())).list();
        return list == null || list.isEmpty() || this.deleteEntities(list);
    }

}
