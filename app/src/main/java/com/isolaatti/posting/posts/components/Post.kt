package com.isolaatti.posting.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.utils.UrlGen.userProfileImage

@Composable
fun Post(
    post: Post,
    onClick: () -> Unit = {},
    onLike: () -> Unit = {},
    onComments: () -> Unit = {},
    onShare: () -> Unit = {},
    onInfo: () -> Unit = {},
    onOptions: () -> Unit = {},
    onUsernameClick: () -> Unit = {}
) {
    Card {
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
            // TODO audio player here
        }

        Text(post.textContent, modifier = Modifier.padding(8.dp))

        // TODO pager
        if(post.images.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f))
        }

        Row(modifier = Modifier.height(50.dp).padding(4.dp)) {
            FilledTonalIconButton(onClick = onLike, modifier = Modifier.padding(4.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.hands_clapping_solid), null, modifier = Modifier.size(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Clap")
                        Text(post.numberOfLikes.toString())
                    }
                }
            }
            FilledTonalIconButton(onClick = onComments, modifier = Modifier.padding(4.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.comments_solid), null, modifier = Modifier.size(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Comments")
                        Text(post.numberOfComments.toString())
                    }
                }
            }
            FilledTonalIconButton(onClick = onShare, modifier = Modifier.padding(4.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_share_24), null, modifier = Modifier.size(30.dp))
                    Text("Share")
                }
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
    Post(Post(id = 0L, textContent = "Test", userId = 1, privacy = 2, date = "Date", images = emptyList(), liked = false, userName = "Username", numberOfLikes = 1, numberOfComments = 2))
}