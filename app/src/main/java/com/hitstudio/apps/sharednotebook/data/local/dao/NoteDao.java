package com.hitstudio.apps.sharednotebook.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hitstudio.apps.sharednotebook.data.local.entity.NoteEntity;
import com.hitstudio.apps.sharednotebook.data.local.entity.NoteRevisionEntity;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    LiveData<List<NoteEntity>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    NoteEntity getNoteById(String noteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(NoteEntity note);

    @Update
    void updateNote(NoteEntity note);

    @Delete
    void deleteNote(NoteEntity note);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRevision(NoteRevisionEntity revision);

    @Query("SELECT * FROM note_revisions WHERE noteId = :noteId ORDER BY createdAt DESC")
    LiveData<List<NoteRevisionEntity>> getRevisionsForNote(String noteId);
}
