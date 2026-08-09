package com.hitstudio.apps.netbook.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hitstudio.apps.netbook.domain.model.Note;
import com.hitstudio.apps.netbook.domain.model.NoteVisibility;
import com.hitstudio.apps.netbook.domain.repository.NoteRepository;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class NoteEditorViewModel extends ViewModel {
    private final NoteRepository noteRepository;
    private final String noteId;
    private final MutableLiveData<String> title = new MutableLiveData<>("");
    private final MutableLiveData<String> body = new MutableLiveData<>("");
    private final MutableLiveData<NoteVisibility> visibility =
            new MutableLiveData<>(NoteVisibility.PRIVATE);
    private final MutableLiveData<Boolean> loading;
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Note loadedNote;

    @Inject
    public NoteEditorViewModel(NoteRepository noteRepository, SavedStateHandle savedStateHandle) {
        this.noteRepository = noteRepository;
        this.noteId = savedStateHandle.get("noteId");
        boolean existingNote = noteId != null && !"new".equals(noteId);
        this.loading = new MutableLiveData<>(existingNote);
        if (existingNote) loadNote();
    }

    public LiveData<String> getTitle() { return title; }
    public LiveData<String> getBody() { return body; }
    public LiveData<NoteVisibility> getVisibility() { return visibility; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getSaving() { return saving; }

    public void onTitleChange(String newTitle) { title.setValue(newTitle); }
    public void onBodyChange(String newBody) { body.setValue(newBody); }
    public void onVisibilityChange(NoteVisibility newVisibility) { visibility.setValue(newVisibility); }

    private void loadNote() {
        databaseExecutor.execute(() -> {
            try {
                Note note = noteRepository.getNoteById(noteId);
                if (note != null) {
                    loadedNote = note;
                    title.postValue(note.getTitle());
                    body.postValue(note.getBody());
                    visibility.postValue(note.getVisibility());
                }
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void saveNote(Runnable onSaved) {
        if (Boolean.TRUE.equals(saving.getValue())) return;
        saving.setValue(true);

        String currentTitle = valueOrEmpty(title.getValue());
        String currentBody = valueOrEmpty(body.getValue());
        NoteVisibility currentVisibility = visibility.getValue() == null
                ? NoteVisibility.PRIVATE : visibility.getValue();
        long now = System.currentTimeMillis();
        String id = noteId == null || "new".equals(noteId)
                ? UUID.randomUUID().toString() : noteId;
        Note note = new Note(
                id,
                currentVisibility,
                currentTitle,
                currentBody,
                loadedNote == null ? "device-1" : loadedNote.getCreatorId(),
                UUID.randomUUID().toString(),
                loadedNote == null ? now : loadedNote.getCreatedAt(),
                now,
                loadedNote != null && loadedNote.isDeleted()
        );

        databaseExecutor.execute(() -> {
            try {
                if (noteId == null || "new".equals(noteId)) {
                    noteRepository.insertNote(note);
                } else {
                    noteRepository.updateNote(note);
                }
                loadedNote = note;
                mainHandler.post(onSaved);
            } finally {
                saving.postValue(false);
            }
        });
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected void onCleared() {
        databaseExecutor.shutdown();
    }
}
