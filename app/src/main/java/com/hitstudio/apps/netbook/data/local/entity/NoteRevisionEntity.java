package com.hitstudio.apps.netbook.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_revisions")
public final class NoteRevisionEntity {
    @PrimaryKey @NonNull private final String id;
    @NonNull private final String noteId;
    @NonNull private final String parentRevisionIds;
    @NonNull private final String encryptedSnapshot;
    @NonNull private final String authorDeviceId;
    private final long createdAt;

    public NoteRevisionEntity(
            @NonNull String id,
            @NonNull String noteId,
            @NonNull String parentRevisionIds,
            @NonNull String encryptedSnapshot,
            @NonNull String authorDeviceId,
            long createdAt
    ) {
        this.id = id;
        this.noteId = noteId;
        this.parentRevisionIds = parentRevisionIds;
        this.encryptedSnapshot = encryptedSnapshot;
        this.authorDeviceId = authorDeviceId;
        this.createdAt = createdAt;
    }

    @NonNull public String getId() { return id; }
    @NonNull public String getNoteId() { return noteId; }
    @NonNull public String getParentRevisionIds() { return parentRevisionIds; }
    @NonNull public String getEncryptedSnapshot() { return encryptedSnapshot; }
    @NonNull public String getAuthorDeviceId() { return authorDeviceId; }
    public long getCreatedAt() { return createdAt; }
}
