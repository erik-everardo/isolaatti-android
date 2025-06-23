package com.isolaatti.posting.post_creator.ui

import android.content.ComponentName
import android.content.Intent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import com.isolaatti.audio.common.components.AudioPlayer
import com.isolaatti.audio.common.components.AudioRecorder
import com.isolaatti.audio.player.MediaService
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.posting.post_creator.presentation.CreatePostEvents
import com.isolaatti.posting.posts.components.PostAttachments
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.post_creator.presentation.CreatePostViewModel
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
        return FileProvider.getUriForFile(this, "${MyApplication.Companion.myApp.packageName}.provider", cacheFile)
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
            viewModel.audioRecorderState.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when(playbackState) {
                Player.STATE_READY -> {
                    viewModel.ended = false
                    viewModel.audioRecorderState.isLoading = false
                    viewModel.audioRecorderState.durationMilliseconds = this@CreatePostActivity.mediaController.duration
                }
                Player.STATE_ENDED -> {
                    viewModel.ended = true
                }
                Player.STATE_BUFFERING -> {
                    viewModel.audioRecorderState.isLoading = true
                }

                Player.STATE_IDLE -> {}
            }
        }
    }

    private lateinit var recordOutputFile: File

    fun onDismissRecorder() {

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
        viewModel.audioRecorderState.clearState()
        viewModel.audioToUpload = null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            mode = it.getInt(EXTRA_KEY_MODE, EXTRA_MODE_CREATE)
            postId = it.getLong(EXTRA_KEY_POST_ID)
        }

        if(mode == EXTRA_MODE_EDIT && postId != 0L) {
            viewModel.loadDiscussion(postId)
        }

        val sessionToken = SessionToken(this, ComponentName(this, MediaService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()


        lifecycleScope.launch {
            mediaController = mediaControllerFuture.await()

            mediaController.addListener(playerListener)

            // check audios directory
            val directory = File(this@CreatePostActivity.filesDir, "recordings")

            withContext(Dispatchers.IO) {
                if (!directory.isDirectory) {
                    if (directory.isFile) {
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

                var showExitConfirmationDialog by remember { mutableStateOf(false) }
                var showDiscardAudioConfirmationDialog by remember { mutableStateOf(false) }
                var showExistingAudioDeletionConfirmationDialog by remember { mutableStateOf(false) }
                var showReplaceAudioConfirmationDialog by remember { mutableStateOf(false) }
                val loading by viewModel.loading.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when(event) {
                            is CreatePostEvents.PostCreatedOrEdited -> {
                                setResult(
                                    RESULT_OK,
                                    Intent().apply {
                                        putExtra(
                                            EXTRA_KEY_POST_POSTED,
                                            event.post
                                        )
                                    })
                                finish()
                            }
                        }

                    }
                }

                IsolaattiTheme {
                    if (loading) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Companion.CenterHorizontally,
                            modifier = Modifier.Companion.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Scaffold(
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = { Text(stringResource(if (editMode) R.string.edit else R.string.new_post)) },
                                    navigationIcon = {
                                        IconButton(
                                            onClick = {
                                                if (viewModel.thereIsContent) {
                                                    showExitConfirmationDialog = true
                                                } else {
                                                    exit()
                                                }

                                            }
                                        ) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    },
                                    actions = {
                                        Button(
                                            enabled = text.isNotBlank() || pictures.isNotEmpty(),
                                            onClick = {
                                                if (editMode) {
                                                    viewModel.editDiscussion(postId)
                                                } else {
                                                    viewModel.postDiscussion()
                                                }

                                            }
                                        ) {
                                            Text(stringResource(if (editMode) R.string.save else R.string.post))
                                        }
                                    }
                                )
                            }
                        ) {
                            val scrollState = rememberScrollState()

                            val posting by viewModel.sendingPost.collectAsState()
                            val audioRecorderIsVisible by viewModel.audioRecorderIsVisible.collectAsState()
                            val existingAudio by viewModel.existingAudio.collectAsState()

                            if (posting) {
                                val postingStep by viewModel.postingStep.collectAsState()
                                AlertDialog(
                                    onDismissRequest = {},
                                    text = {
                                        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                                            CircularProgressIndicator()
                                            Text(
                                                when (postingStep) {
                                                    PostingSteps.PostContent -> stringResource(R.string.posting)
                                                    PostingSteps.UploadingPhotos -> stringResource(R.string.uploading_photos)
                                                    PostingSteps.Finished -> ""
                                                    PostingSteps.Unspecified -> ""
                                                    PostingSteps.UploadingAudio -> stringResource(R.string.uploading_audio)
                                                },
                                                modifier = Modifier.Companion.padding(4.dp)
                                            )
                                        }
                                    },
                                    dismissButton = null,
                                    title = {
                                        Text(getString(R.string.posting))
                                    },
                                    confirmButton = {}
                                )
                            } else if (posting == false) {

                            }

                            if (showExitConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showExitConfirmationDialog = false
                                    },
                                    text = {
                                        Text(stringResource(R.string.discard_post_confirm_message))
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                exit()
                                            }
                                        ) {
                                            Text(stringResource(R.string.accept))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showExitConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    }
                                )
                            }

                            if (showDiscardAudioConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showDiscardAudioConfirmationDialog = false
                                    },
                                    text = {
                                        Text(stringResource(R.string.discard_audio_confirm_message))
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.audioRecorderIsVisible.value = false
                                                onDismissRecorder()
                                                showDiscardAudioConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.accept))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showDiscardAudioConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    }
                                )
                            }

                            if (showExistingAudioDeletionConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showExistingAudioDeletionConfirmationDialog = false
                                    },
                                    text = {
                                        Text(stringResource(R.string.remove_audio_confirm_message))
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.markCurrentAudioForDeletion()
                                                showExistingAudioDeletionConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.accept))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showExistingAudioDeletionConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    }
                                )
                            }

                            if (showReplaceAudioConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showReplaceAudioConfirmationDialog = false
                                    },
                                    text = {
                                        Text(stringResource(R.string.replace_audio_confirmation_message))
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.setToReplaceAudio()
                                                showReplaceAudioConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.accept))
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showReplaceAudioConfirmationDialog = false
                                            }
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    }
                                )
                            }


                            Column(
                                modifier = Modifier.Companion
                                    .padding(it)
                                    .verticalScroll(scrollState)
                            ) {


                                OutlinedTextField(
                                    value = text,
                                    onValueChange = { textFieldValue ->
                                        viewModel.liveContent.postValue(textFieldValue)
                                    },
                                    modifier = Modifier.Companion
                                        .padding(horizontal = 8.dp)
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    placeholder = { Text(stringResource(R.string.what_do_you_want_to_talk_about_you_can_record_an_audio_if_you_want)) },
                                    colors = OutlinedTextFieldDefaults
                                        .colors(
                                            focusedBorderColor = Color.Companion.Transparent,
                                            unfocusedBorderColor = Color.Companion.Transparent
                                        ),
                                    trailingIcon = {
                                        IconButton(
                                            enabled = existingAudio == null,
                                            onClick = {
                                                when {
                                                    audioRecorderIsVisible && (viewModel.audioRecorderState.isRecording || viewModel.audioToUpload != null) -> {
                                                        showDiscardAudioConfirmationDialog = true
                                                    }

                                                    audioRecorderIsVisible -> {
                                                        viewModel.audioRecorderIsVisible.value =
                                                            false
                                                    }

                                                    !audioRecorderIsVisible -> {
                                                        mediaRecorder =
                                                            if (Build.VERSION.SDK_INT >= 31) MediaRecorder(
                                                                this@CreatePostActivity
                                                            ) else MediaRecorder()
                                                        viewModel.audioRecorderIsVisible.value =
                                                            true
                                                    }
                                                }

                                            }
                                        ) {
                                            Icon(
                                                painterResource(id = R.drawable.baseline_mic_24),
                                                null
                                            )
                                        }
                                    }
                                )

                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }

                                AnimatedVisibility(existingAudio != null) {
                                    OutlinedCard(Modifier.Companion.padding(16.dp)) {
                                        Row(
                                            Modifier.Companion.padding(8.dp),
                                            verticalAlignment = Alignment.Companion.CenterVertically
                                        ) {
                                            Text(
                                                "Existing audio",
                                                modifier = Modifier.Companion.weight(1f)
                                            )
                                            IconButton(
                                                modifier = Modifier.Companion.padding(end = 4.dp),
                                                onClick = {
                                                    showExistingAudioDeletionConfirmationDialog =
                                                        true
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    stringResource(R.string.delete)
                                                )
                                            }
                                            FilledTonalButton(
                                                onClick = {
                                                    showReplaceAudioConfirmationDialog = true
                                                }
                                            ) {
                                                Text(stringResource(R.string.replace))
                                            }
                                        }
                                        AudioPlayer(
                                            modifier = Modifier.Companion
                                                .fillMaxWidth()
                                                .height(80.dp),
                                            positionMs = 0,
                                            isPlaying = false,
                                            durationMs = 1,
                                            isLoading = false,
                                            focused = false,
                                            onPlay = { },
                                            onPause = {},
                                            onSeek = { }
                                        )

                                    }

                                }

                                AnimatedVisibility(audioRecorderIsVisible) {


                                    LaunchedEffect(viewModel.audioRecorderState.isPlaying) {
                                        while (viewModel.audioRecorderState.isPlaying) {
                                            delay(200.milliseconds)
                                            viewModel.audioRecorderState.position =
                                                mediaController.currentPosition
                                        }
                                    }

                                    AudioRecorder(
                                        modifier = Modifier.Companion.padding(16.dp),
                                        onDismiss = {

                                            if ((viewModel.audioRecorderState.isRecording || viewModel.audioToUpload != null)) {
                                                showDiscardAudioConfirmationDialog = true
                                            } else {
                                                viewModel.audioRecorderIsVisible.value = false
                                            }
                                        },
                                        onPlay = {
                                            if (mediaController.isCommandAvailable(Player.COMMAND_PREPARE)) {
                                                mediaController.prepare()
                                            }

                                            if (viewModel.ended) {
                                                mediaController.seekTo(0L)
                                            }
                                            mediaController.playWhenReady = true
                                        },
                                        onPause = {
                                            mediaController.pause()
                                        },
                                        onStartRecording = { fromPause ->

                                            if (fromPause) {
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

                                            viewModel.audioRecorderState.recordIsStopped = false
                                            viewModel.audioRecorderState.isRecording = true
                                            viewModel.audioRecorderState.recordIsPaused = false


                                        },
                                        onPauseRecording = {
                                            mediaRecorder.pause()
                                            viewModel.audioRecorderState.recordIsStopped = false
                                            viewModel.audioRecorderState.isRecording = true
                                            viewModel.audioRecorderState.recordIsPaused = true
                                        },
                                        onStopRecording = {
                                            mediaRecorder.stop()
                                            val audioUri = recordOutputFile.toUri()
                                            mediaController.setMediaItem(
                                                MediaItem.Builder()
                                                    .setUri(audioUri)
                                                    .setMediaMetadata(
                                                        MediaMetadata.Builder()
                                                            .setTitle(getString(R.string.just_recorded_audio_title))
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            viewModel.audioToUpload = audioUri
                                            viewModel.audioRecorderState.recordIsStopped = true
                                            viewModel.audioRecorderState.isRecording = false
                                            viewModel.audioRecorderState.recordIsPaused = false
                                        },
                                        state = viewModel.audioRecorderState,
                                        onSeek = { ms ->
                                            mediaController.seekTo(ms.toLong())
                                        }
                                    )
                                }


                                PostAttachments(
                                    modifier = Modifier.Companion.padding(16.dp),
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
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }

                }

            }



        }
    }

    private fun exit() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onStop() {
        super.onStop()

        MediaController.releaseFuture(mediaControllerFuture)
    }
}