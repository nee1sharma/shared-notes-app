package com.hitstudio.apps.sharednotebook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hitstudio.apps.sharednotebook.domain.model.NoteVisibility
import com.hitstudio.apps.sharednotebook.ui.viewmodel.NoteEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onBackClick: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val body by viewModel.body.collectAsState()
    val visibility by viewModel.visibility.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new") "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveNote(onBackClick) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = visibility == NoteVisibility.PRIVATE,
                    onClick = { viewModel.onVisibilityChange(NoteVisibility.PRIVATE) },
                    label = { Text("Private") }
                )
                FilterChip(
                    selected = visibility == NoteVisibility.SHARED,
                    onClick = { viewModel.onVisibilityChange(NoteVisibility.SHARED) },
                    label = { Text("Shared") }
                )
            }

            OutlinedTextField(
                value = body,
                onValueChange = viewModel::onBodyChange,
                label = { Text("Body") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}