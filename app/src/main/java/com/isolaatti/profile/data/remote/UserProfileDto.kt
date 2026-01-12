package com.isolaatti.profile.data.remote


data class UserProfileDto(
    val id: Int,
    val uniqueUsername: String,
    val name: String,
    val lastName: String,
    val email: String?,
    val numberOfFollowers: Int,
    val numberOfFollowing: Int,
    val numberOfLikes: Int,
    val numberOfPosts: Int,
    val isUserItself: Boolean,
    val followingThisUser: Boolean,
    val thisUserIsFollowingMe: Boolean,
    val profileImageId: String?,
    val descriptionText: String?,
    val audioDurationMs: Long,
    val descriptionAudioId: String?,
    val hasAudio: Boolean,
    val genre: String?,
    val countryCode: String?,
    val birthday: String?
)
