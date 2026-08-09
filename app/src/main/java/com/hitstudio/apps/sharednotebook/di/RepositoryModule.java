package com.hitstudio.apps.sharednotebook.di;

import com.hitstudio.apps.sharednotebook.data.repository.NoteRepositoryImpl;
import com.hitstudio.apps.sharednotebook.domain.repository.NoteRepository;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Binds
    @Singleton
    public abstract NoteRepository bindNoteRepository(NoteRepositoryImpl implementation);
}
