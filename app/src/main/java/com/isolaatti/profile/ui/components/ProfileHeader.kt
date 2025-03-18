package com.isolaatti.profile.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.images.common.domain.entity.RemoteImage

@Composable
private fun ProfileHeaderLoading() {
    val infiniteTransition = rememberInfiniteTransition()
    val tonalElevation by infiniteTransition.animateValue(
        initialValue = 6.dp,
        targetValue = 30.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "tonal elevation for loading skeleton"
    )
    Surface(modifier = Modifier.fillMaxSize(), tonalElevation = tonalElevation) {}
}

@Composable
fun ProfileHeader(
    name: String,
    description: String?,
    image: RemoteImage?,
    audio: Audio?,
    followingText: String?,
    followerCount: Int,
    followingCount: Int,
    onImageClick: () -> Unit,
    showProfileAudioProgress: Boolean,
    profileAudioProgress: Float,
    onFollowersClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onFollowChange: (Boolean) -> Unit,
    onAddAudioDescription: () -> Unit,
    loading: Boolean = false,
    isOwnUser: Boolean,
    followingThisUser: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        if(loading) {
            ProfileHeaderLoading()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                    if(image != null) {
                        AsyncImage(
                            model = image.reducedImageUrl,
                            contentDescription = stringResource(R.string.profile_photo),
                            modifier = Modifier
                                .size(140.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    onImageClick()
                                }
                        )
                    } else {
                        Image(
                            painterResource(R.drawable.avatar),
                            contentDescription = stringResource(R.string.profile_photo),
                            modifier = Modifier
                                .size(140.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    onImageClick()
                                }
                        )
                    }
                }

                if(!isOwnUser) {
                    followingText?.let {
                        Text(it)
                    }
                }


                Text(name, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp))
                description?.let {
                    Text(it, modifier = Modifier.padding(top = 8.dp))
                }

                TextButton(onClick = onFollowersClick, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.go_to_followers_btn_text, followerCount, followingCount))
                }

                when {
                    isOwnUser && audio == null -> {
                        OutlinedCard(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.you_have_now_audio))
                                FilledTonalButton(
                                    onClick = {

                                    }
                                ) {
                                    Text(stringResource(R.string.set_audio_description))
                                }
                            }
                        }
                    }

                    audio != null -> {

                    }
                }

                when {
                    isOwnUser -> {
                        OutlinedButton(onClick = onEditProfileClick) {
                            Icon(Icons.Default.Edit, null)
                            Text(stringResource(R.string.edit_profile))
                        }
                    }

                    followingThisUser -> {
                        OutlinedButton(onClick = { onFollowChange(false) }) {
                            Text(stringResource(R.string.unfollow))
                        }
                    }
                    else -> {
                        Button(onClick = { onFollowChange(true) }) {
                            Text(stringResource(R.string.follow))
                        }
                    }
                }
            }
        }

    }
}

@Composable
@Preview(device = Devices.PIXEL_3)
fun ProfileHeaderPreview() {
    ProfileHeader(
        name = "Erik",
        description = "Hola",
        image = null,
        audio = null,
        followingText = "Mutually following",
        followerCount = 100,
        followingCount = 100,
        isOwnUser = true,
        followingThisUser = true,
        onImageClick = {},
        profileAudioProgress = 0.6f,
        showProfileAudioProgress = true,
        onFollowersClick = {},
        onFollowChange = {},
        onEditProfileClick = {},
        onAddAudioDescription = {}
    )
}