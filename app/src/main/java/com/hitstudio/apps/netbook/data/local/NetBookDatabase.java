package com.hitstudio.apps.netbook.data.local;

import androidx.room.Database;
import androidx.room.migration.Migration;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hitstudio.apps.netbook.data.local.dao.NoteDao;
import com.hitstudio.apps.netbook.data.local.entity.ActivityEventEntity;
import com.hitstudio.apps.netbook.data.local.entity.NoteEntity;
import com.hitstudio.apps.netbook.data.local.entity.NoteRevisionEntity;

@Database(
        entities = {NoteEntity.class, NoteRevisionEntity.class, ActivityEventEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class NetBookDatabase extends RoomDatabase {
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN parentRevisionId TEXT NOT NULL DEFAULT ''");
        }
    };

    public abstract NoteDao noteDao();
}
