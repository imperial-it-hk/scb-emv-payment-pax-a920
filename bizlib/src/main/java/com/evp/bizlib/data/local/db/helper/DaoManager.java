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

import com.evp.bizlib.BuildConfig;
import com.evp.bizlib.data.local.db.dao.DaoMaster;
import com.evp.bizlib.data.local.db.dao.DaoSession;
import com.evp.bizlib.data.local.db.helper.upgrade.MyEncryptedSQLiteOpenHelper;
import com.evp.bizlib.data.local.db.helper.upgrade.migration.MySQLiteOpenHelper;
import com.evp.commonlib.application.BaseApplication;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

/**
 * GreenDao Database Manager
 */
public class DaoManager {
    private DaoMaster.OpenHelper openHelper;
    private Database database;
    private DaoSession daoSession;
    private DaoManager() {
    }

    private static class LazyHolder {
        public static final DaoManager INSTANCE = new DaoManager();
    }

    public static DaoManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void init(){
        if (BuildConfig.RELEASE) {
            openHelper = new MyEncryptedSQLiteOpenHelper(BaseApplication.getAppContext(), "data.db", null);
            database = openHelper.getEncryptedWritableDb(BuildConfig.DATABASE_PWD);
        } else {
            openHelper = new MySQLiteOpenHelper(BaseApplication.getAppContext(), "data.db", null);
            database = openHelper.getWritableDb();
        }
        DaoMaster daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
        QueryBuilder.LOG_SQL = !BuildConfig.RELEASE;
        QueryBuilder.LOG_VALUES = !BuildConfig.RELEASE;
    }


    public DaoSession getDaoSession() {
        return daoSession;
    }

    public DaoMaster.OpenHelper getOpenHelper() {
        return openHelper;
    }

    public Database getDatabase() {
        return database;
    }

}
