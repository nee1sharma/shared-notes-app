package com.hitstudio.apps.sharednotebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val visibility: String, // "PRIVATE" or "SHARED"
    val title: String,
    val body: String,
    val creatorId: String,
    val currentRevisionId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)