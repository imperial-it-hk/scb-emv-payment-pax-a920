package com.evp.bizlib.data.local.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.evp.bizlib.data.entity.ClssTornLog;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "clsstornlog".
*/
public class ClssTornLogDao extends AbstractDao<ClssTornLog, Long> {

    public static final String TABLENAME = "clsstornlog";

    /**
     * Properties of entity ClssTornLog.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "id");
        public final static Property AucPan = new Property(1, String.class, "aucPan", false, "AUC_PAN");
        public final static Property PanLen = new Property(2, int.class, "panLen", false, "PAN_LEN");
        public final static Property PanSeqFlg = new Property(3, boolean.class, "panSeqFlg", false, "PAN_SEQ_FLG");
        public final static Property PanSeq = new Property(4, byte.class, "panSeq", false, "PAN_SEQ");
        public final static Property AucTornData = new Property(5, String.class, "aucTornData", false, "AUC_TORN_DATA");
        public final static Property TornDataLen = new Property(6, int.class, "tornDataLen", false, "TORN_DATA_LEN");
    }


    public ClssTornLogDao(DaoConfig config) {
        super(config);
    }
    
    public ClssTornLogDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"clsstornlog\" (" + //
                "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"AUC_PAN\" TEXT NOT NULL ," + // 1: aucPan
                "\"PAN_LEN\" INTEGER NOT NULL ," + // 2: panLen
                "\"PAN_SEQ_FLG\" INTEGER NOT NULL ," + // 3: panSeqFlg
                "\"PAN_SEQ\" INTEGER NOT NULL ," + // 4: panSeq
                "\"AUC_TORN_DATA\" TEXT NOT NULL ," + // 5: aucTornData
                "\"TORN_DATA_LEN\" INTEGER NOT NULL );"); // 6: tornDataLen
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"clsstornlog\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ClssTornLog entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getAucPan());
        stmt.bindLong(3, entity.getPanLen());
        stmt.bindLong(4, entity.getPanSeqFlg() ? 1L: 0L);
        stmt.bindLong(5, entity.getPanSeq());
        stmt.bindString(6, entity.getAucTornData());
        stmt.bindLong(7, entity.getTornDataLen());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ClssTornLog entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getAucPan());
        stmt.bindLong(3, entity.getPanLen());
        stmt.bindLong(4, entity.getPanSeqFlg() ? 1L: 0L);
        stmt.bindLong(5, entity.getPanSeq());
        stmt.bindString(6, entity.getAucTornData());
        stmt.bindLong(7, entity.getTornDataLen());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ClssTornLog readEntity(Cursor cursor, int offset) {
        ClssTornLog entity = new ClssTornLog( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // aucPan
            cursor.getInt(offset + 2), // panLen
            cursor.getShort(offset + 3) != 0, // panSeqFlg
            (byte) cursor.getShort(offset + 4), // panSeq
            cursor.getString(offset + 5), // aucTornData
            cursor.getInt(offset + 6) // tornDataLen
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ClssTornLog entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAucPan(cursor.getString(offset + 1));
        entity.setPanLen(cursor.getInt(offset + 2));
        entity.setPanSeqFlg(cursor.getShort(offset + 3) != 0);
        entity.setPanSeq((byte) cursor.getShort(offset + 4));
        entity.setAucTornData(cursor.getString(offset + 5));
        entity.setTornDataLen(cursor.getInt(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ClssTornLog entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ClssTornLog entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ClssTornLog entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}