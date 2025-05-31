package com.isolaatti.posting.posts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.profile.domain.use_case.FollowUser
import com.isolaatti.utils.UrlGen

@Composable
fun PostParticipants(participants: List<ProfileListItem>, onOpenProfile: (ProfileListItem) -> Unit, onFollow: (ProfileListItem) -> Unit) {
    LazyRow {
        items(count = participants.size, key = { participants[it].id }) {
            val profile = participants[it]
            OutlinedCard(
                modifier = Modifier.padding(8.dp).width(120.dp),
                onClick = {
                    onOpenProfile(profile)
                }
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = UrlGen.userProfileImage(profile.id, true),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 4.dp)
                            .size(60.dp)
                            .clip(RoundedCornerShape(50))

                    )

                    Text(profile.name, modifier = Modifier.padding(4.dp), maxLines = 1)

                    if(profile.following == true) {
                        OutlinedButton(
                            onClick = {
                                onFollow(profile)
                            },
                        ) {
                            Text(stringResource(R.string.unfollow))
                        }
                    } else {
                        FilledTonalButton(
                            onClick = {
                                onFollow(profile)
                            },
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text(stringResource(R.string.follow), fontSize = 11.sp)
                        }
                    }
                }



            }
        }
    }
}

@Composable
@Preview
fun PostParticipantsPreview() {
    PostParticipants(
        participants = listOf(
            ProfileListItem(
                id = 1,
                name = "Erik",
                following = true
            ),
            ProfileListItem(
                id = 2,
                name = "Everardo",
                following = true
            )
        ),
        onOpenProfile = { },
        onFollow = { }
    )
}