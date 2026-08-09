package com.hitstudio.apps.netbook.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.hitstudio.apps.netbook.domain.model.Note;
import com.hitstudio.apps.netbook.domain.repository.NoteRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class NoteViewModel extends ViewModel {
    private final NoteRepository noteRepository;
    private final LiveData<List<Note>> notes;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public NoteViewModel(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
        this.notes = noteRepository.getAllNotes();
    }

    public LiveData<List<Note>> getNotes() {
        return notes;
    }

    public void deleteNote(Note note) {
        databaseExecutor.execute(() -> noteRepository.deleteNote(note));
    }

    @Override
    protected void onCleared() {
        databaseExecutor.shutdown();
    }
}
