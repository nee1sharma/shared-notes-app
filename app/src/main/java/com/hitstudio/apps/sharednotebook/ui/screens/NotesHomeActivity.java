package com.hitstudio.apps.sharednotebook.ui.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.hitstudio.apps.sharednotebook.R;
import com.hitstudio.apps.sharednotebook.ui.adapter.NoteAdapter;
import com.hitstudio.apps.sharednotebook.ui.navigation.Screen;
import com.hitstudio.apps.sharednotebook.ui.viewmodel.NoteViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class NotesHomeActivity extends AppCompatActivity {
    private NoteAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView noteCount;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.open_drawer_button).setOnClickListener(
                view -> drawerLayout.openDrawer(GravityCompat.START)
        );

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_connected_devices) {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, ConnectedDevicesActivity.class));
                return true;
            }
            if (itemId == R.id.nav_settings) {
                drawerLayout.closeDrawer(GravityCompat.START);
                navigationView.setCheckedItem(R.id.nav_notes);
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        R.string.settings_coming_soon,
                        Snackbar.LENGTH_SHORT
                ).show();
                return true;
            }
            if (itemId == R.id.nav_about_me) {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, AboutMeActivity.class));
                return true;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        noteCount = findViewById(R.id.note_count);
        emptyState = findViewById(R.id.empty_state);
        emptyTitle = findViewById(R.id.empty_title);
        emptyBody = findViewById(R.id.empty_body);

        adapter = new NoteAdapter(note -> openEditor(note.getId()));
        RecyclerView notesList = findViewById(R.id.notes_list);
        notesList.setLayoutManager(new LinearLayoutManager(this));
        notesList.setAdapter(adapter);

        EditText searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable editable) {
                adapter.setQuery(editable.toString());
                updateEmptyState();
            }
        });

        ChipGroup filterGroup = findViewById(R.id.filter_group);
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? R.id.filter_all : checkedIds.get(0);
            if (checkedId == R.id.filter_private) {
                adapter.setFilter(NoteAdapter.Filter.PRIVATE);
            } else if (checkedId == R.id.filter_shared) {
                adapter.setFilter(NoteAdapter.Filter.SHARED);
            } else {
                adapter.setFilter(NoteAdapter.Filter.ALL);
            }
            updateEmptyState();
        });

        findViewById(R.id.new_note_button).setOnClickListener(view -> openEditor(Screen.NEW_NOTE_ID));

        NoteViewModel viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        viewModel.getNotes().observe(this, notes -> {
            adapter.submitNotes(notes);
            int count = notes == null ? 0 : notes.size();
            noteCount.setText(getString(
                    R.string.note_count,
                    count,
                    getString(count == 1 ? R.string.note_singular : R.string.note_plural)
            ));
            updateEmptyState();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.nav_notes);
        }
    }

    private void openEditor(String noteId) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra(Screen.EXTRA_NOTE_ID, noteId);
        startActivity(intent);
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() > 0) {
            emptyState.setVisibility(View.GONE);
            return;
        }
        emptyState.setVisibility(View.VISIBLE);
        if (adapter.hasQuery()) {
            emptyTitle.setText(R.string.empty_search_title);
            emptyBody.setText(R.string.empty_search_body);
        } else if (adapter.getTotalCount() > 0) {
            emptyTitle.setText(R.string.empty_filter_title);
            emptyBody.setText(R.string.empty_filter_body);
        } else {
            emptyTitle.setText(R.string.empty_notebook_title);
            emptyBody.setText(R.string.empty_notebook_body);
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
    }
}
