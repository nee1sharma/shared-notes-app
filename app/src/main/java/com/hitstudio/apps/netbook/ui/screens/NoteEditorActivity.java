package com.hitstudio.apps.netbook.ui.screens;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.hitstudio.apps.netbook.R;
import com.hitstudio.apps.netbook.domain.model.NoteVisibility;
import com.hitstudio.apps.netbook.ui.navigation.Screen;
import com.hitstudio.apps.netbook.ui.viewmodel.NoteEditorViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class NoteEditorActivity extends AppCompatActivity {
    private static final String STATE_EDITING = "editing";

    private NoteEditorViewModel viewModel;
    private MaterialToolbar toolbar;
    private MaterialButtonToggleGroup visibilityToggle;
    private MaterialButton privateButton;
    private MaterialButton sharedButton;
    private ProgressBar loadingView;
    private View editContainer;
    private ScrollView readContainer;
    private EditText titleInput;
    private EditText bodyInput;
    private TextView readTitle;
    private TextView readBody;
    private boolean newNote;
    private boolean editing;
    private boolean loading = true;
    private boolean saving;
    private boolean updatingToggle;
    private boolean focusRequested;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        String noteId = getIntent().getStringExtra(Screen.EXTRA_NOTE_ID);
        newNote = noteId == null || noteId.trim().isEmpty() || Screen.NEW_NOTE_ID.equals(noteId);
        editing = savedInstanceState == null
                ? newNote : savedInstanceState.getBoolean(STATE_EDITING, newNote);

        bindViews();
        configureActions();
        viewModel = new ViewModelProvider(this).get(NoteEditorViewModel.class);
        observeViewModel();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.editor_toolbar);
        visibilityToggle = findViewById(R.id.visibility_toggle);
        privateButton = findViewById(R.id.visibility_private);
        sharedButton = findViewById(R.id.visibility_shared);
        loadingView = findViewById(R.id.editor_loading);
        editContainer = findViewById(R.id.edit_container);
        readContainer = findViewById(R.id.read_container);
        titleInput = findViewById(R.id.title_input);
        bodyInput = findViewById(R.id.body_input);
        readTitle = findViewById(R.id.read_title);
        readBody = findViewById(R.id.read_body);
    }

    private void configureActions() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
        toolbar.setNavigationOnClickListener(view -> handleBack());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                save(true);
                return true;
            }
            if (item.getItemId() == R.id.action_edit) {
                setEditing(true);
                return true;
            }
            return false;
        });

        visibilityToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || updatingToggle || saving) return;
            NoteVisibility selected = checkedId == R.id.visibility_shared
                    ? NoteVisibility.SHARED : NoteVisibility.PRIVATE;
            NoteVisibility current = viewModel == null ? null : viewModel.getVisibility().getValue();
            if (selected != current && viewModel != null) {
                if (!editing) setEditing(true);
                viewModel.onVisibilityChange(selected);
            }
            updateToggleAppearance(selected);
        });

        titleInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable editable) {
                if (viewModel != null) viewModel.onTitleChange(editable.toString());
                updateReadOnlyText();
            }
        });
        bodyInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable editable) {
                if (viewModel != null) viewModel.onBodyChange(editable.toString());
                updateReadOnlyText();
            }
        });

        GestureDetector detector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onDown(MotionEvent event) { return true; }
                    @Override public boolean onDoubleTap(MotionEvent event) {
                        setEditing(true);
                        return true;
                    }
                });
        readContainer.setOnTouchListener((view, event) -> detector.onTouchEvent(event));
    }

    private void observeViewModel() {
        viewModel.getTitle().observe(this, title -> {
            String value = title == null ? "" : title;
            if (!Objects.equals(titleInput.getText().toString(), value)) titleInput.setText(value);
            updateReadOnlyText();
        });
        viewModel.getBody().observe(this, body -> {
            String value = body == null ? "" : body;
            if (!Objects.equals(bodyInput.getText().toString(), value)) bodyInput.setText(value);
            updateReadOnlyText();
        });
        viewModel.getVisibility().observe(this, visibility -> {
            NoteVisibility value = visibility == null ? NoteVisibility.PRIVATE : visibility;
            updatingToggle = true;
            visibilityToggle.check(value == NoteVisibility.PRIVATE
                    ? R.id.visibility_private : R.id.visibility_shared);
            updatingToggle = false;
            updateToggleAppearance(value);
        });
        viewModel.getLoading().observe(this, value -> {
            loading = Boolean.TRUE.equals(value);
            updateUiState();
        });
        viewModel.getSaving().observe(this, value -> {
            saving = Boolean.TRUE.equals(value);
            updateUiState();
        });
    }

    private void updateUiState() {
        loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
        editContainer.setVisibility(!loading && editing ? View.VISIBLE : View.GONE);
        readContainer.setVisibility(!loading && !editing ? View.VISIBLE : View.GONE);
        visibilityToggle.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);

        MenuItem saveItem = toolbar.getMenu().findItem(R.id.action_save);
        MenuItem editItem = toolbar.getMenu().findItem(R.id.action_edit);
        saveItem.setVisible(!loading && editing);
        saveItem.setEnabled(!saving);
        editItem.setVisible(!loading && !editing);
        privateButton.setEnabled(!saving);
        sharedButton.setEnabled(!saving);

        if (!loading && editing && !focusRequested) {
            focusRequested = true;
            EditText target = newNote ? titleInput : bodyInput;
            target.post(() -> {
                target.requestFocus();
                target.setSelection(target.length());
            });
        }
    }

    private void setEditing(boolean editing) {
        this.editing = editing;
        focusRequested = !editing;
        updateUiState();
        if (!editing) {
            View current = getCurrentFocus();
            if (current != null) {
                InputMethodManager keyboard = getSystemService(InputMethodManager.class);
                if (keyboard != null) keyboard.hideSoftInputFromWindow(current.getWindowToken(), 0);
                current.clearFocus();
            }
        }
    }

    private void updateToggleAppearance(NoteVisibility selected) {
        styleToggleButton(
                privateButton,
                selected == NoteVisibility.PRIVATE,
                R.color.notebook_secondary_container,
                R.color.notebook_on_secondary_container
        );
        styleToggleButton(
                sharedButton,
                selected == NoteVisibility.SHARED,
                R.color.notebook_tertiary_container,
                R.color.notebook_on_tertiary_container
        );
    }

    private void styleToggleButton(MaterialButton button, boolean selected,
                                   int selectedBackground, int selectedText) {
        int background = selected
                ? ContextCompat.getColor(this, selectedBackground) : Color.TRANSPARENT;
        int text = ContextCompat.getColor(
                this,
                selected ? selectedText : R.color.notebook_on_surface_variant
        );
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(text);
    }

    private void updateReadOnlyText() {
        if (readTitle == null) return;
        String title = titleInput.getText().toString();
        String body = bodyInput.getText().toString();
        readTitle.setText(title.trim().isEmpty() ? getString(R.string.untitled) : title);
        readBody.setText(body.trim().isEmpty() ? getString(R.string.empty_note) : body);
        readBody.setTextColor(ContextCompat.getColor(
                this,
                body.trim().isEmpty() ? R.color.notebook_on_surface_variant : R.color.notebook_on_surface
        ));
    }

    private void save(boolean stayOnNote) {
        if (saving || loading) return;
        viewModel.saveNote(() -> {
            if (stayOnNote && !newNote) {
                setEditing(false);
            } else {
                finish();
            }
        });
    }

    private void handleBack() {
        if (editing && (!titleInput.getText().toString().trim().isEmpty()
                || !bodyInput.getText().toString().trim().isEmpty())) {
            save(false);
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        outState.putBoolean(STATE_EDITING, editing);
        super.onSaveInstanceState(outState);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
    }
}
