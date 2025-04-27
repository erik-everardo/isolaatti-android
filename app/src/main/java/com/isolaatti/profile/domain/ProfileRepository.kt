package com.isolaatti.profile.domain

import android.net.Uri
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.images.common.data.remote.ImageDto
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(userId: Int): Flow<Resource<UserProfile>>

    fun setProfileImage(image: LocalImage): Flow<Resource<RemoteImage>>

    fun updateProfile(newDisplayName: String, newDescription: String): Flow<Resource<Boolean>>

    fun removeProfileImage(): Flow<Resource<Boolean>>

    fun setProfileAudio(audioFile: Uri, duration: Long): Flow<Resource<Audio>>

    fun removeProfileAudio(): Flow<Resource<Boolean>>
}