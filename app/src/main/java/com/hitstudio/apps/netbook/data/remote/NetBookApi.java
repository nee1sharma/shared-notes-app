package com.hitstudio.apps.netbook.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/** The authenticated Android control-plane protocol advertised through mDNS. */
public interface NetBookApi {
    @POST("/api/v1/mobile/registration")
    Call<RegistrationResponse> requestRegistration(@Body RegistrationRequest request);

    @POST("/api/v1/mobile/presence/heartbeat")
    Call<Void> sendHeartbeat(@Header("Authorization") String authorization, @Body HeartbeatRequest request);

    @POST("/api/v1/mobile/sync")
    Call<SyncResponse> synchronize(@Header("Authorization") String authorization, @Body SyncRequest request);

    @GET("/api/v1/mobile/devices")
    Call<List<DeviceView>> getDevices(@Header("Authorization") String authorization);

    class RegistrationRequest {
        public String installationId;
        public String memberName;
        public String deviceName;
        public String email;
        public String publicKey;
        public String appName;
        public String modelName;
        public String platform;

        public RegistrationRequest(
                String installationId,
                String memberName,
                String deviceName,
                String email,
                String publicKey,
                String appName,
                String modelName,
                String platform
        ) {
            this.installationId = installationId;
            this.memberName = memberName;
            this.deviceName = deviceName;
            this.email = email;
            this.publicKey = publicKey;
            this.appName = appName;
            this.modelName = modelName;
            this.platform = platform;
        }
    }

    class RegistrationResponse {
        public String deviceId;
        public String householdId;
        public String status;
        public String accessToken;
    }

    class HeartbeatRequest {
        public long timestamp;

        public HeartbeatRequest(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    class SyncRequest {
        public List<SyncNote> notes;
        public long lastSynchronizedAt;

        public SyncRequest(List<SyncNote> notes, long lastSynchronizedAt) {
            this.notes = notes;
            this.lastSynchronizedAt = lastSynchronizedAt;
        }
    }

    class SyncResponse {
        public List<SyncNote> notes;
        public long synchronizedAt;
    }

    class SyncNote {
        public String id;
        public String visibility;
        public String title;
        public String body;
        public String creatorId;
        public String revisionId;
        public String parentRevisionId;
        public long createdAt;
        public long updatedAt;
        public boolean deleted;

        public SyncNote() {
        }

        public SyncNote(
                String id,
                String visibility,
                String title,
                String body,
                String creatorId,
                String revisionId,
                String parentRevisionId,
                long createdAt,
                long updatedAt,
                boolean deleted
        ) {
            this.id = id;
            this.visibility = visibility;
            this.title = title;
            this.body = body;
            this.creatorId = creatorId;
            this.revisionId = revisionId;
            this.parentRevisionId = parentRevisionId;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.deleted = deleted;
        }
    }

    class DeviceView {
        public String id;
        public String memberName;
        public String deviceName;
        public String status;
        public long lastSeenAt;
    }
}
