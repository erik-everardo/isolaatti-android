package com.isolaatti.posting.posts.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isolaatti.MyApplication
import com.isolaatti.R
import com.isolaatti.audio.common.components.AudioRecorder
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.posting.posts.components.PostAttachments
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.presentation.CreatePostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.Calendar

@AndroidEntryPoint
class CreatePostActivity : IsolaattiBaseActivity() {
    companion object {
        const val EXTRA_MODE_CREATE = 0
        /**
         * Post activity in edit mode
         */
        const val EXTRA_MODE_EDIT = 1
        const val EXTRA_KEY_MODE = "mode"

        /**
         * post id, pass long
         */
        const val EXTRA_KEY_POST_ID = "postId"

        const val EXTRA_KEY_POST_POSTED = "post"
    }

    val viewModel: CreatePostViewModel by viewModels()
    var mode: Int = EXTRA_MODE_CREATE
    var postId: Long = 0L

    private val choosePictureLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if(it != null) {
            viewModel.addPicture(it)
        }
    }

    private fun makePhotoUri(): Uri {
        val cacheFile = File(filesDir, "temp_picture_${Calendar.getInstance().timeInMillis}")
        return FileProvider.getUriForFile(this, "${MyApplication.myApp.packageName}.provider", cacheFile)
    }

    private var cameraPhotoUri: Uri? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it && cameraPhotoUri != null) {
            viewModel.addPicture(cameraPhotoUri!!)
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val text by viewModel.liveContent.observeAsState("")
            val focusRequester = remember { FocusRequester() }
            val pictures by viewModel.photos.collectAsStateWithLifecycle()

            val editMode = remember {
                mode == EXTRA_MODE_EDIT && postId != 0L
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            IsolaattiTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(if(editMode) R.string.edit else R.string.new_post)) },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        exit()
                                    }
                                ) {
                                    Icon(Icons.Default.Close, null)
                                }
                            },
                            actions = {
                                Button(
                                    enabled = text.isNotBlank() || pictures.isNotEmpty(),
                                    onClick = {
                                        if(editMode) {
                                            viewModel.editDiscussion(postId)
                                        } else {
                                            viewModel.postDiscussion()
                                        }

                                    }
                                ) {
                                    Text(stringResource(if(editMode) R.string.save else R.string.post))
                                }
                            }
                        )
                    }
                ) {
                    val scrollState = rememberScrollState()

                    val posting by viewModel.sendingPost.observeAsState()

                    if(posting == true) {
                        val postingStep by viewModel.postingStep.collectAsState()
                        AlertDialog(
                            onDismissRequest = {},
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator()
                                    Text(
                                        when(postingStep) {
                                            PostingSteps.PostContent -> getString(R.string.posting)
                                            PostingSteps.UploadingPhotos -> getString(R.string.uploading_photos)
                                            PostingSteps.Finished -> ""
                                            PostingSteps.Unspecified -> ""
                                        },
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            },
                            dismissButton = null,
                            title = {
                                Text(getString(R.string.posting))
                            },
                            confirmButton = {}
                        )
                    } else if(posting == false){
                        finish()
                    }
                    Column(modifier = Modifier.padding(it).verticalScroll(scrollState)) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { textFieldValue ->
                                viewModel.liveContent.postValue(textFieldValue)
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text(stringResource(R.string.what_do_you_want_to_talk_about_you_can_record_an_audio_if_you_want)) },
                            colors = OutlinedTextFieldDefaults
                                .colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                            trailingIcon = {
                                IconButton(onClick = {}) {
                                    Icon(painterResource(id = R.drawable.baseline_mic_24), null)
                                }
                            }
                        )

                        AnimatedVisibility(false) {
                            AudioRecorder(
                                onAudioRecorded = {

                                },
                                dismissible = true,
                                onDismiss = {

                                }
                            )
                        }


                        PostAttachments(
                            modifier = Modifier.padding(16.dp),
                            images = pictures,
                            deletable = true,
                            imageAddable = true,
                            onImageClick = {

                            },
                            onImageDeleteClick = {
                                viewModel.removePicture(it)
                            },
                            onTakePicture = {
                                cameraPhotoUri = makePhotoUri()
                                takePhotoLauncher.launch(cameraPhotoUri!!)
                            },
                            onUploadPicture = {
                                choosePictureLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        )
                    }
                }
            }

        }


        intent.extras?.let {
            mode = it.getInt(EXTRA_KEY_MODE, EXTRA_MODE_CREATE)
            postId = it.getLong(EXTRA_KEY_POST_ID)
        }



        if(mode == EXTRA_MODE_EDIT && postId != 0L) {
            viewModel.loadDiscussion(postId)
        }

    }

    private fun exit() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}