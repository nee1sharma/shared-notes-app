package com.hitstudio.apps.sharednotebook.di

import com.hitstudio.apps.sharednotebook.data.repository.NoteRepositoryImpl
import com.hitstudio.apps.sharednotebook.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl
    ): NoteRepository
}