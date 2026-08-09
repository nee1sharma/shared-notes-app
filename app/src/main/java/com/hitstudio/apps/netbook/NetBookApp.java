package com.hitstudio.apps.netbook;

import android.app.Application;

import com.hitstudio.apps.netbook.data.remote.RegistrationManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class NetBookApp extends Application {
    @Inject
    RegistrationManager registrationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        if (registrationManager.isRegistered()) {
            registrationManager.scheduleHeartbeat();
        }
    }
}
