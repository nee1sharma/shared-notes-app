package com.hitstudio.apps.netbook.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public final class NoteEntity {
    @PrimaryKey
    @NonNull
    private final String id;
    @NonNull
    private final String visibility;
    @NonNull
    private final String title;
    @NonNull
    private final String body;
    @NonNull
    private final String creatorId;
    @NonNull
    private final String currentRevisionId;
    @NonNull
    private final String parentRevisionId;
    private final long createdAt;
    private final long updatedAt;
    private final boolean isDeleted;

    public NoteEntity(
            @NonNull String id,
            @NonNull String visibility,
            @NonNull String title,
            @NonNull String body,
            @NonNull String creatorId,
            @NonNull String currentRevisionId,
            @NonNull String parentRevisionId,
            long createdAt,
            long updatedAt,
            boolean isDeleted
    ) {
        this.id = id;
        this.visibility = visibility;
        this.title = title;
        this.body = body;
        this.creatorId = creatorId;
        this.currentRevisionId = currentRevisionId;
        this.parentRevisionId = parentRevisionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    @NonNull public String getId() { return id; }
    @NonNull public String getVisibility() { return visibility; }
    @NonNull public String getTitle() { return title; }
    @NonNull public String getBody() { return body; }
    @NonNull public String getCreatorId() { return creatorId; }
    @NonNull public String getCurrentRevisionId() { return currentRevisionId; }
    @NonNull public String getParentRevisionId() { return parentRevisionId; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public boolean isDeleted() { return isDeleted; }
}
