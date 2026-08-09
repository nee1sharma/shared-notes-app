package com.hitstudio.apps.sharednotebook;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hitstudio.apps.sharednotebook.data.remote.RegistrationManager;
import com.hitstudio.apps.sharednotebook.ui.screens.JoinHouseholdActivity;
import com.hitstudio.apps.sharednotebook.ui.screens.NotesHomeActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class MainActivity extends AppCompatActivity {
    @Inject
    RegistrationManager registrationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        findViewById(R.id.enter_button).setOnClickListener(view -> {
            if (registrationManager.isRegistered()) {
                startActivity(new Intent(this, NotesHomeActivity.class));
            } else {
                startActivity(new Intent(this, JoinHouseholdActivity.class));
            }
            finish();
        });
    }
}
