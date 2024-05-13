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

import com.evp.bizlib.data.local.db.dao.DaoSession;
import com.evp.commonlib.utils.LogUtils;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * BaseDaoHelper
 * @param <T> The data table entity
 */
public class BaseDaoHelper<T> {
    protected static final String TAG = "BaseDaoHelper";
    private final Class<T> entityClass;

    public BaseDaoHelper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    static DaoSession getDaoSession() {
        return DaoManager.getInstance().getDaoSession();
    }

    protected final AbstractDao<T, Long> getDao() {
        return (AbstractDao<T, Long>) getDaoSession().getDao(entityClass);
    }

    protected final QueryBuilder<T> getNoSessionQuery() {
        this.getDao().detachAll();
        return getDao().queryBuilder();
    }

    protected final Database getDatabase() {
        return getDao().getDatabase();
    }

    /**
     * insert record of corresponding Entity
     * @param entity entity
     * @return insert result
     */
    public final boolean insert(T entity) {
        boolean success;
        try {
            long insert = this.getDao().insertOrReplace(entity);
            success = (int) insert != -1;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }
        return success;
    }

    /**
     * insert record of corresponding Entity
     * @param entityList entityList
     * @return insert result
     */
    public final boolean insert(List entityList) {
        boolean success;
        try {
            this.getDao().insertOrReplaceInTx(entityList);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }

        return success;
    }

    /**
     * update record of corresponding Entity
     * @param entity entity
     * @return update result
     */
    public final boolean update(T entity) {
        boolean success;
        try {
            this.getDao().update(entity);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }

        return success;
    }

    /**
     * update record of corresponding Entity
     * @param entityList corresponding Entity
     * @return update result
     */
    public final boolean update(List entityList) {
        boolean success;
        try {
            this.getDao().updateInTx(entityList);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }

        return success;
    }

    /**
     * delete record of corresponding Entity
     * @param entity  corresponding Entity
     * @return delete result
     */
    public final boolean delete(T entity) {
        boolean success;
        try {
            this.getDao().delete(entity);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }

        return success;
    }

    /**
     * delete all record of corresponding Entity
     * @return delete result
     */
    public final boolean deleteAll() {
        boolean success;
        try {
            this.getDao().deleteAll();
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }
        return success;
    }

    /**
     * delete record of corresponding Entity by key
     * @param key key
     * @return delete result
     */
    public final boolean deleteByKey(long key) {
        boolean success;
        try {
            this.getDao().deleteByKey(key);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }

        return success;
    }

    /**
     * delete record of corresponding Entity by keyList
     * @param keyList keyList
     * @return delete result
     */
    public final boolean deleteByKeyList(List keyList) {
        boolean success;
        try {
            this.getDao().deleteByKeyInTx(keyList);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }
        return success;
    }

    /**
     * delete record of corresponding Entity
     * @param entityList record of corresponding Entity
     * @return delete result
     */
    public final boolean deleteEntities(List entityList) {
        boolean success;
        try {
            this.getDao().deleteInTx(entityList);
            success = true;
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            success = false;
        }
        return success;
    }

    /**
     * query record of corresponding Entity by key
     * @param key key
     * @return corresponding Entity record
     */
    public final T loadByKey(long key) {
        return this.getDao().load(key);
    }

    /**
     * query record of corresponding Entity by rowId
     * @param rowId rowId
     * @return corresponding Entity record
     */
    public final T loadByRowId(long rowId) {
        return this.getDao().loadByRowId(rowId);
    }

    /**
     * query all record of corresponding Entity
     * @return query result
     */
    public final List<T> loadAll() {
        return this.getDao().loadAll();
    }
}
