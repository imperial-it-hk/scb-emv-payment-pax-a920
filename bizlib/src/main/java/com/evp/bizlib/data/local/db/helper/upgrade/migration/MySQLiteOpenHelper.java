package com.evp.bizlib.data.local.db.helper.upgrade.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.evp.bizlib.data.local.db.dao.AcqIssuerRelationDao;
import com.evp.bizlib.data.local.db.dao.AcquirerDao;
import com.evp.bizlib.data.local.db.dao.CardBinDao;
import com.evp.bizlib.data.local.db.dao.CardRangeDao;
import com.evp.bizlib.data.local.db.dao.ClssTornLogDao;
import com.evp.bizlib.data.local.db.dao.DaoMaster;
import com.evp.bizlib.data.local.db.dao.EmvAidDao;
import com.evp.bizlib.data.local.db.dao.EmvCapkDao;
import com.evp.bizlib.data.local.db.dao.IssuerDao;
import com.evp.bizlib.data.local.db.dao.TransDataDao;
import com.evp.bizlib.data.local.db.dao.TransTotalDao;

import org.greenrobot.greendao.database.Database;

public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);
            }
        }, AcquirerDao.class, IssuerDao.class, AcqIssuerRelationDao.class, CardBinDao.class, CardRangeDao.class, ClssTornLogDao.class, EmvAidDao.class, EmvCapkDao.class, TransDataDao.class, TransTotalDao.class);
    }
}
