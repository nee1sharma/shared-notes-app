package com.hitstudio.apps.sharednotebook.ui.navigation

sealed class Screen(val route: String) {
    object Launch : Screen("launch")
    object JoinHousehold : Screen("join_household")
    object Registration : Screen("registration")
    object PendingRegistration : Screen("pending_registration")
    object NotesHome : Screen("notes_home")
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String) = "note_editor/$noteId"
    }
    object RevisionHistory : Screen("revision_history/{noteId}") {
        fun createRoute(noteId: String) = "revision_history/$noteId"
    }
    object ConflictResolution : Screen("conflict_resolution/{noteId}") {
        fun createRoute(noteId: String) = "conflict_resolution/$noteId"
    }
    object SyncStatus : Screen("sync_status")
    object Profile : Screen("profile")
}