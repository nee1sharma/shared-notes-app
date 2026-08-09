package com.hitstudio.apps.netbook.data.remote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hitstudio.apps.netbook.data.local.NetBookDatabase;

/** WorkManager entry point for syncs triggered by a shared save and app launch. */
public final class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        RegistrationManager registrationManager = new RegistrationManager(getApplicationContext());
        if (!registrationManager.isRegistered()) return Result.success();

        NetBookDatabase database = Room.databaseBuilder(
                getApplicationContext(),
                NetBookDatabase.class,
                "netbook.db"
        ).addMigrations(NetBookDatabase.MIGRATION_1_2).build();
        try {
            return new SharedNoteSynchronizer(database.noteDao(), registrationManager).synchronize()
                    ? Result.success() : Result.retry();
        } finally {
            database.close();
        }
    }
}
