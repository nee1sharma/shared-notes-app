package com.hitstudio.apps.netbook.domain.repository;

import androidx.lifecycle.LiveData;

import com.hitstudio.apps.netbook.domain.model.Note;

import java.util.List;

public interface NoteRepository {
    LiveData<List<Note>> getAllNotes();
    Note getNoteById(String id);
    void insertNote(Note note);
    void updateNote(Note note);
    void deleteNote(Note note);
}
