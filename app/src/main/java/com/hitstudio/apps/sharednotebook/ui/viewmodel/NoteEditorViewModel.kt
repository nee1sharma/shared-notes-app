package com.hitstudio.apps.sharednotebook.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hitstudio.apps.sharednotebook.domain.model.Note
import com.hitstudio.apps.sharednotebook.domain.model.NoteVisibility
import com.hitstudio.apps.sharednotebook.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body.asStateFlow()

    private val _visibility = MutableStateFlow(NoteVisibility.PRIVATE)
    val visibility: StateFlow<NoteVisibility> = _visibility.asStateFlow()

    init {
        if (noteId != null && noteId != "new") {
            viewModelScope.launch {
                noteRepository.getNoteById(noteId)?.let { note ->
                    _title.value = note.title
                    _body.value = note.body
                    _visibility.value = note.visibility
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onBodyChange(newBody: String) {
        _body.value = newBody
    }

    fun onVisibilityChange(newVisibility: NoteVisibility) {
        _visibility.value = newVisibility
    }

    fun saveNote(onSaved: () -> Unit) {
        viewModelScope.launch {
            val note = Note(
                id = noteId?.takeIf { it != "new" } ?: UUID.randomUUID().toString(),
                visibility = _visibility.value,
                title = _title.value,
                body = _body.value,
                creatorId = "device-1", // Mock for now
                currentRevisionId = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            if (noteId == null || noteId == "new") {
                noteRepository.insertNote(note)
            } else {
                noteRepository.updateNote(note)
            }
            onSaved()
        }
    }
}