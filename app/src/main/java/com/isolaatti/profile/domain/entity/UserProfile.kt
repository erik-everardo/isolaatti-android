package com.isolaatti.profile.domain.entity

import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.common.Ownable
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.profile.data.remote.UserProfileDto
import com.isolaatti.utils.UrlGen
import java.io.Serializable

data class UserProfile(
    override val userId: Int,
    val uniqueUsername: String,
    var name: String,
    val email: String?,
    val numberOfFollowers: Int,
    val numberOfFollowing: Int,
    val numberOfLikes: Int,
    val numberOfPosts: Int,
    val isUserItself: Boolean,
    var followingThisUser: Boolean,
    val thisUserIsFollowingMe: Boolean,
    val profileImageId: String?,
    var descriptionText: String?,
    val descriptionAudio: Audio?
) : Ownable, Serializable {

    val profileAvatarPictureUrl: String get() = UrlGen.userProfileImage(userId)
    val profilePictureUrl: String get() = UrlGen.userProfileImageFullQuality(userId)

    val profileImage: RemoteImage? get() = profileImageId?.let { imageId -> RemoteImage(imageId) }
    companion object {
        fun fromDto(userProfileDto: UserProfileDto): UserProfile {
            return UserProfile(
                userId = userProfileDto.id,
                uniqueUsername = userProfileDto.uniqueUsername,
                name = userProfileDto.name,
                email = userProfileDto.email,
                numberOfFollowers = userProfileDto.numberOfFollowers,
                numberOfFollowing = userProfileDto.numberOfFollowing,
                numberOfLikes = userProfileDto.numberOfLikes,
                numberOfPosts = userProfileDto.numberOfPosts,
                isUserItself = userProfileDto.isUserItself,
                followingThisUser = userProfileDto.followingThisUser,
                thisUserIsFollowingMe = userProfileDto.thisUserIsFollowingMe,
                profileImageId = userProfileDto.profileImageId,
                descriptionText = userProfileDto.descriptionText,
                descriptionAudio = userProfileDto.descriptionAudio?.let { Audio.fromDto(it) }
            )
        }
    }
}
