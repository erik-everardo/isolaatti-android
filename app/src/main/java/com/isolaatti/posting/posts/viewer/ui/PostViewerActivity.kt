package com.isolaatti.posting.posts.viewer.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.TaskStackBuilder
import com.isolaatti.R
import com.isolaatti.common.IsolaattiBaseActivity
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.viewer.presentation.PostViewerViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.comments.ui.components.CommentComponent
import com.isolaatti.posting.posts.components.PostParticipants
import java.time.ZonedDateTime

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent?.getLongExtra(POST_ID, 0)?.let {
            postId = it
            viewModel.postId = postId
            viewModel.getPost()
        }
    }

    private fun exit() {

    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        postId = intent.getLongExtra(POST_ID, 0)
        Log.d(LOG_TAG, "Post id: $postId")


        if(postId!! > 0) {
            viewModel.postId = postId
            viewModel.getPost()
            viewModel.getComments(true)
        }

        setContent {



            val post by viewModel.post.collectAsState()
            val comments by viewModel.comments.collectAsState()
            val noMoreContent by viewModel.noMoreComments.collectAsState()
            val loadingComments by viewModel.loadingComments.collectAsState()
            val postLoading by viewModel.postLoading.collectAsState()

            IsolaattiTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.discussion))
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {}
                                ) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(modifier = Modifier.height(140.dp)) {
                            val commentText by viewModel.commentText.collectAsState()
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = {
                                    viewModel.commentText.value = it
                                },
                                label = { Text(stringResource(R.string.comment)) },
                                modifier = Modifier.padding(16.dp).weight(1f),
                            )
                            IconButton(
                                modifier = Modifier.padding(16.dp),
                                enabled = commentText.isNotBlank(),
                                onClick = {

                                }
                            ) {
                                Icon(Icons.AutoMirrored.Default.Send, stringResource(R.string.send))
                            }
                        }
                    }
                ) {
                    PullToRefreshBox(
                        isRefreshing = postLoading,
                        onRefresh = {
                            viewModel.refresh()
                        },
                        modifier = Modifier.padding(it).fillMaxSize()
                    ) {
                        LazyColumn() {
                            post?.let {
                                item {
                                    PostComponent(
                                        post = it,
                                        modifier = TODO(),
                                        onLike = TODO(),
                                        onDislike = TODO(),
                                        onShare = TODO(),
                                        onInfo = TODO(),
                                        onOptions = TODO(),
                                        onUsernameClick = TODO(),
                                        onImageClick = TODO(),
                                        onHashtagClick = TODO(),
                                        isCurrentlyPlaying = TODO(),
                                        audioIsPlaying = TODO(),
                                        progress = TODO(),
                                        duration = TODO(),
                                        onPlay = TODO(),
                                        onPause = TODO(),
                                        onSeek = TODO(),
                                    )
                                }
                            }

                            post?.participants?.let {
                                if(it.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(R.string.post_participants),
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    item {
                                        PostParticipants(it, onOpenProfile = {}, onFollow = {})
                                    }
                                }
                            }

                            if(comments.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.comments),
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                items(count = comments.size) { index ->
                                    val comment = comments[index]
                                    CommentComponent(
                                        comment = comment,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                if(loadingComments) {
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                if(noMoreContent != null) {
                                    item {
                                        if(noMoreContent == true) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                Text(stringResource(R.string.there_is_no_more_content_to_show))
                                            }
                                        } else {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                Button(
                                                    enabled = !loadingComments,
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
                }
            }
        }
    }
}