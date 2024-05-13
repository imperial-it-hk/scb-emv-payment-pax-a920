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
package com.evp.bizlib.data.local.db.helper.upgrade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.evp.bizlib.data.local.db.dao.DaoMaster;
import com.evp.commonlib.utils.LogUtils;

import net.sqlcipher.database.SQLiteOpenHelper;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.EncryptedDatabase;
/**
 * encrypt text database SQLiteOpenHelper
 */
public class MyEncryptedSQLiteOpenHelper extends DaoMaster.OpenHelper {
    private static final String TAG = "MyEncryptedSQLiteOpenHe";
    private final Context context;
    private final String name;
    private static final String UPGRADER_PATH = "com.pax.bizlib.data.local.db.helper.upgrade.history";

    public MyEncryptedSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        this.context = context;
        this.name = name;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        for (int i = oldVersion;i<newVersion;i++){
            DbUpgrade.upgrade(db,i,i+1,UPGRADER_PATH);
        }
        LogUtils.e(TAG, "upgrade run success");
    }

    @Override
    public Database getEncryptedWritableDb(String password) {
        int version = DaoMaster.SCHEMA_VERSION;
        MyEncryptedHelper encryptedHelper = new MyEncryptedHelper(this.context, this.name, version, true);
        return encryptedHelper.wrap(encryptedHelper.getWritableDatabase(password));
    }

    private final class MyEncryptedHelper extends SQLiteOpenHelper {

        public MyEncryptedHelper(Context context, String name, int version, Boolean loadLibs) {
            super(context, name, null, version);
            if (loadLibs) {
                net.sqlcipher.database.SQLiteDatabase.loadLibs(context);
            }
        }

        @Override
        public void onCreate(net.sqlcipher.database.SQLiteDatabase sqLiteDatabase) {
            MyEncryptedSQLiteOpenHelper.this.onCreate(this.wrap(sqLiteDatabase));
        }

        @Override
        public void onUpgrade(net.sqlcipher.database.SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            MyEncryptedSQLiteOpenHelper.this.onUpgrade(this.wrap(sqLiteDatabase), oldVersion, newVersion);
        }

        @Override
        public void onOpen(net.sqlcipher.database.SQLiteDatabase db) {
            MyEncryptedSQLiteOpenHelper.this.onOpen(this.wrap(db));
        }

        public final Database wrap(@Nullable net.sqlcipher.database.SQLiteDatabase sqLiteDatabase) {
            return (Database) (new EncryptedDatabase(sqLiteDatabase));
        }
    }
}
