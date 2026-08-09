package com.hitstudio.apps.sharednotebook.data.local.dao

import androidx.room.*
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteEntity
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteRevisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: NoteRevisionEntity)

    @Query("SELECT * FROM note_revisions WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getRevisionsForNote(noteId: String): Flow<List<NoteRevisionEntity>>
}