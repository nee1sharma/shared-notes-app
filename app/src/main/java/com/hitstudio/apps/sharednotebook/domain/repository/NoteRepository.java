package com.hitstudio.apps.sharednotebook.domain.repository;

import androidx.lifecycle.LiveData;

import com.hitstudio.apps.sharednotebook.domain.model.Note;

import java.util.List;

public interface NoteRepository {
    LiveData<List<Note>> getAllNotes();
    Note getNoteById(String id);
    void insertNote(Note note);
    void updateNote(Note note);
    void deleteNote(Note note);
}
