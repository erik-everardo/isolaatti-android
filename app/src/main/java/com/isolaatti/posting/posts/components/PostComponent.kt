package com.isolaatti.posting.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.audio.common.components.AudioPlayer
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.player.AudioPlaybackState
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.domain.PostPrivacy
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.utils.UrlGen.userProfileImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComponent(
    post: Post,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onComments: () -> Unit = {},
    onShare: () -> Unit = {},
    onInfo: () -> Unit = {},
    onOptions: () -> Unit = {},
    onUsernameClick: () -> Unit = {},
    onImageClick: (images: List<RemoteImage>, index: Int) -> Unit = {_, _ -> },
    onHashtagClick: (hashtag: String) -> Unit = {},

    isCurrentlyPlaying: Boolean = false,
    audioIsPlaying: Boolean = false,
    progress: Long = 0,
    duration: Long = 0,
    onPlay: () -> Unit =  {},
    onPause: () -> Unit = {},
    onSeek: (seconds: Float) -> Unit = {},

) {
    Card(modifier = modifier.padding(8.dp).clickable { onClick() }, shape = RectangleShape) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = userProfileImage(post.userId),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        onUsernameClick()
                    },
                contentDescription = stringResource(R.string.profile_photo)
            )
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.clickable { onUsernameClick() }){
                    Text(post.userName, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(4.dp))
                }
                Text(post.date, maxLines = 1, modifier = Modifier.padding(horizontal = 4.dp))
            }
            FilledTonalIconButton(onClick = onOptions) {
                Icon(painterResource(R.drawable.baseline_more_horiz_24), null)
            }
        }

        post.audio?.let {
            AudioPlayer(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                positionMs = progress,
                isPlaying = isCurrentlyPlaying && audioIsPlaying,
                durationMs = duration,
                isLoading = false,
                focused = isCurrentlyPlaying,
                onPlay = onPlay,
                onPause = onPause,
                onSeek = onSeek
            )
        }

        if(post.textContent.isNotBlank()) {
            Text(post.textContent, modifier = Modifier.padding(8.dp))
        }

        if(post.images.isNotEmpty()) {
            Box {
                val pagerState = rememberPagerState(pageCount = {post.images.size})
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    val image = post.images[it]
                    AsyncImage(
                        image.reducedImageUrl, null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onImageClick(post.images, it)
                            }
                    )
                }
                if(post.images.size > 1) {
                    Text("${pagerState.currentPage + 1} of ${pagerState.pageCount}",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        color = Color.White
                    )
                }
            }
        }

        Row(modifier = Modifier.height(50.dp).padding(4.dp)) {
            FilledTonalIconButton(
                onClick = { if(post.liked) { onDislike() } else { onLike() } },
                modifier = Modifier.padding(4.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.hands_clapping_solid),
                        null,
                        modifier = Modifier.padding(horizontal = 8.dp).size(30.dp),
                        tint = if(post.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.numberOfLikes.toString(),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            FilledTonalIconButton(onClick = onComments, modifier = Modifier.padding(4.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.comments_solid),
                        null,
                        modifier = Modifier.padding(horizontal = 8.dp).size(30.dp)
                    )
                    Text(
                        text = post.numberOfComments.toString(),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            FilledTonalIconButton(onClick = onShare, modifier = Modifier.padding(4.dp)) {
                Icon(
                    painterResource(R.drawable.baseline_share_24),
                    null,
                )
            }
            FilledTonalIconButton(onClick = onInfo, modifier = Modifier.padding(4.dp)) {
                Icon(painterResource(R.drawable.baseline_info_24), null)
            }
        }

    }
}


@Composable
@Preview(device = Devices.PIXEL_5)
fun PostPreview() {
    PostComponent(Post(id = 0L, textContent = "Test", userId = 1, privacy = PostPrivacy.Isolaatti, date = "Date", images = emptyList(), liked = false, userName = "Username", numberOfLikes = 2, numberOfComments = 3))
}