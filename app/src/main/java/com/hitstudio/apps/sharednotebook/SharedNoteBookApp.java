package com.hitstudio.apps.sharednotebook;

import android.app.Application;

import com.hitstudio.apps.sharednotebook.data.remote.RegistrationManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class SharedNoteBookApp extends Application {
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
