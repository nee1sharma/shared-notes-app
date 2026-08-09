package com.hitstudio.apps.sharednotebook.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hitstudio.apps.sharednotebook.R;
import com.hitstudio.apps.sharednotebook.domain.model.Note;
import com.hitstudio.apps.sharednotebook.domain.model.NoteVisibility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    public enum Filter { ALL, PRIVATE, SHARED }

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private final OnNoteClickListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    private List<Note> allNotes = Collections.emptyList();
    private List<Note> visibleNotes = Collections.emptyList();
    private String query = "";
    private Filter filter = Filter.ALL;

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitNotes(List<Note> notes) {
        allNotes = notes == null ? Collections.emptyList() : new ArrayList<>(notes);
        applyFilter();
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        applyFilter();
    }

    public void setFilter(Filter filter) {
        this.filter = filter == null ? Filter.ALL : filter;
        applyFilter();
    }

    public int getTotalCount() { return allNotes.size(); }
    public boolean hasQuery() { return !query.isEmpty(); }

    private void applyFilter() {
        List<Note> filtered = new ArrayList<>();
        for (Note note : allNotes) {
            boolean matchesVisibility = filter == Filter.ALL
                    || (filter == Filter.PRIVATE && note.getVisibility() == NoteVisibility.PRIVATE)
                    || (filter == Filter.SHARED && note.getVisibility() == NoteVisibility.SHARED);
            String title = note.getTitle().toLowerCase(Locale.getDefault());
            String body = note.getBody().toLowerCase(Locale.getDefault());
            boolean matchesQuery = query.isEmpty() || title.contains(query) || body.contains(query);
            if (matchesVisibility && matchesQuery) filtered.add(note);
        }
        visibleNotes = filtered;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return visibleNotes.get(position).getId().hashCode();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(visibleNotes.get(position));
    }

    @Override
    public int getItemCount() {
        return visibleNotes.size();
    }

    final class NoteViewHolder extends RecyclerView.ViewHolder {
        private final View visibilityBar;
        private final TextView title;
        private final TextView visibility;
        private final TextView body;
        private final TextView updated;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            visibilityBar = itemView.findViewById(R.id.visibility_bar);
            title = itemView.findViewById(R.id.note_title);
            visibility = itemView.findViewById(R.id.note_visibility);
            body = itemView.findViewById(R.id.note_body);
            updated = itemView.findViewById(R.id.note_updated);
        }

        void bind(Note note) {
            boolean privateNote = note.getVisibility() == NoteVisibility.PRIVATE;
            if (note.getTitle().trim().isEmpty()) {
                title.setText(R.string.untitled_note);
            } else {
                title.setText(note.getTitle());
            }
            visibility.setText(privateNote ? R.string.filter_private : R.string.filter_shared);
            String noteBody = note.getBody().trim().isEmpty()
                    ? itemView.getContext().getString(R.string.empty_note)
                    : note.getBody().replace('\n', ' ');
            body.setText(noteBody);
            String formattedDate = dateFormat.format(new Date(note.getUpdatedAt()));
            updated.setText(itemView.getContext().getString(R.string.edited_at, formattedDate));
            visibilityBar.setBackgroundColor(ContextCompat.getColor(
                    itemView.getContext(),
                    privateNote ? R.color.notebook_secondary : R.color.notebook_tertiary
            ));
            itemView.setOnClickListener(view -> listener.onNoteClick(note));
        }
    }
}
