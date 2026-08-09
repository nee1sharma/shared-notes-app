package com.hitstudio.apps.sharednotebook.di

import android.content.Context
import androidx.room.Room
import com.hitstudio.apps.sharednotebook.data.local.SharedNoteBookDatabase
import com.hitstudio.apps.sharednotebook.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SharedNoteBookDatabase {
        return Room.databaseBuilder(
            context,
            SharedNoteBookDatabase::class.java,
            "shared_notebook.db"
        ).build()
    }

    @Provides
    fun provideNoteDao(database: SharedNoteBookDatabase): NoteDao {
        return database.noteDao()
    }
}