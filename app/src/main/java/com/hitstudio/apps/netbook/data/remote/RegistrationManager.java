package com.hitstudio.apps.netbook.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.hitstudio.apps.netbook.domain.model.HouseholdService;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
public final class RegistrationManager {
    private static final String PREFS_NAME = "registration_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_HOUSEHOLD_ID = "household_id";
    private static final String KEY_CONTROL_PLANE_URL = "control_plane_url";
    private static final String KEY_REGISTERED = "is_registered";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_INSTALLATION_ID = "installation_id";
    private static final String KEY_LAST_SYNC_AT = "last_sync_at";

    private final SharedPreferences prefs;

    @Inject
    public RegistrationManager(@ApplicationContext Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private final Context context;

    public boolean isRegistered() {
        return prefs.getBoolean(KEY_REGISTERED, false);
    }

    public String getDeviceId() {
        return prefs.getString(KEY_DEVICE_ID, null);
    }

    public String getControlPlaneUrl() {
        return prefs.getString(KEY_CONTROL_PLANE_URL, null);
    }

    public String getAuthorization() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);
        return token == null || token.isEmpty() ? null : "Bearer " + token;
    }

    public long getLastSynchronizedAt() {
        return prefs.getLong(KEY_LAST_SYNC_AT, 0L);
    }

    public void setLastSynchronizedAt(long timestamp) {
        prefs.edit().putLong(KEY_LAST_SYNC_AT, timestamp).apply();
    }

    public void scheduleHeartbeat() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest heartbeatRequest = new PeriodicWorkRequest.Builder(
                HeartbeatWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "HeartbeatWork",
                ExistingPeriodicWorkPolicy.KEEP,
                heartbeatRequest
        );

        OneTimeWorkRequest immediateHeartbeat = new OneTimeWorkRequest.Builder(HeartbeatWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "ImmediateHeartbeat",
                ExistingWorkPolicy.REPLACE,
                immediateHeartbeat
        );

        OneTimeWorkRequest initialSync = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "SharedNotesSync",
                ExistingWorkPolicy.KEEP,
                initialSync
        );
    }

    public void register(HouseholdService service, String memberName, String email, RegistrationCallback callback) {
        String host = service.getHost();
        String baseUrl = "http://" + (host.contains(":") ? "[" + host + "]" : host)
                + ":" + service.getPort() + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NetBookApi api = retrofit.create(NetBookApi.class);
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        String installationId = getOrCreateInstallationId();
        String publicKey = "installation:" + installationId;

        NetBookApi.RegistrationRequest request = new NetBookApi.RegistrationRequest(
                installationId,
                memberName,
                deviceName,
                email,
                publicKey,
                "NetBook Android",
                deviceName,
                "ANDROID"
        );

        api.requestRegistration(request).enqueue(new Callback<NetBookApi.RegistrationResponse>() {
            @Override
            public void onResponse(Call<NetBookApi.RegistrationResponse> call, Response<NetBookApi.RegistrationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NetBookApi.RegistrationResponse regResponse = response.body();
                    if ("REGISTERED".equals(regResponse.status)) {
                        prefs.edit()
                                .putString(KEY_DEVICE_ID, regResponse.deviceId)
                                .putString(KEY_HOUSEHOLD_ID, regResponse.householdId)
                                .putString(KEY_CONTROL_PLANE_URL, baseUrl)
                                .putString(KEY_ACCESS_TOKEN, regResponse.accessToken)
                                .putBoolean(KEY_REGISTERED, true)
                                .apply();
                        scheduleHeartbeat();
                        callback.onSuccess();
                    } else if ("PENDING".equals(regResponse.status)) {
                        callback.onPending();
                    } else {
                        callback.onError("Registration rejected");
                    }
                } else {
                    callback.onError("Failed to connect to control plane");
                }
            }

            @Override
            public void onFailure(Call<NetBookApi.RegistrationResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void loadConnectedDevices(DeviceListCallback callback) {
        String baseUrl = getControlPlaneUrl();
        String authorization = getAuthorization();
        if (!isRegistered() || baseUrl == null || authorization == null) {
            callback.onError("Register this device to see household devices.");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(NetBookApi.class).getDevices(authorization).enqueue(new Callback<java.util.List<NetBookApi.DeviceView>>() {
            @Override
            public void onResponse(
                    Call<java.util.List<NetBookApi.DeviceView>> call,
                    Response<java.util.List<NetBookApi.DeviceView>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Unable to load household devices.");
                }
            }

            @Override
            public void onFailure(Call<java.util.List<NetBookApi.DeviceView>> call, Throwable throwable) {
                callback.onError(throwable.getMessage() == null ? "Unable to reach the control plane." : throwable.getMessage());
            }
        });
    }

    private String getOrCreateInstallationId() {
        String existing = prefs.getString(KEY_INSTALLATION_ID, null);
        if (existing != null && !existing.isEmpty()) return existing;
        String created = java.util.UUID.randomUUID().toString();
        prefs.edit().putString(KEY_INSTALLATION_ID, created).apply();
        return created;
    }

    public interface RegistrationCallback {
        void onSuccess();
        void onPending();
        void onError(String message);
    }

    public interface DeviceListCallback {
        void onSuccess(java.util.List<NetBookApi.DeviceView> devices);
        void onError(String message);
    }
}
