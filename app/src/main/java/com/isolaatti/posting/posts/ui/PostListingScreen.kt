package com.isolaatti.posting.posts.ui

import android.content.Intent
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.utils.Resource

data class PostListingScreenState(
    val isRefreshing: Boolean = false,
    val posts: List<Post> = emptyList(),
    val noMoreContent: Boolean = false,
    val loading: Boolean = false,
    val isCurrentlyPlaying: Boolean = false,
    val currentAudioPlaying: Audio? = null,
    val progress: Long = 0,
    val error: Resource.Error.ErrorType? = null
)

sealed interface PostListingScreenEvent {
    object Refresh: PostListingScreenEvent
    data class PostClicked(val post: Post): PostListingScreenEvent
    data class PostComments(val post: Post): PostListingScreenEvent
    data class PostOptionsClicked(val post: Post): PostListingScreenEvent
    data class PostInfoClicked(val post: Post): PostListingScreenEvent
    data class PostLikeClicked(val post: Post): PostListingScreenEvent
    data class PostDislikeClicked(val post: Post): PostListingScreenEvent
    data class PostShareClicked(val post: Post): PostListingScreenEvent
    data class PostImageClicked(val images: List<RemoteImage>, val index: Int): PostListingScreenEvent
    data class PostUsernameClicked(val userId: Int): PostListingScreenEvent
    data class PostHashtagClicked(val hashtag: String): PostListingScreenEvent
    data class PostPlayClicked(val audio: Audio): PostListingScreenEvent
    data class PostPauseClicked(val audio: Audio): PostListingScreenEvent
    data class PostSeekClicked(val seconds: Float): PostListingScreenEvent

}

@Composable
fun PostListingScreen(state: PostListingScreenState, onEvent: (PostListingScreenEvent) -> Unit, modifier: Modifier = Modifier) {
    PullToRefreshBox(state.isRefreshing, onRefresh = {onEvent(PostListingScreenEvent.Refresh)}, modifier = modifier) {
        LazyColumn(flingBehavior = ScrollableDefaults.flingBehavior()) {
            items(count = state.posts.size, key = {state.posts[it].id}) {
                val post = state.posts[it]
                PostComponent(
                    modifier = Modifier.animateItem(),
                    post = post,
                    onClick = {
                        onEvent(PostListingScreenEvent.PostClicked(post))
                    },
                    onComments = {
                        onEvent(PostListingScreenEvent.PostClicked(post))
                    },
                    onOptions = {
                        onEvent(PostListingScreenEvent.PostOptionsClicked(post))
                    },
                    onInfo = {
                        onEvent(PostListingScreenEvent.PostInfoClicked(post))
                    },
                    onLike = {
                        onEvent(PostListingScreenEvent.PostLikeClicked(post))
                    },
                    onDislike = {
                        onEvent(PostListingScreenEvent.PostDislikeClicked(post))
                    },
                    onShare = {
                        onEvent(PostListingScreenEvent.PostShareClicked(post))
                    },
                    onImageClick = { images, index ->
                        onEvent(PostListingScreenEvent.PostImageClicked(images, index))
                    },
                    onUsernameClick = {
                        onEvent(PostListingScreenEvent.PostUsernameClicked(post.userId))
                    },
                    onHashtagClick = { hashtag ->
                        onEvent(PostListingScreenEvent.PostHashtagClicked(hashtag))
                    },
                    onPlay = {
                        post.audio?.let { audio -> onEvent(PostListingScreenEvent.PostPlayClicked(audio)) }
                    },
                    onPause = {
                        post.audio?.let { audio -> onEvent(PostListingScreenEvent.PostPauseClicked(audio)) }
                    },
                    onSeek = {
                        onEvent(PostListingScreenEvent.PostSeekClicked(it))
                    },
                    isCurrentlyPlaying = state.isCurrentlyPlaying && post.audio == state.currentAudioPlaying,
                    duration = 0,
                    progress = state.progress,
                )
            }

            if(state.loading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if(!state.noMoreContent && !state.isRefreshing) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onClick = {

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