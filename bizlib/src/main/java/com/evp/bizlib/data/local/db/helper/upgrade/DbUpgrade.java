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
 * 20190621  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.bizlib.data.local.db.helper.upgrade;

import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;

import com.evp.commonlib.utils.LogUtils;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DbUpgrade {
    public static final String TAG = "DbUpgrade";
    public static void upgrade(Database db, int oldVersion, int newVersion, String packagePath){
        try {
            Class<?> cls = Class.forName(packagePath + ".Upgrade" + oldVersion + "To" + newVersion);
            DbUpgrade upgrade = (DbUpgrade) cls.newInstance();
            upgrade.upgrade(db);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LogUtils.e(TAG,e);
        }
    }

    protected abstract void upgrade(Database db);

    @SafeVarargs
    protected static void upgradeTable(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) throws SQLException {
        db.beginTransaction();
        try {
            for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
                DaoConfig daoConfig = new DaoConfig(db, daoClass);
                String tableName = daoConfig.tablename;
                //Rename table
                String tempTableName = tableName + "_temp";
                String sql = "ALTER TABLE " + tableName + " RENAME TO " + tempTableName;
                db.execSQL(sql);

                //Create table
                createTable(db, false, daoClass);

                //Load data

                // get all columns from tempTable, take careful to use the columns list
                List<TableInfo> newTableInfos = TableInfo.getTableInfo(db, tableName);
                List<TableInfo> tempTableInfos = TableInfo.getTableInfo(db, tempTableName);
                ArrayList<String> selectColumns = new ArrayList<>(newTableInfos.size());
                ArrayList<String> intoColumns = new ArrayList<>(newTableInfos.size());
                for (TableInfo tableInfo : tempTableInfos) {
                    if (newTableInfos.contains(tableInfo)) {
                        String column = '`' + tableInfo.name + '`';
                        intoColumns.add(column);
                        selectColumns.add(column);
                    }
                }
                // NOT NULL columns list
                for (TableInfo tableInfo : newTableInfos) {
                    if (tableInfo.notnull && !tempTableInfos.contains(tableInfo)) {
                        String column = '`' + tableInfo.name + '`';
                        intoColumns.add(column);

                        String value;
                        if (tableInfo.dfltValue != null) {
                            value = "'" + tableInfo.dfltValue + "' AS ";
                        } else {
                            value = "'' AS ";
                        }
                        selectColumns.add(value + column);
                    }
                }

                if (!intoColumns.isEmpty()) {
                    String insertTableStringBuilder = "REPLACE INTO " + tableName + " (" +
                            TextUtils.join(",", intoColumns) +
                            ") SELECT " +
                            TextUtils.join(",", selectColumns) +
                            " FROM " + tempTableName + ";";
                    db.execSQL(insertTableStringBuilder);
                    LogUtils.d(TAG, "【Restore data】 to " + tableName);
                }

                //Drop temp table
                sql = "DROP TABLE IF EXISTS " + tempTableName;
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            LogUtils.e(TAG,e);
        }finally {
            db.endTransaction();
        }

    }

    private static void createTable(Database db, boolean ifNotExists, Class<? extends AbstractDao<?, ?>> daoClasses) {
        try {
            Method method = daoClasses.getDeclaredMethod("createTable", Database.class, boolean.class);
            method.invoke(null, db, ifNotExists);
        } catch (Exception e) {
            LogUtils.e(TAG,"",e);
        }
    }

    private static String[] getColumnNames(Database db, String tableName) {
        String[] columnNames = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * from " + tableName, null);
            if (cursor != null) {
                columnNames = cursor.getColumnNames();
            }
        } catch (Exception e) {
            LogUtils.w(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return columnNames;
    }

    private static class TableInfo {
        int cid;
        String name;
        String type;
        boolean notnull;
        String dfltValue;
        boolean pk;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) return false;
            TableInfo tableInfo = (TableInfo) o;
            return Objects.equals(name, tableInfo.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "TableInfo{" +
                    "cid=" + cid +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", notnull=" + notnull +
                    ", dfltValue='" + dfltValue + '\'' +
                    ", pk=" + pk +
                    '}';
        }

        private static List<TableInfo> getTableInfo(Database db, String tableName) {
            String sql = "PRAGMA table_info(" + tableName + ")";
            LogUtils.d(TAG,sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor == null) {
                return new ArrayList<>();
            }
            TableInfo tableInfo;
            List<TableInfo> tableInfos = new ArrayList<>();
            while (cursor.moveToNext()) {
                tableInfo = new TableInfo();
                tableInfo.cid = cursor.getInt(0);
                tableInfo.name = cursor.getString(1);
                tableInfo.type = cursor.getString(2);
                tableInfo.notnull = cursor.getInt(3) == 1;
                tableInfo.dfltValue = cursor.getString(4);
                tableInfo.pk = cursor.getInt(5) == 1;
                tableInfos.add(tableInfo);
            }
            cursor.close();
            return tableInfos;
        }
    }
}
