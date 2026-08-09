package com.hitstudio.apps.netbook.data.remote;

import com.hitstudio.apps.netbook.data.local.dao.NoteDao;
import com.hitstudio.apps.netbook.data.local.entity.NoteEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Uploads locally saved shared notes and applies the control plane's current revisions. */
final class SharedNoteSynchronizer {
    private final NoteDao noteDao;
    private final RegistrationManager registrationManager;

    SharedNoteSynchronizer(NoteDao noteDao, RegistrationManager registrationManager) {
        this.noteDao = noteDao;
        this.registrationManager = registrationManager;
    }

    boolean synchronize() {
        if (!registrationManager.isRegistered()) return true;
        String baseUrl = registrationManager.getControlPlaneUrl();
        String authorization = registrationManager.getAuthorization();
        if (baseUrl == null || authorization == null) return false;

        List<NetBookApi.SyncNote> outgoing = new ArrayList<>();
        for (NoteEntity note : noteDao.getSharedNotesForSync()) {
            outgoing.add(new NetBookApi.SyncNote(
                    note.getId(),
                    note.getVisibility(),
                    note.getTitle(),
                    note.getBody(),
                    note.getCreatorId(),
                    note.getCurrentRevisionId(),
                    emptyToNull(note.getParentRevisionId()),
                    note.getCreatedAt(),
                    note.getUpdatedAt(),
                    note.isDeleted()
            ));
        }

        NetBookApi api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NetBookApi.class);
        try {
            Response<NetBookApi.SyncResponse> response = api.synchronize(
                    authorization,
                    new NetBookApi.SyncRequest(outgoing, registrationManager.getLastSynchronizedAt())
            ).execute();
            if (!response.isSuccessful() || response.body() == null) return false;

            List<NoteEntity> incoming = new ArrayList<>();
            List<NetBookApi.SyncNote> responseNotes = response.body().notes == null
                    ? Collections.emptyList() : response.body().notes;
            for (NetBookApi.SyncNote note : responseNotes) {
                if (!isValidNote(note)) continue;
                incoming.add(new NoteEntity(
                        note.id,
                        "SHARED",
                        safe(note.title),
                        safe(note.body),
                        safe(note.creatorId),
                        note.revisionId,
                        nullToEmpty(note.parentRevisionId),
                        note.createdAt,
                        note.updatedAt,
                        note.deleted
                ));
            }
            if (!incoming.isEmpty()) noteDao.upsertNotes(incoming);
            registrationManager.setLastSynchronizedAt(response.body().synchronizedAt);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static boolean isValidNote(NetBookApi.SyncNote note) {
        try {
            UUID.fromString(note.id);
            UUID.fromString(note.revisionId);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
