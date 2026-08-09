package com.hitstudio.apps.netbook.data.remote;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public final class SyncScheduler {
    private final Context context;

    @Inject
    public SyncScheduler(@ApplicationContext Context context) {
        this.context = context;
    }

    public void enqueue() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "SharedNotesSync",
                ExistingWorkPolicy.REPLACE,
                request
        );
    }
}
