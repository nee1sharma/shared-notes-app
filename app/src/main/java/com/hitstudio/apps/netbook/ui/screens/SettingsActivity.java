package com.hitstudio.apps.netbook.ui.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hitstudio.apps.netbook.R;

public final class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());
        findViewById(R.id.join_household_setting).setOnClickListener(view ->
                startActivity(new Intent(this, JoinHouseholdActivity.class))
        );
    }
}
