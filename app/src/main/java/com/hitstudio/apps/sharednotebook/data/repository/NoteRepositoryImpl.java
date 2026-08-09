package com.hitstudio.apps.sharednotebook.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.hitstudio.apps.sharednotebook.data.local.dao.NoteDao;
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteEntity;
import com.hitstudio.apps.sharednotebook.domain.model.Note;
import com.hitstudio.apps.sharednotebook.domain.model.NoteVisibility;
import com.hitstudio.apps.sharednotebook.domain.repository.NoteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public final class NoteRepositoryImpl implements NoteRepository {
    private final NoteDao noteDao;

    @Inject
    public NoteRepositoryImpl(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    @Override
    public LiveData<List<Note>> getAllNotes() {
        return Transformations.map(noteDao.getAllNotes(), entities -> {
            if (entities == null) return Collections.emptyList();
            List<Note> notes = new ArrayList<>(entities.size());
            for (NoteEntity entity : entities) notes.add(toDomain(entity));
            return notes;
        });
    }

    @Override public Note getNoteById(String id) {
        NoteEntity entity = noteDao.getNoteById(id);
        return entity == null ? null : toDomain(entity);
    }

    @Override public void insertNote(Note note) { noteDao.insertNote(toEntity(note)); }
    @Override public void updateNote(Note note) { noteDao.updateNote(toEntity(note)); }
    @Override public void deleteNote(Note note) { noteDao.deleteNote(toEntity(note)); }

    private static Note toDomain(NoteEntity entity) {
        return new Note(
                entity.getId(),
                NoteVisibility.valueOf(entity.getVisibility()),
                entity.getTitle(),
                entity.getBody(),
                entity.getCreatorId(),
                entity.getCurrentRevisionId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted()
        );
    }

    private static NoteEntity toEntity(Note note) {
        return new NoteEntity(
                note.getId(),
                note.getVisibility().name(),
                note.getTitle(),
                note.getBody(),
                note.getCreatorId(),
                note.getCurrentRevisionId(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.isDeleted()
        );
    }
}
