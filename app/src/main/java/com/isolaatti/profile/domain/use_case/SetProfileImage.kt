package com.isolaatti.profile.domain.use_case

import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.profile.domain.ProfileRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetProfileImage @Inject constructor(private val profileRepository: ProfileRepository) {
    operator fun invoke(image: LocalImage): Flow<Resource<RemoteImage>> = profileRepository.setProfileImage(image)
}