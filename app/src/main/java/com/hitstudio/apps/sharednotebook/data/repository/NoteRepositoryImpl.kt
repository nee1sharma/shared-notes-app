package com.hitstudio.apps.sharednotebook.data.repository

import com.hitstudio.apps.sharednotebook.data.local.dao.NoteDao
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteEntity
import com.hitstudio.apps.sharednotebook.domain.model.Note
import com.hitstudio.apps.sharednotebook.domain.model.NoteVisibility
import com.hitstudio.apps.sharednotebook.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            visibility = NoteVisibility.valueOf(visibility),
            title = title,
            body = body,
            creatorId = creatorId,
            currentRevisionId = currentRevisionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            visibility = visibility.name,
            title = title,
            body = body,
            creatorId = creatorId,
            currentRevisionId = currentRevisionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted
        )
    }
}