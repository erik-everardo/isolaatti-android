package com.isolaatti.profile.ui

import android.content.ComponentName
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.ListenableFuture
import com.isolaatti.BuildConfig
import com.isolaatti.MyApplication
import com.isolaatti.R
import com.isolaatti.audio.common.components.AudioPlayer
import com.isolaatti.audio.common.components.AudioRecorder
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.player.MediaService
import com.isolaatti.common.Dialogs
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.followers.domain.FollowingState
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.CreatePostContract
import com.isolaatti.posting.posts.presentation.EditPostContract
import com.isolaatti.posting.posts.ui.PostInfoActivity
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.profile.presentation.EditProfileContract
import com.isolaatti.profile.presentation.ProfileViewModel
import com.isolaatti.profile.ui.components.ConfirmChangeProfilePictureBottomSheet
import com.isolaatti.profile.ui.components.ProfileHeader
import com.isolaatti.profile.ui.components.ProfilePictureBottomSheet
import com.isolaatti.profile.ui.components.ProfilePictureBottomSheetActions
import com.isolaatti.profile.ui.components.ProfilePictureState
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
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
class ProfileMainFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModels()
    val optionsViewModel: BottomSheetPostOptionsViewModel by activityViewModels()
    val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private var userId: Int? = null

    private var cameraPhotoUri: Uri? = null

    private fun makePhotoUri(): Uri {
        val cacheFile = File(requireContext().filesDir, "temp_picture_${Calendar.getInstance().timeInMillis}")
        return FileProvider.getUriForFile(requireContext(), "${MyApplication.myApp.packageName}.provider", cacheFile)
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it && cameraPhotoUri != null) {
            viewModel.onProfilePictureSelected(cameraPhotoUri!!)
        }
    }

    private val createDiscussion = registerForActivityResult(CreatePostContract()) {
        if(it == null)
            return@registerForActivityResult

        Toast.makeText(requireContext(), R.string.posted_successfully, Toast.LENGTH_SHORT).show()

        viewModel.onPostAddedAtTheBeginning(it)
    }

    private val editProfile = registerForActivityResult(EditProfileContract()) { updatedProfile ->
        if(updatedProfile != null) {
            viewModel.setProfile(updatedProfile)
        }
    }

    private val choosePictureLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if(it != null) {
            viewModel.onProfilePictureSelected(it)
        }
    }

    private val editDiscussion = registerForActivityResult(EditPostContract()) {
        if(it != null) {
            viewModel.onPostUpdate(it)
        }
    }

    private fun getData() {

        userId?.let { profileId ->
            viewModel.profileId = profileId
            viewModel.getProfile()
            viewModel.getFeed(true, null)
        }
    }

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private lateinit var mediaController: MediaController
    private lateinit var mediaRecorder: MediaRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.mediaControllerIsAvailable.value = false
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), MediaService::class.java))
        mediaControllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()

        userId = (requireActivity()).intent.extras?.getInt(ProfileActivity.EXTRA_USER_ID)
        getData()
    }

    private lateinit var recordOutputFile: File

    // initialized in onViewCreated
    private lateinit var directory: File


    private fun seekTo(ms: Float) {
        mediaController.seekTo(ms.toLong())
    }

    private fun playAudio(descriptionAudio: Audio) {

        if(viewModel.audioState.currentAudio== descriptionAudio) {
            mediaController.play()
        } else {
            val mediaItem = MediaItem.Builder()
                .setMediaId(descriptionAudio.id)
                .setUri(descriptionAudio.uri)
                .build()
            mediaController.setMediaItem(mediaItem)
            mediaController.playWhenReady = true
            mediaController.play()
            viewModel.audioState.currentAudio = descriptionAudio
        }

        viewModel.audioState.isPlaying = true

    }

    private fun pauseAudio() {
        mediaController.pause()
        viewModel.audioState.isPlaying = false
    }





    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val lazyColumState = rememberLazyListState()
                val profile by viewModel.profile.collectAsState()
                val followingState by viewModel.followingState.observeAsState()
                val posts by viewModel.posts.collectAsState()
                val loadingProfile by viewModel.loadingProfile.collectAsState()

                // use this to show or hide all audio related UI
                val mediaControllerIsAvailable by viewModel.mediaControllerIsAvailable.collectAsState()
                val isAtTop by remember {
                    derivedStateOf {
                        lazyColumState.firstVisibleItemIndex == 0 && lazyColumState.firstVisibleItemScrollOffset == 0
                    }
                }
                var showAddPostButton by remember { mutableStateOf(false) }

                val nestedScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            // Hide FAB
                            if (available.y < -1) {
                                showAddPostButton = false
                            }

                            // Show FAB
                            if (available.y > 1) {
                                showAddPostButton = true
                            }

                            return Offset.Zero
                        }
                    }
                }

                /* Profile picture bottom sheet */
                var showProfilePictureBottomSheet by remember { mutableStateOf(false) }
                val profilePictureSheetState = rememberModalBottomSheetState()
                var profilePictureState by remember { mutableStateOf(ProfilePictureState()) }

                val scope = rememberCoroutineScope()
                val showConfirmChangeProfilePicture by viewModel.showConfirmChangeProfilePictureBottomSheet.collectAsState()
                val newProfilePicture by viewModel.newProfileImage.collectAsState()
                val uploadingProfilePicture by viewModel.uploadingProfilePicture.collectAsState()

                val showAudioRecorder by viewModel.showAudioRecorder.collectAsState()

                var showRemoveAudioDialog by remember { mutableStateOf(false) }

                val noMoreContent by viewModel.noMoreContent.collectAsState()

                LaunchedEffect(profile) {
                    showAddPostButton = profile?.isUserItself == true
                    profilePictureState = ProfilePictureState(
                        showViewImage = profile?.profileImage != null,
                        showChangeProfileImage = profile?.isUserItself == true,
                        showRemoveImage = profile?.isUserItself == true
                    )
                }

                LaunchedEffect(viewModel.audioState.isPlaying) {
                    while(viewModel.audioState.isPlaying) {
                        delay(500.milliseconds)
                        viewModel.audioState.duration = mediaController.duration
                        viewModel.audioState.progress = mediaController.currentPosition
                    }
                }

                fun onAddAudioDescription() {
                    mediaRecorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(requireContext()) else MediaRecorder()
                    recordOutputFile = File(directory, "${System.currentTimeMillis()}.3gp")
                    viewModel.showAudioRecorder.value = true
                }

                val followingText = when(followingState) {
                    FollowingState.FollowingThisUser -> {
                        stringResource(R.string.following_user)
                    }
                    FollowingState.MutuallyFollowing -> {
                        stringResource(R.string.mutually_following)
                    }
                    FollowingState.ThisUserIsFollowingMe -> {
                        stringResource(R.string.following_you)
                    }
                    FollowingState.NotMutuallyFollowing -> {
                        ""
                    }

                    null -> ""
                }

                IsolaattiTheme {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            activity?.finish()
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back_button))
                                    }
                                },
                                title = {
                                    Text(if(isAtTop) stringResource(R.string.profile) else profile?.name ?: "")
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            profile?.let { findNavController().navigate(ProfileMainFragmentDirections.actionMainFragmentToQrCodeFragment(it)) }
                                        }
                                    ) {
                                        Icon(painterResource(R.drawable.baseline_qr_code_24), stringResource(R.string.qr_code))
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            AnimatedVisibility(
                                visible = showAddPostButton,
                                enter = slideInVertically(initialOffsetY = { it * 2 }),
                                exit = slideOutVertically(targetOffsetY = { it * 2 })
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        createDiscussion.launch(Unit)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, stringResource(R.string.add_post_button_desc))
                                }
                            }

                        }
                    ) { insets ->

                        if(showProfilePictureBottomSheet) {
                            ProfilePictureBottomSheet(
                                sheetState = profilePictureSheetState,
                                onDismissRequest = {
                                    showProfilePictureBottomSheet = false
                                },
                                onAction = { action ->
                                    when(action) {
                                        ProfilePictureBottomSheetActions.ViewImage -> {
                                            profile?.profileImage?.let {
                                                PictureViewerActivity.startActivityWithImages(requireContext(), arrayListOf(it))
                                            }
                                        }
                                        ProfilePictureBottomSheetActions.ChangeImage -> {
                                            choosePictureLauncher
                                                .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        }
                                        ProfilePictureBottomSheetActions.RemoveImage -> {
                                            showRemoveProfileImageDialog()
                                        }

                                        ProfilePictureBottomSheetActions.TakeAPicture -> {
                                            cameraPhotoUri = makePhotoUri()
                                            takePhotoLauncher.launch(cameraPhotoUri)
                                        }
                                    }
                                    scope.launch { profilePictureSheetState.hide() }.invokeOnCompletion {
                                        if (!profilePictureSheetState.isVisible) {
                                            showProfilePictureBottomSheet = false
                                        }
                                    }
                                },
                                state = profilePictureState
                            )
                        }

                        if(showConfirmChangeProfilePicture) {
                            newProfilePicture?.let {
                                ConfirmChangeProfilePictureBottomSheet(
                                    onDismiss = {
                                        viewModel.hideConfirmPictureSheet()
                                    },
                                    oldImage = profile?.profileImage?.imageUrl?.toUri(),
                                    newImage = it,
                                    onAccept = {
                                        viewModel.setProfileImage()
                                    },
                                    loading = uploadingProfilePicture
                                )
                            }
                        }

                        if(showRemoveAudioDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    showRemoveAudioDialog = false
                                },
                                text = {
                                    Text(stringResource(R.string.delete_profile_audio))
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.removeAudioProfile()
                                            showRemoveAudioDialog = false
                                        }
                                    ) {
                                        Text(stringResource(R.string.delete))
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showRemoveAudioDialog = false
                                        }
                                    ) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                }
                            )
                        }

                        if(showAudioRecorder) {
                            val setAudioRecorderModalBottomSheetState = rememberModalBottomSheetState()

                            LaunchedEffect(viewModel.audioRecorderState.isPlaying) {
                                while(viewModel.audioRecorderState.isPlaying) {
                                    delay(500.milliseconds)
                                    viewModel.audioRecorderState.position = mediaController.currentPosition
                                }
                            }


                            fun dismiss() {
                                viewModel.showAudioRecorder.value = false

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
                                viewModel.audioDescriptionToUpload = null
                            }

                            ModalBottomSheet(
                                onDismissRequest = {
                                    viewModel.showAudioRecorder.value = false
                                    dismiss()
                                },
                                sheetState = setAudioRecorderModalBottomSheetState
                            ) {
                                Text(
                                    text = stringResource(R.string.set_audio_description),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp
                                )
                                val error by viewModel.setAudioDescriptionError.collectAsState()

                                if(error) {
                                    Box(
                                        Modifier.background(MaterialTheme.colorScheme.errorContainer)
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Text(stringResource(R.string.error_uploading_audio), modifier = Modifier.padding(8.dp))
                                    }
                                }
                                AudioRecorder(
                                    onDismiss = { dismiss() },
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
                                        viewModel.audioDescriptionToUpload = audioUri
                                        viewModel.audioRecorderState.recordIsStopped = true
                                        viewModel.audioRecorderState.isRecording = false
                                        viewModel.audioRecorderState.recordIsPaused = false
                                        viewModel.audioRecorderState.durationMilliseconds = mediaController.duration
                                    },
                                    onPauseRecording = {
                                        mediaRecorder.pause()
                                        viewModel.audioRecorderState.recordIsStopped = false
                                        viewModel.audioRecorderState.isRecording = true
                                        viewModel.audioRecorderState.recordIsPaused = true
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

                                        viewModel.audioRecorderState.recordIsStopped = false
                                        viewModel.audioRecorderState.isRecording = true
                                        viewModel.audioRecorderState.recordIsPaused = false
                                    },
                                    state = viewModel.audioRecorderState,
                                    onSeek = { ms ->
                                        mediaController.seekTo(ms.toLong())
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                                )

                                AnimatedVisibility(visible = viewModel.audioRecorderState.recordIsStopped) {
                                    val uploading by viewModel.uploadingAudio.collectAsState()

                                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.audioRecorderState.clearState()
                                                mediaController.clearMediaItems()

                                                // delete file
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    if(recordOutputFile.exists()) {
                                                        recordOutputFile.delete()
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(stringResource(R.string.reset))
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            enabled = !uploading,
                                            onClick = {
                                                viewModel.setAudioDescriptionProfile()
                                            }
                                        ) {
                                            if(uploading) {
                                                CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize))
                                            }
                                            Text(stringResource(R.string.save))
                                        }
                                    }
                                }

                            }


                        }

                        PullToRefreshBox(
                            isRefreshing,
                            onRefresh = {
                                viewModel.getProfile()
                                viewModel.getFeed(refresh = true, hashtag = null)
                            },
                            modifier = Modifier
                                .padding(insets)
                                .consumeWindowInsets(insets)
                        ) {
                            LazyColumn(
                                state = lazyColumState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if(profile?.isUserItself == true) { Modifier.nestedScroll(nestedScrollConnection) } else Modifier)
                            ) {
                                item {
                                    profile?.let {
                                        ProfileHeader(
                                            modifier = Modifier.padding(16.dp),
                                            name = it.name,
                                            description = it.descriptionText,
                                            image = it.profileImage,
                                            audio = it.descriptionAudio,
                                            followingCount = it.numberOfFollowing,
                                            followerCount = it.numberOfFollowers,
                                            loading = loadingProfile,
                                            onImageClick = {
                                                showProfilePictureBottomSheet = true
                                            },
                                            onFollowersClick = {
                                                findNavController().navigate(ProfileMainFragmentDirections.actionDiscussionsFragmentToMainFollowersFragment(it.userId))
                                            },
                                            followingText = followingText,
                                            followingThisUser = it.followingThisUser,
                                            isOwnUser = it.isUserItself,
                                            onFollowChange = { follow ->
                                                viewModel.followUser()
                                            },
                                            audioPlayer = { descriptionAudio ->
                                                if(mediaControllerIsAvailable) {
                                                    AudioPlayer(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        positionMs = if(viewModel.audioState.currentAudio == descriptionAudio) viewModel.audioState.progress else 0,
                                                        isPlaying = viewModel.audioState.isPlaying && (viewModel.audioState.currentAudio == it.descriptionAudio),
                                                        durationMs = viewModel.audioState.duration,
                                                        isLoading = false,
                                                        focused = descriptionAudio == viewModel.audioState.currentAudio,
                                                        onPlay = {
                                                            playAudio(descriptionAudio)
                                                        },
                                                        onPause = {
                                                            pauseAudio()
                                                        },
                                                        onSeek = { ms ->
                                                            seekTo(ms)
                                                        },
                                                        options = {
                                                            if(it.isUserItself) {
                                                                var expanded by remember { mutableStateOf(false) }

                                                                Box {
                                                                    FilledTonalIconButton(
                                                                        modifier = Modifier.padding(4.dp),
                                                                        onClick = {
                                                                            expanded = true
                                                                        }
                                                                    ) {
                                                                        Icon(Icons.Default.MoreVert, stringResource(R.string.audio_options))
                                                                    }

                                                                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                                                                        DropdownMenuItem(
                                                                            text = { Text(stringResource(R.string.set_audio_description)) },
                                                                            onClick = {
                                                                                expanded = false
                                                                                onAddAudioDescription()
                                                                            }
                                                                        )
                                                                        DropdownMenuItem(
                                                                            text = { Text(stringResource(R.string.remove)) },
                                                                            onClick = {
                                                                                showRemoveAudioDialog = true
                                                                                expanded = false

                                                                            }
                                                                        )
                                                                    }
                                                                }

                                                            }
                                                        }
                                                    )
                                                }

                                            },
                                            onEditProfileClick = {
                                                editProfile.launch(it)
                                            },
                                            onAddAudioDescription = {
                                                onAddAudioDescription()
                                            }
                                        )
                                    }
                                }

                                if(posts.isEmpty()) {
                                    item {
                                        Text(getString(R.string.you_will_see_what_user_posts_here, profile?.name ?: ""), modifier = Modifier.padding(24.dp))
                                    }
                                }

                                items(count = posts.size, key = {posts[it].id}) {
                                    val post = posts[it]
                                    PostComponent(
                                        modifier = Modifier.animateItem(),
                                        post = post,
                                        onClick = {
                                            PostViewerActivity.startActivity(
                                                requireContext(),
                                                post.id
                                            )
                                        },
                                        onComments = {
                                            val modalBottomSheet =
                                                BottomSheetPostComments.getInstance(post.id)
                                            modalBottomSheet.show(
                                                requireActivity().supportFragmentManager,
                                                BottomSheetPostComments.TAG
                                            )
                                        },
                                        onOptions = {
                                            optionsViewModel.setOptions(
                                                Options.POST_OPTIONS,
                                                CALLER_ID,
                                                post
                                            )
                                            val modalBottomSheet = BottomSheetPostOptionsFragment()
                                            modalBottomSheet.show(
                                                requireActivity().supportFragmentManager,
                                                BottomSheetPostOptionsFragment.TAG
                                            )
                                        },
                                        onInfo = {
                                            PostInfoActivity.startActivity(
                                                requireContext(),
                                                post.id
                                            )
                                        },
                                        onLike = {
                                            viewModel.likePost(post.id)
                                        },
                                        onDislike = {
                                            viewModel.unLikePost(post.id)
                                        },
                                        onShare = {
                                            val intent = Intent.createChooser(Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "${BuildConfig.backend}/pub/${post.id}"
                                                )
                                                type = "text/plain"
                                            }, getString(R.string.share_post))
                                            startActivity(intent)
                                        },
                                        onImageClick = { images, index ->
                                            PictureViewerActivity.startActivityWithImages(
                                                requireContext(),
                                                ArrayList(images),
                                                index
                                            )
                                        },
                                        onUsernameClick = {
                                            if (profile?.userId == post.userId) return@PostComponent
                                            ProfileActivity.startActivity(
                                                requireContext(),
                                                post.userId
                                            )
                                        },
                                        onHashtagClick = { hashtag ->
                                            HashtagsPostsActivity.startActivity(
                                                requireContext(),
                                                hashtag
                                            )
                                        },
                                        isCurrentlyPlaying = post.audio == viewModel.audioState.currentAudio,
                                        audioIsPlaying = viewModel.audioState.isPlaying,
                                        duration = if(viewModel.audioState.currentAudio == post.audio) viewModel.audioState.duration else viewModel.audioState.duration,
                                        progress = if(viewModel.audioState.currentAudio == post.audio) viewModel.audioState.progress else 0,
                                        onPlay = {
                                            post.audio?.let { playAudio(it) }
                                        },
                                        onPause = {
                                            pauseAudio()
                                        },
                                        onSeek = {
                                            seekTo(it)
                                        },
                                    )
                                }

                                if(!noMoreContent && !isRefreshing) {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            Button(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                onClick = {
                                                    viewModel.getFeed(false, null)
                                                }
                                            ) {
                                                Text(stringResource(R.string.load_more))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val playerListener: Player.Listener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModel.audioRecorderState.isPlaying = isPlaying
            viewModel.audioState.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when(playbackState) {
                Player.STATE_READY -> {
                    viewModel.ended = false
                    viewModel.audioRecorderState.isLoading = false
                    viewModel.audioRecorderState.durationMilliseconds = this@ProfileMainFragment.mediaController.duration
                }
                Player.STATE_ENDED -> {
                    viewModel.ended = true
                    viewModel.audioState.progress = 0
                    viewModel.audioState.currentAudio = null
                }
                Player.STATE_BUFFERING -> {
                    viewModel.audioRecorderState.isLoading = true
                }

                Player.STATE_IDLE -> {}
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            mediaController = mediaControllerFuture.await()
            viewModel.mediaControllerIsAvailable.value = true

            mediaController.addListener(playerListener)

            // check audios directory
            directory = File(requireContext().filesDir, "recordings")

            withContext(Dispatchers.IO) {
                if(!directory.isDirectory) {
                    if(directory.isFile) {
                        directory.delete()
                    }
                    directory.mkdir()
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.retry.collect {
                    viewModel.retry()
                    errorViewModel.handleRetry()
                }
            }
        }

        optionsViewModel.optionClicked.observe(viewLifecycleOwner) { optionClicked ->
            if(optionClicked?.callerId == CALLER_ID) {
                Log.d("ProfileMainFragment", optionClicked.toString())

                when(optionClicked.optionsId) {
                    Options.POST_OPTIONS -> {
                        // post id should come as payload
                        val post = optionClicked.payload as? Post ?: return@observe
                        when(optionClicked.optionId) {
                            Options.Option.OPTION_DELETE -> {
                                Dialogs.buildDeletePostDialog(requireContext()) { delete ->
                                    optionsViewModel.handle()
                                    if(delete) {
                                        viewModel.deletePost(post.id)
                                    }
                                }.show()

                            }
                            Options.Option.OPTION_EDIT -> {
                                optionsViewModel.handle()
                                editDiscussion.launch(post.id)
                            }
                            Options.Option.OPTION_REPORT -> {
                                NewReportBottomSheetDialogFragment
                                    .newInstance(ContentType.Post, post.id.toString())
                                    .show(childFragmentManager, NewReportBottomSheetDialogFragment.LOG_TAG)
                                optionsViewModel.handle()
                            }
                        }
                    }
                    Options.PROFILE_DESCRIPTION_OPTIONS -> {
                        optionsViewModel.handle()
                    }
                }


            }

        }
    }

    private fun showRemoveProfileImageDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.remove_image_message_confirmation)
            .setTitle(R.string.remove_photo)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.yes_continue) { _, _ ->
                viewModel.removeCurrentProfileImage()
            }
            .show()
    }
    companion object {
        const val CALLER_ID = 30
        const val LOG_TAG = "ProfileMainFragment"
    }
}