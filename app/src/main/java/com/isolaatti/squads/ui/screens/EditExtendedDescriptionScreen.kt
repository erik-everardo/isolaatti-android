package com.isolaatti.squads.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.isolaatti.squads.presentation.EditExtendedDescriptionScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExtendedDescriptionScreen(
    viewModel: EditExtendedDescriptionScreenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Description") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        BasicTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        )
    }
}
