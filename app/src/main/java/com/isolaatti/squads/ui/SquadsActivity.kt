package com.isolaatti.squads.ui

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.isolaatti.common.IsolaattiTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.isolaatti.MyApplication
import com.isolaatti.common.dialogs.CameraPermissionRationale
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.post_creator.presentation.CreatePostContract
import com.isolaatti.squads.presentation.EditExtendedDescriptionContract
import com.isolaatti.squads.presentation.SquadSettingsViewModel
import com.isolaatti.squads.presentation.SquadViewModel
import com.isolaatti.squads.ui.screens.MySquadsScreen
import com.isolaatti.squads.ui.screens.SettingsScreen
import com.isolaatti.squads.ui.screens.SquadSearchScreen
import com.isolaatti.squads.ui.squad.SquadScreen
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class SquadsActivity : ComponentActivity() {

    private fun makePhotoUri(): Uri {
        val cacheFile = File(filesDir, "temp_picture_${Calendar.getInstance().timeInMillis}")
        return FileProvider.getUriForFile(this, "${MyApplication.Companion.myApp.packageName}.provider", cacheFile)
    }


    @Inject lateinit var markwon: Markwon
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalMarkwon provides markwon) {
                IsolaattiTheme { // Apply your app's theme
                    val navController = rememberNavController()
                    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

                    var showCameraPermissionRationale by remember { mutableStateOf(false) }

                    if(showCameraPermissionRationale) {
                        CameraPermissionRationale(
                            onContinue = {
                                cameraPermissionState.launchPermissionRequest()
                            },
                            onDismiss = {
                                showCameraPermissionRationale = false
                            }
                        )
                    }

                    NavHost(navController, startDestination = SquadMainNavigation.SquadsHome, modifier = Modifier.fillMaxSize()) {
                        composable<SquadMainNavigation.SquadsHome> {
                            SquadsScreenWithNavigation(
                                onSquadClick = {
                                    navController.navigate(SquadMainNavigation.Squad(it))
                                }
                            )
                        }
                        composable<SquadMainNavigation.Squad> {
                            val route = it.toRoute<SquadMainNavigation.Squad>()

                            val viewModel = hiltViewModel<SquadViewModel>()

                            val createSquadPostLauncher = rememberLauncherForActivityResult(CreatePostContract()) {

                            }

                            val editExtendedDescriptionLauncher = rememberLauncherForActivityResult(EditExtendedDescriptionContract()) {

                            }

                            val chooseSquadImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {

                            }

                            var imageUri: Uri? by remember { mutableStateOf(null) }

                            val takeSquadImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
                                if(result && imageUri != null) {
                                    viewModel.startUploadingImage(imageUri!!)
                                    imageUri = null
                                }


                            }

                            val state by viewModel.state.collectAsState()
                            SquadScreen(
                                state = state,
                                onNavigateToSettingsScreen = {
                                    navController.navigate(SquadMainNavigation.SquadsSettings(route.id))
                                },
                                onRefresh = {
                                    viewModel.getSquad(refresh = true)
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onClickImage = {
                                    viewModel.onClickImage()
                                },
                                onNavigateToAddPost = {
                                    createSquadPostLauncher.launch(CreatePostContract.Input(viewModel.squadId))
                                },
                                onNavigateToEditDescription = {
                                    viewModel.squadId?.let { input -> editExtendedDescriptionLauncher.launch(input) }
                                },
                                onNavigateToSearch = {
                                    viewModel.squadId?.let { it1 -> navController.navigate(SquadMainNavigation.SquadsSearch(it1)) }
                                },
                                onUploadFromGallery = {
                                    chooseSquadImageLauncher.launch(PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    ))
                                },
                                onUploadFromCamera = {
                                    if(cameraPermissionState.status.isGranted) {
                                        val uri = makePhotoUri()
                                        imageUri = uri

                                        takeSquadImageLauncher.launch(uri)
                                    } else {
                                        if(cameraPermissionState.status.shouldShowRationale) {
                                            showCameraPermissionRationale = true
                                        } else {
                                            cameraPermissionState.launchPermissionRequest()
                                        }


                                    }

                                },
                                onViewPicture = { image, images ->
                                    PictureViewerActivity.startActivityWithImages(this@SquadsActivity, ArrayList(images), images.indexOf(image))
                                },
                                onDismissImageDialog = {
                                    viewModel.dismissImageDialog()
                                }
                            )
                        }
                        composable<SquadMainNavigation.SquadsSettings> {
                            val viewModel = hiltViewModel<SquadSettingsViewModel>()
                            val state by viewModel.uiState.collectAsState()
                            SettingsScreen(
                                squadState = state,
                                onNameChanged = viewModel::onNameChange,
                                onDescriptionChanged = viewModel::onDescriptionChange,
                                onUploadFromCamera = {},
                                onUploadFromGallery = {}
                            )
                        }

                        composable<SquadMainNavigation.SquadsSearch> {
                            SquadSearchScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadsScreenWithNavigation(onSquadClick: (String) -> Unit = {}) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Squads") },
                navigationIcon = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
            MySquadsScreen(hiltViewModel(), onSquadClick = onSquadClick)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IsolaattiTheme {
        SquadsScreenWithNavigation()
    }
}