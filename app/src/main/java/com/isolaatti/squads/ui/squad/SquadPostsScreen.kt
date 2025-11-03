package com.isolaatti.squads.ui.squad

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import com.isolaatti.posting.posts.components.PostComponent

@Composable
fun SquadPostsScreen() {
    PullToRefreshBox(isRefreshing = false, onRefresh = {}) {
        LazyColumn {
            items(count = 0) {
                PostComponent(
                    post = TODO(),
                    modifier = TODO(),
                    onClick = TODO(),
                    onLike = TODO(),
                    onDislike = TODO(),
                    onComments = TODO(),
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
                    onSeek = TODO()
                )
            }
        }
    }
}