package com.isolaatti.squads.ui.squad

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.squads.presentation.SquadState
import com.isolaatti.utils.UrlGen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadHome(
    state: SquadState,
    onRefresh: () -> Unit,
    onCreatePost: () -> Unit,
    onPostClick: (post: FeedDto.PostDto) -> Unit,
    onProfileClick: (post: FeedDto.PostDto) -> Unit,
    onMoreClick: (post: FeedDto.PostDto) -> Unit,
    onLike: (post: FeedDto.PostDto) -> Unit,
    onDislike: (post: FeedDto.PostDto) -> Unit,
    onImageClick: (post: FeedDto.PostDto, imageUrl: String) -> Unit,
    onShareClick: (post: FeedDto.PostDto) -> Unit
) {


    Box(Modifier.fillMaxWidth()) {
        if(state.loadingSquad) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        PullToRefreshBox(isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            val lazyListState = rememberLazyListState()

            LazyColumn(contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues(), state = lazyListState) {
                state.squad?.description?.let {
                    item {
                        Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            var showExtendedDescription by remember { mutableStateOf(false) }
                            Text(
                                text = "Welcome!",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            if(state.squad.extendedDescription.isNotBlank()) {
                                if(!showExtendedDescription) {
                                    Button(onClick = { showExtendedDescription }) {
                                        Text("Read more")
                                    }
                                } else {
                                    Text(state.squad.extendedDescription)
                                }

                            }
                        }

                    }

                    item {
                        Button(onClick = onCreatePost, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text("Create post")
                        }
                    }

                    item {
                        Text("Posts", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
                    }

                    items(state.posts.size) {
                        val post = state.posts[it]
                        PostComponent(
                            post = post,

                        )
                    }
                }

            }
        }
    }

}