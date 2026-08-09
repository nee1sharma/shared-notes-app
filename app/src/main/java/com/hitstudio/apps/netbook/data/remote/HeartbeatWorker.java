package com.hitstudio.apps.netbook.data.remote;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class HeartbeatWorker extends Worker {
    private static final String TAG = "HeartbeatWorker";

    public HeartbeatWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        RegistrationManager registrationManager = new RegistrationManager(getApplicationContext());
        if (!registrationManager.isRegistered()) {
            return Result.success();
        }

        String baseUrl = registrationManager.getControlPlaneUrl();
        String deviceId = registrationManager.getDeviceId();

        if (baseUrl == null || deviceId == null) {
            return Result.failure();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NetBookApi api = retrofit.create(NetBookApi.class);
        NetBookApi.HeartbeatRequest request = new NetBookApi.HeartbeatRequest(
                deviceId, System.currentTimeMillis()
        );

        try {
            Response<Void> response = api.sendHeartbeat(request).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "Heartbeat sent successfully");
                return Result.success();
            } else {
                Log.e(TAG, "Heartbeat failed: " + response.code());
                return Result.retry();
            }
        } catch (IOException e) {
            Log.e(TAG, "Heartbeat failed", e);
            return Result.retry();
        }
    }
}
