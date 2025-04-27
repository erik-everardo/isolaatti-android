package com.isolaatti.profile.domain.use_case

import com.isolaatti.profile.domain.ProfileRepository
import javax.inject.Inject

class DeleteProfileAudio @Inject constructor(private val profileRepository: ProfileRepository) {
    operator fun invoke() = profileRepository.removeProfileAudio()
}