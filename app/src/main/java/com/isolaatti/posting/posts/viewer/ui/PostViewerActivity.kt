package com.isolaatti.posting.posts.viewer.ui

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.TaskStackBuilder
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.viewer.presentation.PostViewerViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.isolaatti.BuildConfig
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.player.MediaService
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.PostEvents
import com.isolaatti.posting.comments.ui.components.CommentComponent
import com.isolaatti.posting.posts.components.PostParticipants
import com.isolaatti.posting.posts.ui.PostInfoActivity
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PostViewerActivity : IsolaattiBaseActivity() {
    companion object {
        const val POST_ID = "postId"
        const val LOG_TAG = "PostViewerActivity"

        fun getIntent(context: Context, postId: Long): Intent {
            return Intent(context, PostViewerActivity::class.java).apply {
                putExtra(POST_ID, postId)
            }
        }
        fun startActivity(context: Context, postId: Long) {
            context.startActivity(getIntent(context, postId))
        }

        fun getPendingIntent(context: Context, postId: Long): PendingIntent? {
            return TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(getIntent(context, postId))

                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
        }
    }


    private val viewModel: PostViewerViewModel by viewModels()
    private var postId: Long = 0
    private lateinit var mediaController: MediaController
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent?.getLongExtra(POST_ID, 0)?.let {
            postId = it
            viewModel.postId = postId
            viewModel.getPost()
        }
    }

    private val playerListener: Player.Listener = object: Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModel.audioState.isPlaying = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when(playbackState) {
                Player.STATE_READY -> {
                    viewModel.ended = false
                }
                Player.STATE_ENDED -> {
                    viewModel.ended = true
                    viewModel.audioState.progress = 0
                    viewModel.audioState.currentAudio = null
                }
                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {}
            }
        }
    }


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
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val sessionToken = SessionToken(this@PostViewerActivity, ComponentName(this@PostViewerActivity, MediaService::class.java))
            mediaControllerFuture = MediaController.Builder(this@PostViewerActivity, sessionToken).buildAsync()

            mediaController = mediaControllerFuture.await()

            mediaController.addListener(playerListener)

            postId = intent.getLongExtra(POST_ID, 0)
            Log.d(LOG_TAG, "Post id: $postId")


            if(postId!! > 0) {
                viewModel.postId = postId
                viewModel.getPost()
                viewModel.getComments(true)
            }

            setContent {
                val state by viewModel.state.collectAsState()

                val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))

                LaunchedEffect(viewModel.audioState.isPlaying) {
                    while(viewModel.audioState.isPlaying) {
                        delay(500.milliseconds)
                        viewModel.audioState.duration = mediaController.duration
                        viewModel.audioState.progress = mediaController.currentPosition
                    }
                }



                val coroutineScope = rememberCoroutineScope()

                val commentsLazyListState = rememberLazyListState()

                BackHandler(enabled = bottomSheetScaffoldState.bottomSheetState.isVisible) {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }
                }

                val bottomSheetMaxPeekHeight = when(bottomSheetScaffoldState.bottomSheetState.currentValue) {
                    SheetValue.Hidden -> 0.dp
                    SheetValue.Expanded -> 240.dp
                    SheetValue.PartiallyExpanded -> 240.dp
                }

                var commentsPositionToHighlight: Int? by remember { mutableStateOf(null) }

                LaunchedEffect(Unit) {
                    viewModel.events.collect {
                        when(val event = it) {
                            PostEvents.CommentEditError -> {
                                bottomSheetScaffoldState.snackbarHostState
                                    .showSnackbar(getString(R.string.comment_failed_to_edit))
                            }
                            is PostEvents.CommentEdited -> {
                                bottomSheetScaffoldState.snackbarHostState.currentSnackbarData?.dismiss()


                                val snackbarResult = bottomSheetScaffoldState.snackbarHostState
                                    .showSnackbar(
                                        message = getString(R.string.comment_edited),
                                        actionLabel = if(event.position == -1) null else getString(R.string.go_to_comment),
                                        duration = SnackbarDuration.Long
                                    )

                                if(snackbarResult == SnackbarResult.ActionPerformed) {
                                    commentsLazyListState.animateScrollToItem(event.position)
                                    commentsPositionToHighlight = event.position
                                }
                            }
                            PostEvents.CommentRemoveError -> {
                                bottomSheetScaffoldState.snackbarHostState
                                    .showSnackbar(getString(R.string.comment_failed_to_remove))
                            }
                            PostEvents.CommentRemoved -> {
                                bottomSheetScaffoldState.snackbarHostState
                                    .showSnackbar(getString(R.string.comment_removed))
                            }

                            is PostEvents.CommentPosted -> {
                                bottomSheetScaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                                val snackbarResult = bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                    actionLabel = if(event.position == -1) null else getString(R.string.go_to_comment),
                                    message = getString(R.string.comment_posted),
                                    duration = SnackbarDuration.Long
                                )

                                if(snackbarResult == SnackbarResult.ActionPerformed) {
                                    commentsLazyListState.animateScrollToItem(event.position)
                                    commentsPositionToHighlight = event.position
                                }

                            }

                            PostEvents.PostingComment -> {
                                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                    message = getString(R.string.posting_comment),
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                            PostEvents.EditingComment -> {
                                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                    message = getString(R.string.posting_comment),
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }
                    }
                }
                IsolaattiTheme {
                    BottomSheetScaffold(
                        scaffoldState = bottomSheetScaffoldState,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(stringResource(R.string.discussion))
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            finish()
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, null)
                                    }
                                }
                            )
                        },
                        sheetPeekHeight = bottomSheetMaxPeekHeight,
                        sheetMaxWidth = Dp.Unspecified,
                        snackbarHost = {
                            SnackbarHost(bottomSheetScaffoldState.snackbarHostState, modifier = Modifier.navigationBarsPadding())
                        },
                        sheetContent = {

                            Column(modifier = Modifier.fillMaxHeight().navigationBarsPadding()) {
                                Text(
                                    text = stringResource(R.string.comments),
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleLarge
                                )


                                PullToRefreshBox(
                                    isRefreshing = state.refreshingComments,
                                    onRefresh = { viewModel.getComments(true) },
                                    modifier = Modifier.weight(1f, true).fillMaxWidth()
                                ) {

                                    LazyColumn(state = commentsLazyListState) {
                                        if(state.comments.isNotEmpty()) {

                                            items(count = state.comments.size, key = { state.comments[it].id }) { index ->
                                                val comment = state.comments[index]
                                                CommentComponent(
                                                    comment = comment,
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).animateItem(),
                                                    onDelete = {
                                                        viewModel.setCommentToDelete(comment)
                                                    },
                                                    onEdit = {
                                                        viewModel.setCommentToEdit(comment)
                                                    },
                                                    onReport = {
                                                        NewReportBottomSheetDialogFragment
                                                            .newInstance(ContentType.Comment, comment.id.toString())
                                                            .show(supportFragmentManager, NewReportBottomSheetDialogFragment.LOG_TAG)
                                                    },
                                                    onProfileClick = {
                                                        ProfileActivity.startActivity(this@PostViewerActivity, comment.userId)
                                                    },
                                                    highligth = commentsPositionToHighlight == index,
                                                    finishedHighlight = {
                                                        commentsPositionToHighlight = null
                                                    }
                                                )
                                            }
                                            if(state.loadingComments) {
                                                item {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                        CircularProgressIndicator()
                                                    }
                                                }
                                            }
                                            if(state.noMoreComments != null) {
                                                item {
                                                    if(state.noMoreComments != true) {
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                            Button(
                                                                enabled = !state.loadingComments,
                                                                onClick = {
                                                                    viewModel.getComments(false)
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

                                val commentText by viewModel.commentText.collectAsState()
                                val commentInEdit by viewModel.commentInEdit.collectAsState()


                                AnimatedVisibility(commentInEdit) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            modifier = Modifier.weight(1f).padding(start = 16.dp),
                                            text = stringResource(R.string.edit_comment_here),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        IconButton(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            onClick = {
                                                viewModel.cancelCommentToEdit()
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = commentText,
                                        enabled = !state.uploadingComment,
                                        onValueChange = {
                                            viewModel.commentText.value = it
                                        },
                                        label = { Text(stringResource(R.string.comment)) },
                                        modifier = Modifier.padding(16.dp).weight(1f),
                                    )
                                    IconButton(
                                        modifier = Modifier.padding(16.dp),
                                        enabled = commentText.isNotBlank() && !state.uploadingComment,
                                        onClick = {
                                            if(viewModel.commentInEdit.value) {
                                                viewModel.editComment()
                                            } else {
                                                viewModel.postComment()
                                            }

                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Default.Send, stringResource(R.string.send))
                                    }
                                }
                            }


                        }
                    ) {

                        if(state.showDeleteCommentDialog) {
                            AlertDialog(
                                title = {
                                    Text(stringResource(R.string.delete_comment_question))
                                },
                                dismissButton = {
                                    TextButton(
                                        enabled = !state.deletingComment,
                                        onClick = {
                                            viewModel.cancelCommentToDelete()
                                        }
                                    ) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        enabled = !state.deletingComment,
                                        onClick = {
                                            viewModel.deleteComment()
                                        }
                                    ) {
                                        if(state.deletingComment) {
                                            Text(stringResource(R.string.deleting_please_wait))
                                        } else {
                                            Text(stringResource(R.string.accept))
                                        }

                                    }
                                },
                                onDismissRequest = {
                                    viewModel.cancelCommentToDelete()
                                }
                            )
                        }

                        PullToRefreshBox(
                            isRefreshing = state.postLoading,
                            onRefresh = {
                                viewModel.refresh()
                            },
                            modifier = Modifier.padding(it).fillMaxSize()
                        ) {
                            LazyColumn() {
                                state.post?.let {  post ->
                                    item {
                                        PostComponent(
                                            modifier = Modifier.animateItem(),
                                            post = post,
                                            onOptions = {

                                            },
                                            onComments = {
                                                coroutineScope.launch {
                                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                                }
                                            },
                                            onInfo = {
                                                PostInfoActivity.startActivity(this@PostViewerActivity, post.id)
                                            },
                                            onLike = {
                                                viewModel.likePost()
                                            },
                                            onDislike = {
                                                viewModel.unLikePost()
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
                                                PictureViewerActivity.startActivityWithImages(this@PostViewerActivity, ArrayList(images), index)
                                            },
                                            onUsernameClick = {
                                                ProfileActivity.startActivity(this@PostViewerActivity, post.userId)
                                            },
                                            onHashtagClick = { hashtag ->
                                                HashtagsPostsActivity.startActivity(this@PostViewerActivity, hashtag)
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
                                }

                                state.post?.participants?.let {
                                    if(it.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = stringResource(R.string.post_participants),
                                                modifier = Modifier.padding(8.dp),
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                        item {
                                            PostParticipants(
                                                participants = it,
                                                onOpenProfile = {
                                                    ProfileActivity.startActivity(this@PostViewerActivity, it.id)
                                                },
                                                onFollow = {

                                                }
                                            )
                                        }
                                    }
                                }

                                item {
                                    Spacer(Modifier.navigationBarsPadding())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}