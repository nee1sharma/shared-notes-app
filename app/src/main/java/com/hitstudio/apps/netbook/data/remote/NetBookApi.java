package com.hitstudio.apps.netbook.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NetBookApi {
    @POST("/api/v1/registration/request")
    Call<RegistrationResponse> requestRegistration(@Body RegistrationRequest request);

    @POST("/api/v1/presence/heartbeat")
    Call<Void> sendHeartbeat(@Body HeartbeatRequest request);

    class RegistrationRequest {
        public String memberName;
        public String deviceName;
        public String email;
        public String publicKey;

        public RegistrationRequest(String memberName, String deviceName, String email, String publicKey) {
            this.memberName = memberName;
            this.deviceName = deviceName;
            this.email = email;
            this.publicKey = publicKey;
        }
    }

    class RegistrationResponse {
        public String deviceId;
        public String householdId;
        public String status; // REGISTERED, PENDING, REJECTED
    }

    class HeartbeatRequest {
        public String deviceId;
        public long timestamp;

        public HeartbeatRequest(String deviceId, long timestamp) {
            this.deviceId = deviceId;
            this.timestamp = timestamp;
        }
    }
}
