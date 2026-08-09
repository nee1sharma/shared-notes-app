package com.hitstudio.apps.sharednotebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_events")
data class ActivityEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val occurredAt: Long,
    val originClientId: String,
    val actorMemberId: String?,
    val targetId: String?,
    val outcome: String,
    val encryptedMetadata: String?,
    val expiresAt: Long?
)