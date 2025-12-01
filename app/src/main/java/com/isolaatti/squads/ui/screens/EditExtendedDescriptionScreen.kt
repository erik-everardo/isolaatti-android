package com.isolaatti.squads.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isolaatti.markdown.MarkdownComponent
import com.isolaatti.squads.presentation.EditExtendedDescriptionScreenState
import com.isolaatti.squads.ui.LocalMarkwon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExtendedDescriptionScreen(
    state: EditExtendedDescriptionScreenState,
    onExtendedDescriptionTextChanged: (String) -> Unit,
    onNavigateBack: () -> Unit,
    setIsInPreview: (Boolean) -> Unit,
    shouldShowPendingChangedDialog: Boolean,
    onSave: () -> Unit
) {

    var showPendingChangedDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = state.isInPreview) {
        setIsInPreview(false)
    }

    BackHandler(enabled = !state.isInPreview && shouldShowPendingChangedDialog) {
        showPendingChangedDialog = true
    }

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
                    Button(
                        onClick = onSave,
                        enabled = shouldShowPendingChangedDialog
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)

        ) {
            if(showPendingChangedDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        TextButton(onClick = onSave) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPendingChangedDialog = false
                        }) {
                            Text("No")
                        }
                    },
                    text = { Text("Changed will be discarded, continue?") }
                )
            }

            Column(modifier = Modifier.imePadding().fillMaxSize()) {
                if(state.isInPreview) {
                    MarkdownComponent(
                        text = state.squad?.extendedDescription ?: "",
                        markwon = LocalMarkwon.current,
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                            .verticalScroll(rememberScrollState())

                    )
                } else {
                    BasicTextField(
                        value = state.squad?.extendedDescription ?: "",
                        onValueChange = { onExtendedDescriptionTextChanged(it)},
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        decorationBox = { innerTextField ->
                            if ((state.squad?.extendedDescription ?: "").isEmpty()) {
                                Text(
                                    text = "Write some markdown here...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    SegmentedButton(
                        selected = !state.isInPreview,
                        onClick = {
                            setIsInPreview(false)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 0,
                            count = 2
                        ),
                        icon = {
                            SegmentedButtonDefaults.Icon(
                                active = !state.isInPreview,
                                activeContent = {
                                    Icon(Icons.Default.ModeEdit, "Markdown")
                                },
                                inactiveContent = {
                                    Icon(Icons.Default.ModeEdit, "Markdown")
                                }
                            )
                        },
                        label = { Text("Edit Markdown") }
                    )

                    SegmentedButton(
                        selected = state.isInPreview,
                        onClick = {
                            setIsInPreview(true)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 1,
                            count = 2
                        ),
                        label = { Text("Preview") },
                        icon = {
                            SegmentedButtonDefaults.Icon(
                                active = state.isInPreview,
                                activeContent = {
                                    Icon(Icons.Default.Preview, "Preview")
                                },
                                inactiveContent = {
                                    Icon(Icons.Default.Preview, "Preview")
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun EditExtendedDescriptionScreenPreview() {
    EditExtendedDescriptionScreen(
        state = EditExtendedDescriptionScreenState(
            null,
            false
        ),
        onExtendedDescriptionTextChanged = {},
        onNavigateBack = {},
        onSave = {},
        setIsInPreview = {},
        shouldShowPendingChangedDialog = false,
    )
}
