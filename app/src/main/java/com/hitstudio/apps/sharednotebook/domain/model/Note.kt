package com.hitstudio.apps.sharednotebook.domain.model

enum class NoteVisibility {
    PRIVATE, SHARED
}

data class Note(
    val id: String,
    val visibility: NoteVisibility,
    val title: String,
    val body: String,
    val creatorId: String,
    val currentRevisionId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)