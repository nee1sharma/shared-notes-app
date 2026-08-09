package com.hitstudio.apps.sharednotebook.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activity_events")
public final class ActivityEventEntity {
    @PrimaryKey @NonNull private final String id;
    @NonNull private final String type;
    private final long occurredAt;
    @NonNull private final String originClientId;
    @Nullable private final String actorMemberId;
    @Nullable private final String targetId;
    @NonNull private final String outcome;
    @Nullable private final String encryptedMetadata;
    @Nullable private final Long expiresAt;

    public ActivityEventEntity(
            @NonNull String id,
            @NonNull String type,
            long occurredAt,
            @NonNull String originClientId,
            @Nullable String actorMemberId,
            @Nullable String targetId,
            @NonNull String outcome,
            @Nullable String encryptedMetadata,
            @Nullable Long expiresAt
    ) {
        this.id = id;
        this.type = type;
        this.occurredAt = occurredAt;
        this.originClientId = originClientId;
        this.actorMemberId = actorMemberId;
        this.targetId = targetId;
        this.outcome = outcome;
        this.encryptedMetadata = encryptedMetadata;
        this.expiresAt = expiresAt;
    }

    @NonNull public String getId() { return id; }
    @NonNull public String getType() { return type; }
    public long getOccurredAt() { return occurredAt; }
    @NonNull public String getOriginClientId() { return originClientId; }
    @Nullable public String getActorMemberId() { return actorMemberId; }
    @Nullable public String getTargetId() { return targetId; }
    @NonNull public String getOutcome() { return outcome; }
    @Nullable public String getEncryptedMetadata() { return encryptedMetadata; }
    @Nullable public Long getExpiresAt() { return expiresAt; }
}
