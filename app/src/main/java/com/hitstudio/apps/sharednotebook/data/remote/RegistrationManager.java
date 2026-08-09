package com.hitstudio.apps.sharednotebook.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.hitstudio.apps.sharednotebook.domain.model.HouseholdService;

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
    }

    public void register(HouseholdService service, String memberName, String email, RegistrationCallback callback) {
        String baseUrl = "http://" + service.getHost() + ":" + service.getPort() + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SharedNoteBookApi api = retrofit.create(SharedNoteBookApi.class);
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        String publicKey = "dummy-public-key"; // Cryptographic identity planned for future

        SharedNoteBookApi.RegistrationRequest request = new SharedNoteBookApi.RegistrationRequest(
                memberName, deviceName, email, publicKey
        );

        api.requestRegistration(request).enqueue(new Callback<SharedNoteBookApi.RegistrationResponse>() {
            @Override
            public void onResponse(Call<SharedNoteBookApi.RegistrationResponse> call, Response<SharedNoteBookApi.RegistrationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedNoteBookApi.RegistrationResponse regResponse = response.body();
                    if ("REGISTERED".equals(regResponse.status)) {
                        prefs.edit()
                                .putString(KEY_DEVICE_ID, regResponse.deviceId)
                                .putString(KEY_HOUSEHOLD_ID, regResponse.householdId)
                                .putString(KEY_CONTROL_PLANE_URL, baseUrl)
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
            public void onFailure(Call<SharedNoteBookApi.RegistrationResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface RegistrationCallback {
        void onSuccess();
        void onPending();
        void onError(String message);
    }
}
