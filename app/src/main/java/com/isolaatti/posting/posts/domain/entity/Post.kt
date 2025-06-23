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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Post(
    val id: Long,
    textContent: String,
    override val userId: Int,
    val privacy: PostPrivacy,
    val date: ZonedDateTime,
    val squadId: String? = null,
    numberOfLikes: Int,
    numberOfComments: Int,
    val userName: String,
    val squadName: String? = null,
    liked: Boolean,
    val audio: Audio? = null,
    images: List<RemoteImage>,
    participants: List<ProfileListItem> = emptyList()
) : Ownable, Parcelable {

    var liked by mutableStateOf(liked)
    var images by mutableStateOf(images)
    var textContent by mutableStateOf(textContent)
    var numberOfLikes by mutableIntStateOf(numberOfLikes)
    var numberOfComments by mutableStateOf(numberOfComments)

    var participants by mutableStateOf(participants)

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        PostPrivacy.fromInt(parcel.readInt()),
        ZonedDateTime.parse(parcel.readString()!!),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readSerializable() as?  Audio,
        parcel.readParcelableArray(RemoteImage::class.java.classLoader)?.toList() as? List<RemoteImage> ?: emptyList(),
        parcel.readParcelableArray(ProfileListItem::class.java.classLoader)?.toList() as? List<ProfileListItem> ?: emptyList()
    ) {
    }

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

        @JvmField
        val CREATOR = object: Parcelable.Creator<Post> {
            override fun createFromParcel(parcel: Parcel): Post {
                return Post(parcel)
            }

            override fun newArray(size: Int): Array<Post?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(textContent)
        parcel.writeInt(userId)
        parcel.writeInt(privacy.toInt())
        parcel.writeString(date.format(DateTimeFormatter.ISO_DATE_TIME))
        parcel.writeString(squadId)
        parcel.writeInt(numberOfLikes)
        parcel.writeInt(numberOfComments)
        parcel.writeString(userName)
        parcel.writeString(squadName)
        parcel.writeByte(if (liked) 1 else 0)
        parcel.writeSerializable(audio)
        parcel.writeParcelableArray(images.toTypedArray(), 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    val hashtagsSpans by lazy {
        hashtagRegex.findAll(textContent).map { it.range }.toList()
    }
}