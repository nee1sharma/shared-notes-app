package com.hitstudio.apps.sharednotebook.di;

import android.content.Context;

import androidx.room.Room;

import com.hitstudio.apps.sharednotebook.data.local.SharedNoteBookDatabase;
import com.hitstudio.apps.sharednotebook.data.local.dao.NoteDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {
    private DatabaseModule() {
    }

    @Provides
    @Singleton
    public static SharedNoteBookDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                SharedNoteBookDatabase.class,
                "shared_notebook.db"
        ).build();
    }

    @Provides
    public static NoteDao provideNoteDao(SharedNoteBookDatabase database) {
        return database.noteDao();
    }
}
