package com.isolaatti.posting.viewer.ui

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import com.isolaatti.posting.viewer.presentation.PostViewerViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.isolaatti.BuildConfig
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.player.MediaService
import com.isolaatti.common.Dialogs
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.common.options_bottom_sheet.domain.OptionClicked
import com.isolaatti.common.options_bottom_sheet.domain.Options
import com.isolaatti.common.options_bottom_sheet.presentation.BottomSheetPostOptionsViewModel
import com.isolaatti.common.options_bottom_sheet.ui.BottomSheetPostOptionsFragment
import com.isolaatti.hashtags.ui.HashtagsPostsActivity
import com.isolaatti.images.picture_viewer.ui.PictureViewerActivity
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.viewer.presentation.PostEvents
import com.isolaatti.posting.comments.ui.components.CommentComponent
import com.isolaatti.posting.posts.components.PostParticipants
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.post_creator.presentation.EditPostContract
import com.isolaatti.posting.posts.ui.PostInfoActivity
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.ui.NewReportBottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.collections.flatten
import kotlin.getValue
import kotlin.time.Duration.Companion.milliseconds


class PostViewerActivity : IsolaattiBaseActivity() {
    companion object {
        const val POST_ID = "postId"
        const val OPEN_COMMENTS_FULLY = "commentsFully"
        const val LOG_TAG = "PostViewerActivity"

        const val CALLER_ID = 50

        fun getIntent(context: Context, postId: Long, openCommentsFully: Boolean = false): Intent {
            return Intent(context, PostViewerActivity::class.java).apply {
                putExtra(POST_ID, postId)
                putExtra(OPEN_COMMENTS_FULLY, openCommentsFully)
            }
        }
        fun startActivity(context: Context, postId: Long, openCommentsFully: Boolean = false) {
            context.startActivity(getIntent(context, postId, openCommentsFully))
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

    val optionsViewModel: BottomSheetPostOptionsViewModel by viewModels()

    private val editDiscussion = registerForActivityResult(EditPostContract()) {
        if(it != null) {
            viewModel.onPostUpdate(it)
        }
    }

    private val optionsObserver: Observer<OptionClicked?> = Observer { optionClicked ->
        if(optionClicked?.callerId == CALLER_ID) {
            // post id should come as payload
            val post = optionClicked.payload as? Post ?: return@Observer
            when(optionClicked.optionId) {
                Options.Option.OPTION_DELETE -> {
                    Dialogs.buildDeletePostDialog(this) { delete ->
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
                        .show(supportFragmentManager, NewReportBottomSheetDialogFragment.LOG_TAG)
                }
            }
        }
    }

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

        optionsViewModel.optionClicked.observe(this, optionsObserver)

        lifecycleScope.launch {
            val sessionToken = SessionToken(this@PostViewerActivity, ComponentName(this@PostViewerActivity, MediaService::class.java))
            mediaControllerFuture = MediaController.Builder(this@PostViewerActivity, sessionToken).buildAsync()

            mediaController = mediaControllerFuture.await()

            mediaController.addListener(playerListener)

            postId = intent.getLongExtra(POST_ID, 0)
            Log.d(LOG_TAG, "Post id: $postId")


            if(postId!! > 0 && !viewModel.initialized) {
                viewModel.postId = postId
                viewModel.getPost()
                viewModel.getComments(true)
                viewModel.initialized = true
            }

            setContent {
                val state by viewModel.state.collectAsState()

                var showCommentsFully by rememberSaveable { mutableStateOf(intent.getBooleanExtra(OPEN_COMMENTS_FULLY, false)) }

                val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))

                LaunchedEffect(Unit) {
                    if(showCommentsFully) {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                        showCommentsFully = false
                    }
                }

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

                                    viewModel.setCommentToHighlight(event.position)
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
                                    viewModel.setCommentToHighlight(event.position)
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

                            PostEvents.PostDeleted -> {
                                Toast.makeText(this@PostViewerActivity, R.string.post_deleted, Toast.LENGTH_SHORT).show()
                                finish()
                            }

                            PostEvents.ErrorDeletingPost -> {
                                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                                    message = getString(R.string.error_deleting_post)
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
                                            items(count = state.comments.size, key = { state.comments[it].id}) { commentIndex ->
                                                val comment = state.comments[commentIndex]
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                                        highligth = state.commentPositionToHighlight == commentIndex,
                                                        finishedHighlight = {
                                                            viewModel.setCommentToHighlight(null)
                                                        },
                                                        onReply = {
                                                            viewModel.setCommentToReply(comment)
                                                        },
                                                        onShowReplies = {
                                                            viewModel.loadReplies(comment)
                                                        },
                                                        showActions = true,
                                                        allowReply = comment.allowReplies,
                                                        nestingLevel = comment.nestedLevel
                                                    )
                                                    if(comment.loadingReplies) {
                                                        CircularProgressIndicator()
                                                        Text(stringResource(R.string.loading_replies))
                                                    }
                                                }

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


                                state.commentToReply?.let { commentToReply ->
                                    OutlinedCard() {
                                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.write_reply_to),
                                                    modifier = Modifier.fillMaxWidth().weight(1f)
                                                )
                                                IconButton(onClick = {
                                                    viewModel.setCommentToReply(null)
                                                }) {
                                                    Icon(Icons.Default.Close, stringResource(R.string.close))
                                                }
                                            }
                                            CommentComponent(
                                                comment = commentToReply,
                                                modifier = Modifier.padding(8.dp),
                                                highligth = false,
                                                showActions = false,
                                                finishedHighlight = {}
                                            )
                                        }
                                    }

                                }

                                state.commentToEdit?.let { commentToEdit ->
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
                                        value = state.commentBoxText,
                                        enabled = !state.uploadingComment,
                                        onValueChange = {
                                            viewModel.onCommentBoxTextChange(it)
                                        },
                                        label = { Text(stringResource(R.string.comment)) },
                                        modifier = Modifier.padding(16.dp).weight(1f),
                                    )
                                    IconButton(
                                        modifier = Modifier.padding(16.dp),
                                        enabled = state.commentBoxText.isNotBlank() && !state.uploadingComment,
                                        onClick = {
                                            // TODO refactor: move this logic to view model
                                            if(state.commentToEdit != null) {
                                                viewModel.editComment()
                                            } else if(state.commentToReply != null) {
                                                viewModel.reply()
                                            }else {
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
                                                optionsViewModel.setOptions(Options.POST_OPTIONS, CALLER_ID, post)
                                                val modalBottomSheet = BottomSheetPostOptionsFragment()
                                                modalBottomSheet.show(supportFragmentManager, BottomSheetPostOptionsFragment.TAG)
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
                                            isCurrentlyPlaying = viewModel.audioState.isPlaying && post.audio == viewModel.audioState.currentAudio,
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
                                                onFollow = { profile ->
                                                    viewModel.toggleFollowParticipant(profile)
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