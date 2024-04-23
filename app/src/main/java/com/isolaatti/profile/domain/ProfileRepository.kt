package com.isolaatti.profile.domain

import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.profile.data.remote.UpdateProfileDto
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(userId: Int): Flow<Resource<UserProfile>>

    fun setProfileImage(image: Image): Flow<Resource<Boolean>>

    fun updateProfile(newDisplayName: String, newDescription: String): Flow<Resource<Boolean>>

    fun removeProfileImage(): Flow<Resource<Boolean>>
}