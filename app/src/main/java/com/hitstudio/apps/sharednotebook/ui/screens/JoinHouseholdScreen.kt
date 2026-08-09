package com.hitstudio.apps.sharednotebook.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinHouseholdScreen(onJoined: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Find or Join Household") }) }
    ) { paddingValues ->
        Button(onClick = onJoined, modifier = Modifier.padding(paddingValues)) {
            Text("Join Household")
        }
    }
}