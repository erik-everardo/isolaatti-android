package com.isolaatti.profile.domain.use_case

import com.isolaatti.profile.domain.ProfileRepository
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfile @Inject constructor(private val profileRepository: ProfileRepository) {
    operator fun invoke(userId: Int): Flow<Resource<UserProfile>> = profileRepository.getProfile(userId)

    fun getMyProfile(): Flow<Resource<UserProfile>> = profileRepository.getMyProfile()
}