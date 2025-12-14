package com.isolaatti.squads.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.makePhotoUri
import com.isolaatti.squads.domain.Squad
import com.isolaatti.squads.presentation.SquadSettingScreenState
import com.isolaatti.squads.presentation.SquadSettingsViewModel
import com.isolaatti.utils.UrlGen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    squadSettingsViewModel: SquadSettingsViewModel,
    onBack: () -> Unit = {},
) {
    val squadState by squadSettingsViewModel.uiState.collectAsState()

    var imageToUploadImageUri: Uri by rememberSaveable { mutableStateOf( Uri.EMPTY ) }

    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if(result) {
            squadSettingsViewModel.onImageSelected(LocalImage(imageToUploadImageUri))
        }
    }

    val uploadPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
        if(result != null) {
            squadSettingsViewModel.onImageSelected(LocalImage(result))
        }
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            squadSettingsViewModel.save()
                        },
                        enabled = true
                    ) {
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
                        model = squadState.imageToUpload?.defaultUri ?: squadState.squad?.image?.defaultUri,
                        contentDescription = "Squad image",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
                        fallback = BrushPainter(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White, MaterialTheme.colorScheme.primary
                                ),
                            )
                        )
                    )

                    if(squadState.uploadingPicture) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)) {
                        if(squadState.imageToUpload != null) {
                            FilledTonalButton(onClick = {
                                squadSettingsViewModel.undoImageSelected()
                            }) {
                                Icon(Icons.AutoMirrored.Default.Undo, stringResource(R.string.undo))
                                Text(stringResource(R.string.undo))
                            }
                            Spacer(Modifier.width(16.dp))
                        }



                        Box {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            FilledTonalButton(onClick = { dropdownExpanded = true }) {
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
                                        val uri = context.makePhotoUri()
                                        imageToUploadImageUri = uri

                                        takePhotoLauncher.launch(uri)
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(stringResource(R.string.upload_photo))
                                    },
                                    onClick = {
                                        dropdownExpanded = false
                                        val mediaRequest = PickVisualMediaRequest.Builder().setMediaType(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly).build()
                                        uploadPhotoLauncher.launch(mediaRequest)
                                    }
                                )
                            }
                        }


                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 500.dp).fillMaxWidth(),
                    value = squadState.squad?.name ?: "",
                    onValueChange = { value ->
                        squadSettingsViewModel.onNameChange(value)
                    },
                    label = { Text(text = "Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 500.dp).fillMaxWidth(),
                    value = squadState.squad?.description ?: "",
                    onValueChange = { value ->
                        squadSettingsViewModel.onDescriptionChange(value)
                    },
                    label = { Text(text = "Description") },
                    minLines = 4
                )
            }
        }

    }
}