package com.isolaatti.posting.posts.domain.entity

import android.os.Parcel
import android.os.Parcelable
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.common.Ownable
import com.isolaatti.common.hashtagRegex
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.FeedDto

data class Post(
    val id: Long,
    var textContent: String,
    override val userId: Int,
    val privacy: Int,
    val date: String,
    var audioId: String?,
    val squadId: String?,
    var numberOfLikes: Int,
    var numberOfComments: Int,
    val userName: String,
    val squadName: String?,
    var liked: Boolean,
    val audio: Audio? = null,
    val images: List<RemoteImage>
) : Ownable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readSerializable() as?  Audio,
        parcel.readParcelableArray(RemoteImage::class.java.classLoader)?.toList() as? List<RemoteImage> ?: emptyList()
    ) {
    }

    companion object {
        fun fromFeedDto(feedDto: FeedDto): MutableList<Post> {
            return feedDto.data.map {
                Post(
                    id = it.post.id,
                    userId = it.post.userId,
                    textContent = it.post.textContent,
                    privacy = it.post.privacy,
                    date = it.post.date,
                    audioId = it.post.audioId,
                    squadId = it.post.squadId,
                    numberOfComments = it.numberOfComments,
                    numberOfLikes = it.numberOfLikes,
                    userName = it.userName,
                    squadName = it.squadName,
                    liked = it.liked,
                    audio = it.audio?.let { audioDto -> Audio.fromDto(audioDto) },
                    images = it.post.images.map { imageDto -> RemoteImage(imageDto.id) }
                )
            }.toMutableList()
        }

        fun fromPostDto(postDto: FeedDto.PostDto): Post {
            return Post(
                id = postDto.post.id,
                userId = postDto.post.userId,
                textContent = postDto.post.textContent,
                privacy = postDto.post.privacy,
                date = postDto.post.date,
                audioId = postDto.post.audioId,
                squadId = postDto.post.squadId,
                numberOfComments = postDto.numberOfComments,
                numberOfLikes = postDto.numberOfLikes,
                userName = postDto.userName,
                squadName = postDto.squadName,
                liked = postDto.liked,
                images = postDto.post.images.map { imageDto -> RemoteImage(imageDto.id) }
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
        parcel.writeInt(privacy)
        parcel.writeString(date)
        parcel.writeString(audioId)
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