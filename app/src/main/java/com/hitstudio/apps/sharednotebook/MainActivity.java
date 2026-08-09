package com.hitstudio.apps.sharednotebook;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hitstudio.apps.sharednotebook.ui.screens.NotesHomeActivity;

public final class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        findViewById(R.id.enter_button).setOnClickListener(view -> {
            startActivity(new Intent(this, NotesHomeActivity.class));
            finish();
        });
    }
}
