package com.isolaatti.profile.domain.use_case

import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.profile.domain.ProfileRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetProfileImage @Inject constructor(private val profileRepository: ProfileRepository) {
    operator fun invoke(image: RemoteImage): Flow<Resource<Boolean>> = profileRepository.setProfileImage(image)
}