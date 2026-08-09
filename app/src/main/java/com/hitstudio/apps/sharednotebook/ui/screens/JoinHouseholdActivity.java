package com.hitstudio.apps.sharednotebook.ui.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hitstudio.apps.sharednotebook.R;

public final class JoinHouseholdActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_household);
        findViewById(R.id.join_household_button).setOnClickListener(view -> {
            startActivity(new Intent(this, NotesHomeActivity.class));
            finish();
        });
    }
}
