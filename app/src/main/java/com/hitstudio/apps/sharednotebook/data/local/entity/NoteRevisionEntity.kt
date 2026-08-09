package com.hitstudio.apps.sharednotebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_revisions")
data class NoteRevisionEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val parentRevisionIds: String, // Comma-separated list of IDs
    val encryptedSnapshot: String, // Base64 or similar
    val authorDeviceId: String,
    val createdAt: Long
)