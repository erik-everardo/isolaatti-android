package com.isolaatti.profile.domain.use_case

import android.net.Uri
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.profile.domain.ProfileRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetProfileAudio @Inject constructor(private val profileRepository: ProfileRepository) {

    operator fun invoke(audioUri: Uri, duration: Long): Flow<Resource<Audio>> = profileRepository.setProfileAudio(audioUri, duration)
}