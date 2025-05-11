package com.isolaatti.audio.common.domain

import android.net.Uri
import androidx.core.net.toUri
import com.isolaatti.common.Ownable
import com.isolaatti.connectivity.RetrofitClient.Companion.BASE_URL
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.profile.data.remote.UserProfileDto
import com.isolaatti.utils.UrlGen
import java.io.Serializable
import java.time.ZonedDateTime

data class Audio(
    val id: String,
    override val userId: Int,
    val isProfileAudio: Boolean = false
): Ownable, Playable(), Serializable {

    override val uri: Uri get() {
        return if(isProfileAudio) {
            "${BASE_URL}users/$userId/downloadAudio".toUri()
        } else {
            "${BASE_URL}Posting/Post/${id}/DownloadAudio".toUri()
        }
    }

    override val thumbnail: String get() {
        return UrlGen.userProfileImage(userId)
    }

    companion object {
        fun fromDto(post: FeedDto.PostDto): Audio {
            return Audio(
                id = post.post.id.toString(),
                userId =  post.post.userId,
            )
        }

        fun fromDto(userProfileDto: UserProfileDto): Audio {
            return Audio(
                id = "user-${userProfileDto.id}",
                userId = userProfileDto.id,
                isProfileAudio = true
            )
        }
    }
}