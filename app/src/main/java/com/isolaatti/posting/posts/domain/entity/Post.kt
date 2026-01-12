package com.isolaatti.posting.posts.domain.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.common.Ownable
import com.isolaatti.common.hashtagRegex
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.posting.posts.domain.PostPrivacy
import com.isolaatti.profile.domain.entity.ProfileListItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Post(
    val id: Long,
    val textContent: String,
    override val userId: Int,
    val privacy: PostPrivacy,
    val date: ZonedDateTime,
    val squadId: String? = null,
    val numberOfLikes: Int,
    val numberOfComments: Int,
    val userName: String,
    val squadName: String? = null,
    val liked: Boolean,
    val audio: Audio? = null,
    val images: List<RemoteImage>,
    val participants: List<ProfileListItem> = emptyList()
) : Ownable, Parcelable {


    companion object {
        fun fromFeedDto(feedDto: FeedDto): MutableList<Post> {
            return feedDto.data.map {
                Post(
                    id = it.post.id,
                    userId = it.post.userId,
                    textContent = it.post.textContent,
                    privacy = PostPrivacy.fromInt(it.post.privacy),
                    date = it.post.date,
                    squadId = it.post.squadId,
                    numberOfComments = it.numberOfComments,
                    numberOfLikes = it.numberOfLikes,
                    userName = it.userName,
                    squadName = it.squadName,
                    liked = it.liked,
                    audio = if(it.post.hasAudio) Audio.fromDto(it) else null,
                    images = it.post.images.map { imageDto -> RemoteImage(imageDto.id) },
                    participants = emptyList()
                )
            }.toMutableList()
        }

        fun fromPostDto(postDto: FeedDto.PostDto): Post {
            return Post(
                id = postDto.post.id,
                userId = postDto.post.userId,
                textContent = postDto.post.textContent,
                privacy = PostPrivacy.fromInt(postDto.post.privacy),
                date = postDto.post.date,
                squadId = postDto.post.squadId,
                numberOfComments = postDto.numberOfComments,
                numberOfLikes = postDto.numberOfLikes,
                userName = postDto.userName,
                squadName = postDto.squadName,
                liked = postDto.liked,
                audio = if(postDto.post.hasAudio) Audio.fromDto(postDto) else null,
                images = postDto.post.images.map { imageDto -> RemoteImage(imageDto.id) },
                participants = postDto.participants?.map { ProfileListItem.fromDto(it) } ?: emptyList()
            )
        }
    }

    @IgnoredOnParcel
    val hashtagsSpans by lazy {
        hashtagRegex.findAll(textContent).map { it.range }.toList()
    }
}