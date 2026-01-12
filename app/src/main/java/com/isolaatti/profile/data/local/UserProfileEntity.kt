package com.isolaatti.profile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.isolaatti.profile.data.remote.UserProfileDto
import com.isolaatti.utils.UrlGen

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Long,
    val uniqueUsername: String,
    val name: String,
    val lastName: String,
    val numberOfFollowers: Int,
    val numberOfFollowing: Int,
    val numberOfLikes: Int,
    val numberOfPosts: Int,
    val isUserItself: Boolean,
    val followingThisUser: Boolean,
    val thisUserIsFollowingMe: Boolean,
    val profileImageId: String?,
    val profileImageUrl: String?,
    val descriptionText: String?,
    val audioDurationMs: Long,
    val hasAudio: Boolean,
    val genre: String?,
    val countryCode: String?,
    val birthday: String?
) {
    companion object {
        fun fromDto(dto: UserProfileDto): UserProfileEntity {
            return UserProfileEntity(
                id = dto.id.toLong(),
                uniqueUsername = dto.uniqueUsername,
                name = dto.name,
                lastName = dto.lastName,
                numberOfFollowers = dto.numberOfFollowers,
                numberOfFollowing = dto.numberOfFollowing,
                numberOfLikes = dto.numberOfLikes,
                numberOfPosts = dto.numberOfPosts,
                isUserItself = dto.isUserItself,
                followingThisUser = dto.followingThisUser,
                thisUserIsFollowingMe = dto.thisUserIsFollowingMe,
                profileImageId = dto.profileImageId,
                profileImageUrl = UrlGen.userProfileImage(dto.id),
                descriptionText = dto.descriptionText,
                audioDurationMs = dto.audioDurationMs,
                hasAudio = dto.hasAudio,
                genre = dto.genre,
                countryCode = dto.countryCode,
                birthday = dto.birthday
            )
        }
    }
}
