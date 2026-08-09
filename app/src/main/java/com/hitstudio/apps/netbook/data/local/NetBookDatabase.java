package com.hitstudio.apps.netbook.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.hitstudio.apps.netbook.data.local.dao.NoteDao;
import com.hitstudio.apps.netbook.data.local.entity.ActivityEventEntity;
import com.hitstudio.apps.netbook.data.local.entity.NoteEntity;
import com.hitstudio.apps.netbook.data.local.entity.NoteRevisionEntity;

@Database(
        entities = {NoteEntity.class, NoteRevisionEntity.class, ActivityEventEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class NetBookDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();
}
