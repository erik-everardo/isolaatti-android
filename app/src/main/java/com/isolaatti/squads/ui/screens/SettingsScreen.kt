package com.isolaatti.squads.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.squads.domain.Squad
import com.isolaatti.squads.presentation.SquadSettingScreenState
import com.isolaatti.utils.UrlGen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    squadState: SquadSettingScreenState,
    onNameChanged: (name: String) -> Unit,
    onDescriptionChanged: (description: String) -> Unit,
    onBack: () -> Unit = {},
    onUploadFromGallery: () -> Unit = {},
    onUploadFromCamera: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    Button(onClick = {}) {
                        Text(text = "Save")
                    }
                }
            )
        }
    ) {
        if(squadState.squad != null) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .consumeWindowInsets(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = squadState.squad?.image?.defaultUri,
                        contentDescription = "Squad image",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                        fallback = BrushPainter(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White, MaterialTheme.colorScheme.primary
                                ),
                            )
                        )
                    )

                    Box(modifier = Modifier.align(Alignment.Center)) {
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        Button(onClick = { dropdownExpanded = true }) {
                            Text(text = "Change image")
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = {
                                dropdownExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.take_a_photo))
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onUploadFromCamera()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.upload_photo))
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onUploadFromGallery()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 500.dp).fillMaxWidth(),
                    value = squadState.squad.name,
                    onValueChange = onNameChanged,
                    label = { Text(text = "Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 500.dp).fillMaxWidth(),
                    value = squadState.squad.description,
                    onValueChange = onDescriptionChanged,
                    label = { Text(text = "Description") },
                    minLines = 4
                )
            }
        }

    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        squadState = SquadSettingScreenState(
            squad = Squad(
                id = "1",
                name = "Test squad",
                description = "This is a test squad",
                image = null
            ),
        ),
        onNameChanged = {},
        onDescriptionChanged = {}
    )
}