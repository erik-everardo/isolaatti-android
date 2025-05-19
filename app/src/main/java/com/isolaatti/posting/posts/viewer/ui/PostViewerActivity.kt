package com.isolaatti.posting.posts.viewer.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.comments.ui.components.CommentComponent
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
                addNextIntentWithParentStack(Companion.getIntent(context, postId))

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
            viewModel.getComments()
        }

        setContent {



            val post by viewModel.post.collectAsState()
            val comments by viewModel.comments.collectAsState()

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
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.comment)) },
                            modifier = Modifier.padding(16.dp).weight(1f),
                        )
                        IconButton(
                            modifier = Modifier.padding(16.dp),
                            onClick = {}
                        ) {
                            Icon(Icons.AutoMirrored.Default.Send, stringResource(R.string.send))
                        }
                    }
                }
            ) {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = {

                    },
                    modifier = Modifier.padding(it).fillMaxSize()
                ) {
                    LazyColumn() {
                        post?.let {
                            item {
                                PostComponent(
                                    post = it
                                )
                            }
                        }

                        items(count = comments.size) { index ->
                            val comment = comments[index]
                            CommentComponent(
                                comment = comment,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}