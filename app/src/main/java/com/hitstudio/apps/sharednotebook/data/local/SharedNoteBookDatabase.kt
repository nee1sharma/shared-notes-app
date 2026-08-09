package com.hitstudio.apps.sharednotebook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hitstudio.apps.sharednotebook.data.local.dao.NoteDao
import com.hitstudio.apps.sharednotebook.data.local.entity.ActivityEventEntity
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteEntity
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteRevisionEntity

@Database(
    entities = [
        NoteEntity::class,
        NoteRevisionEntity::class,
        ActivityEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SharedNoteBookDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}