package com.hitstudio.apps.sharednotebook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hitstudio.apps.sharednotebook.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Launch.route
    ) {
        composable(Screen.Launch.route) {
            LaunchScreen(onContinue = {
                // For now, skip registration flow and go straight to notes
                navController.navigate(Screen.NotesHome.route) {
                    popUpTo(Screen.Launch.route) { inclusive = true }
                }
            })
        }
        composable(Screen.NotesHome.route) {
            NotesHomeScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                },
                onNewNoteClick = {
                    navController.navigate(Screen.NoteEditor.createRoute("new"))
                }
            )
        }
        composable(Screen.NoteEditor.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditorScreen(
                noteId = noteId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}