package com.isolaatti.posting.posts.ui

import android.app.Activity
import android.content.ComponentName
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
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
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.isolaatti.MyApplication
import com.isolaatti.R
import com.isolaatti.audio.common.components.AudioRecorder
import com.isolaatti.audio.player.MediaService
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.posting.posts.components.PostAttachments
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.presentation.CreatePostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

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
            viewModel.addPicture(LocalImage(it))
        }
    }

    private fun makePhotoUri(): Uri {
        val cacheFile = File(filesDir, "temp_picture_${Calendar.getInstance().timeInMillis}")
        return FileProvider.getUriForFile(this, "${MyApplication.myApp.packageName}.provider", cacheFile)
    }

    private var cameraPhotoUri: Uri? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it && cameraPhotoUri != null) {
            viewModel.addPicture(LocalImage(cameraPhotoUri!!))
        }
    }

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    private lateinit var mediaController: MediaController
    private lateinit var mediaRecorder: MediaRecorder

    private val playerListener: Player.Listener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModel.isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when(playbackState) {
                Player.STATE_READY -> {
                    viewModel.ended = false
                    viewModel.isLoading.value = false
                    viewModel.duration.value = this@CreatePostActivity.mediaController.duration
                }
                Player.STATE_ENDED -> {
                    viewModel.ended = true
                }
                Player.STATE_BUFFERING -> {
                    viewModel.isLoading.value = true
                }

                Player.STATE_IDLE -> {}
            }
        }
    }

    private lateinit var recordOutputFile: File



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(this, ComponentName(this, MediaService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()


        lifecycleScope.launch {
            mediaController = mediaControllerFuture.await()

            mediaController.addListener(playerListener)

            // check audios directory
            val directory = File(this@CreatePostActivity.filesDir, "recordings")

            withContext(Dispatchers.IO) {
                if(!directory.isDirectory) {
                    if(directory.isFile) {
                        directory.delete()
                    }
                    directory.mkdir()
                }
            }

            recordOutputFile = File(directory, "${System.currentTimeMillis()}.3gp")


            setContent {
                val text by viewModel.liveContent.observeAsState("")
                val focusRequester = remember { FocusRequester() }
                val pictures by viewModel.photos.collectAsStateWithLifecycle()

                val editMode = remember {
                    mode == EXTRA_MODE_EDIT && postId != 0L
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
                        var audioRecorderIsVisible by remember { mutableStateOf(false) }

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
                                    IconButton(
                                        onClick = {
                                            audioRecorderIsVisible = !audioRecorderIsVisible
                                            mediaRecorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this@CreatePostActivity) else MediaRecorder()
                                        }
                                    ) {
                                        Icon(painterResource(id = R.drawable.baseline_mic_24), null)
                                    }
                                }
                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }

                            AnimatedVisibility(audioRecorderIsVisible) {

                                val isPlaying by viewModel.isPlaying.collectAsState()
                                val position by viewModel.position.collectAsState()
                                val duration by viewModel.duration.collectAsState()
                                val isLoading by viewModel.isLoading.collectAsState()

                                val isRecording by viewModel.isRecording.collectAsState()
                                val recordIsPaused by viewModel.recordingIsPaused.collectAsState()
                                val recordIsStopped by viewModel.recodingIsStopped.collectAsState()

                                LaunchedEffect(isPlaying) {
                                    while(isPlaying) {
                                        delay(500.milliseconds)
                                        viewModel.position.value = mediaController.currentPosition
                                    }
                                }

                                AudioRecorder(
                                    modifier = Modifier.padding(16.dp),
                                    onDismiss = {
                                        audioRecorderIsVisible = false

                                        // stop playback
                                        mediaController.stop()
                                        mediaController.clearMediaItems()

                                        // delete file
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            if(recordOutputFile.exists()) {
                                                recordOutputFile.delete()
                                            }
                                        }

                                        // release media recorder
                                        mediaRecorder.release()

                                        // clear references
                                        viewModel.recodingIsStopped.value = false
                                        viewModel.isRecording.value = false
                                        viewModel.recordingIsPaused.value = false
                                        viewModel.isPlaying.value = false
                                        viewModel.duration.value = 0L
                                        viewModel.position.value = 0L
                                        viewModel.audioToUpload = null
                                    },
                                    onPlay = {
                                        if(mediaController.isCommandAvailable(Player.COMMAND_PREPARE)) {
                                            mediaController.prepare()
                                        }

                                        if(viewModel.ended) {
                                            mediaController.seekTo(0L)
                                        }
                                        mediaController.playWhenReady = true
                                    },
                                    onPause = {
                                        mediaController.pause()
                                    },
                                    onStartRecording = { fromPause ->

                                        if(fromPause) {
                                            mediaRecorder.resume()
                                        } else {
                                            mediaRecorder.run {
                                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                                if (Build.VERSION.SDK_INT >= 26) {
                                                    setOutputFile(recordOutputFile)
                                                } else {
                                                    setOutputFile(recordOutputFile.path)
                                                }

                                                prepare()
                                                start()
                                            }
                                        }
                                        viewModel.recodingIsStopped.value = false
                                        viewModel.isRecording.value = true
                                        viewModel.recordingIsPaused.value = false



                                    },
                                    onPauseRecording = {
                                        mediaRecorder.pause()
                                        viewModel.recodingIsStopped.value = false
                                        viewModel.isRecording.value = true
                                        viewModel.recordingIsPaused.value = true
                                    },
                                    onStopRecording = {
                                        mediaRecorder.stop()
                                        val audioUri = recordOutputFile.toUri()
                                        mediaController.setMediaItem(
                                            MediaItem.Builder()
                                                .setUri(audioUri)
                                                .setMediaMetadata(MediaMetadata.Builder()
                                                    .setTitle(getString(R.string.just_recorded_audio_title))
                                                    .build())
                                                .build()
                                        )
                                        viewModel.audioToUpload = audioUri
                                        viewModel.recodingIsStopped.value = true
                                        viewModel.isRecording.value = false
                                        viewModel.recordingIsPaused.value = false
                                    },
                                    isPlaying = isPlaying,
                                    position = position,
                                    durationSeconds = duration,
                                    isRecording = isRecording,
                                    recordIsPaused = recordIsPaused,
                                    recordIsStopped = recordIsStopped,
                                    isLoading = isLoading,
                                    onSeek = { ms ->
                                        mediaController.seekTo(ms.toLong())
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
    }

    private fun exit() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onStop() {
        super.onStop()

        MediaController.releaseFuture(mediaControllerFuture)
    }
}