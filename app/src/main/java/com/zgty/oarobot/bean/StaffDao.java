package com.zgty.oarobot.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "STAFF".
*/
public class StaffDao extends AbstractDao<Staff, Void> {

    public static final String TABLENAME = "STAFF";

    /**
     * Properties of entity Staff.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", false, "ID");
        public final static Property Name_user = new Property(1, String.class, "name_user", false, "NAME");
        public final static Property Id_user = new Property(2, String.class, "id_user", false, "ID_USER");
        public final static Property Id_clerk = new Property(3, String.class, "id_clerk", false, "ID_CLERK");
        public final static Property Name_part = new Property(4, String.class, "name_part", false, "NAME_PART");
        public final static Property Name_position = new Property(5, String.class, "name_position", false, "NAME_POSITION");
        public final static Property Call_num = new Property(6, String.class, "call_num", false, "CALL_NUM");
        public final static Property User_type = new Property(7, String.class, "user_type", false, "USER_TYPE");
        public final static Property IsRecordFace = new Property(8, Boolean.class, "isRecordFace", false, "HAS_FACE");
    }


    public StaffDao(DaoConfig config) {
        super(config);
    }
    
    public StaffDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"STAFF\" (" + //
                "\"ID\" TEXT," + // 0: id
                "\"NAME\" TEXT," + // 1: name_user
                "\"ID_USER\" TEXT," + // 2: id_user
                "\"ID_CLERK\" TEXT," + // 3: id_clerk
                "\"NAME_PART\" TEXT," + // 4: name_part
                "\"NAME_POSITION\" TEXT," + // 5: name_position
                "\"CALL_NUM\" TEXT," + // 6: call_num
                "\"USER_TYPE\" TEXT," + // 7: user_type
                "\"HAS_FACE\" INTEGER);"); // 8: isRecordFace
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"STAFF\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Staff entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String name_user = entity.getName_user();
        if (name_user != null) {
            stmt.bindString(2, name_user);
        }
 
        String id_user = entity.getId_user();
        if (id_user != null) {
            stmt.bindString(3, id_user);
        }
 
        String id_clerk = entity.getId_clerk();
        if (id_clerk != null) {
            stmt.bindString(4, id_clerk);
        }
 
        String name_part = entity.getName_part();
        if (name_part != null) {
            stmt.bindString(5, name_part);
        }
 
        String name_position = entity.getName_position();
        if (name_position != null) {
            stmt.bindString(6, name_position);
        }
 
        String call_num = entity.getCall_num();
        if (call_num != null) {
            stmt.bindString(7, call_num);
        }
 
        String user_type = entity.getUser_type();
        if (user_type != null) {
            stmt.bindString(8, user_type);
        }
 
        Boolean isRecordFace = entity.getIsRecordFace();
        if (isRecordFace != null) {
            stmt.bindLong(9, isRecordFace ? 1L: 0L);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Staff entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
 
        String name_user = entity.getName_user();
        if (name_user != null) {
            stmt.bindString(2, name_user);
        }
 
        String id_user = entity.getId_user();
        if (id_user != null) {
            stmt.bindString(3, id_user);
        }
 
        String id_clerk = entity.getId_clerk();
        if (id_clerk != null) {
            stmt.bindString(4, id_clerk);
        }
 
        String name_part = entity.getName_part();
        if (name_part != null) {
            stmt.bindString(5, name_part);
        }
 
        String name_position = entity.getName_position();
        if (name_position != null) {
            stmt.bindString(6, name_position);
        }
 
        String call_num = entity.getCall_num();
        if (call_num != null) {
            stmt.bindString(7, call_num);
        }
 
        String user_type = entity.getUser_type();
        if (user_type != null) {
            stmt.bindString(8, user_type);
        }
 
        Boolean isRecordFace = entity.getIsRecordFace();
        if (isRecordFace != null) {
            stmt.bindLong(9, isRecordFace ? 1L: 0L);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public Staff readEntity(Cursor cursor, int offset) {
        Staff entity = new Staff( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name_user
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // id_user
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // id_clerk
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // name_part
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // name_position
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // call_num
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // user_type
            cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0 // isRecordFace
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Staff entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setName_user(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setId_user(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setId_clerk(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setName_part(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setName_position(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setCall_num(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setUser_type(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setIsRecordFace(cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0);
     }
    
    @Override
    protected final Void updateKeyAfterInsert(Staff entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(Staff entity) {
        return null;
    }

    @Override
    public boolean hasKey(Staff entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}