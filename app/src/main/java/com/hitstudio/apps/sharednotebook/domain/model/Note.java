package com.hitstudio.apps.sharednotebook.domain.model;

import java.util.Objects;

public final class Note {
    private final String id;
    private final NoteVisibility visibility;
    private final String title;
    private final String body;
    private final String creatorId;
    private final String currentRevisionId;
    private final long createdAt;
    private final long updatedAt;
    private final boolean deleted;

    public Note(
            String id,
            NoteVisibility visibility,
            String title,
            String body,
            String creatorId,
            String currentRevisionId,
            long createdAt,
            long updatedAt,
            boolean deleted
    ) {
        this.id = id;
        this.visibility = visibility;
        this.title = title;
        this.body = body;
        this.creatorId = creatorId;
        this.currentRevisionId = currentRevisionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public String getId() { return id; }
    public NoteVisibility getVisibility() { return visibility; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCreatorId() { return creatorId; }
    public String getCurrentRevisionId() { return currentRevisionId; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public boolean isDeleted() { return deleted; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Note)) return false;
        Note note = (Note) other;
        return createdAt == note.createdAt
                && updatedAt == note.updatedAt
                && deleted == note.deleted
                && Objects.equals(id, note.id)
                && visibility == note.visibility
                && Objects.equals(title, note.title)
                && Objects.equals(body, note.body)
                && Objects.equals(creatorId, note.creatorId)
                && Objects.equals(currentRevisionId, note.currentRevisionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, visibility, title, body, creatorId, currentRevisionId,
                createdAt, updatedAt, deleted);
    }
}
