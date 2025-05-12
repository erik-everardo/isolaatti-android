package com.isolaatti.posting.posts.ui

import android.content.ComponentName
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.isolaatti.BuildConfig
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.player.MediaService
import com.isolaatti.common.Dialogs
import com.isolaatti.common.ErrorMessageViewModel
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.common.options_bottom_sheet.domain.OptionClicked
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.home.ui.FeedFragment
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.comments.ui.BottomSheetPostComments
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.EditPostContract
import com.isolaatti.posting.posts.presentation.PostListingViewModel
import com.isolaatti.posting.posts.viewer.ui.PostViewerActivity
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class PostListingFragment : Fragment() {
    companion object {
        const val ARG_HASHTAG = "hashtag"
        const val LOG_TAG = "PostListingFragment"
    }

    var hashtag: String? = null

    private val errorViewModel: ErrorMessageViewModel by activityViewModels()
    private val viewModel: PostListingViewModel by viewModels()
    val optionsViewModel: BottomSheetPostOptionsViewModel by activityViewModels()

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private lateinit var mediaController: MediaController
    private lateinit var mediaRecorder: MediaRecorder

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate ; hashtag: $hashtag")
        hashtag = arguments?.getString(ARG_HASHTAG)
        viewModel.getFeed(false, hashtag)

        viewModel.mediaControllerIsAvailable.value = false
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), MediaService::class.java))
        mediaControllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
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
                val isRefreshing by viewModel.refreshing.collectAsState()
                val posts by viewModel.posts.collectAsState(emptyList())
                val noMoreContent by viewModel.noMoreContent.collectAsState()
                val loading by viewModel.loadingPosts.collectAsState()

                LaunchedEffect(viewModel.audioState.isPlaying) {
                    while(viewModel.audioState.isPlaying) {
                        delay(500.milliseconds)
                        viewModel.audioState.duration = mediaController.duration
                        viewModel.audioState.progress = mediaController.currentPosition
                    }
                }

                IsolaattiTheme {
                    PullToRefreshBox(isRefreshing, onRefresh = {viewModel.getFeed(true, null)}) {
                        LazyColumn(flingBehavior = ScrollableDefaults.flingBehavior()) {
                            items(count = posts.size, key = {posts[it].id}) {
                                val post = posts[it]
                                PostComponent(
                                    modifier = Modifier.animateItem(),
                                    post = post,
                                    onClick = {
                                        PostViewerActivity.startActivity(requireContext(), post.id)
                                    },
                                    onComments = {
                                        val modalBottomSheet = BottomSheetPostComments.getInstance(post.id)
                                        modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostComments.TAG)
                                    },
                                    onOptions = {
                                        optionsViewModel.setOptions(Options.POST_OPTIONS, FeedFragment.CALLER_ID, post)
                                        val modalBottomSheet = BottomSheetPostOptionsFragment()
                                        modalBottomSheet.show(requireActivity().supportFragmentManager, BottomSheetPostOptionsFragment.TAG)
                                    },
                                    onInfo = {
                                        PostInfoActivity.startActivity(requireContext(), post.id)
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
                                            putExtra(Intent.EXTRA_TEXT, "${BuildConfig.backend}/pub/${post.id}")
                                            type = "text/plain"
                                        }, getString(R.string.share_post))
                                        startActivity(intent)
                                    },
                                    onImageClick = { images, index ->
                                        PictureViewerActivity.startActivityWithImages(requireContext(), images.toTypedArray(), index)
                                    },
                                    onUsernameClick = {
                                        ProfileActivity.startActivity(requireContext(), post.userId)
                                    },
                                    onHashtagClick = { hashtag ->
                                        HashtagsPostsActivity.startActivity(requireContext(), hashtag)
                                    },
                                    onPlay = {
                                        post.audio?.let { playAudio(it) }
                                    },
                                    onPause = {
                                        pauseAudio()
                                    },
                                    onSeek = {
                                        seekTo(it)
                                    },
                                    isCurrentlyPlaying = post.audio == viewModel.audioState.currentAudio,
                                    audioIsPlaying = viewModel.audioState.isPlaying,
                                    duration = if(viewModel.audioState.currentAudio == post.audio) viewModel.audioState.duration else viewModel.audioState.duration,
                                    progress = if(viewModel.audioState.currentAudio == post.audio) viewModel.audioState.progress else 0,
                                )
                            }

                            if(loading) {
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
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
                    viewModel.audioRecorderState.durationMilliseconds = this@PostListingFragment.mediaController.duration
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

        viewModel.errorLoading.observe(viewLifecycleOwner) {
            errorViewModel.error.postValue(it)
        }

        optionsViewModel.optionClicked.observe(viewLifecycleOwner, optionsObserver)
    }


    private val editDiscussion = registerForActivityResult(EditPostContract()) {
        if(it != null) {
            viewModel.onPostUpdate(it)
        }
    }

    private val optionsObserver: Observer<OptionClicked?> = Observer { optionClicked ->
        if(optionClicked?.callerId == FeedFragment.CALLER_ID) {
            // post id should come as payload
            val post = optionClicked.payload as? Post ?: return@Observer
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
                    optionsViewModel.handle()
                    NewReportBottomSheetDialogFragment.newInstance(ContentType.Post, post.id.toString())
                        .show(childFragmentManager, NewReportBottomSheetDialogFragment.LOG_TAG)
                }
            }
        }
    }


}