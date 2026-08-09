package com.hitstudio.apps.netbook.di;

import android.content.Context;

import androidx.room.Room;

import com.hitstudio.apps.netbook.data.local.NetBookDatabase;
import com.hitstudio.apps.netbook.data.local.dao.NoteDao;

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
    public static NetBookDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                NetBookDatabase.class,
                "netbook.db"
        ).addMigrations(NetBookDatabase.MIGRATION_1_2).build();
    }

    @Provides
    public static NoteDao provideNoteDao(NetBookDatabase database) {
        return database.noteDao();
    }
}
