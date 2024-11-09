package com.isolaatti.audio.audio_selector.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.AudiosRepository
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.drafts.domain.AudioDraft
import com.isolaatti.audio.drafts.domain.repository.AudioDraftsRepository
import com.isolaatti.auth.domain.AuthRepository
import com.isolaatti.common.SortingEnum
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioSelectorViewModel @Inject constructor(
    private val audiosRepository: AudiosRepository,
    private val audioDraftsRepository: AudioDraftsRepository
) : ViewModel() {
    val audios: MutableLiveData<List<Audio>> = MutableLiveData()
    val drafts: MutableLiveData<List<AudioDraft>> = MutableLiveData()
    val sorting: MutableLiveData<SortingEnum> = MutableLiveData()
    var squadId: String? = null
    val error: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData(null)
    val loading: MutableLiveData<Boolean> = MutableLiveData()
    fun setSorting(sort: SortingEnum) {
        sorting.value = sort
    }

    fun getAudioDrafts() {
        viewModelScope.launch {
            audioDraftsRepository.getAudioDrafts().onEach {
                drafts.postValue(it)
            }
        }
    }

    fun getAudios() {
        if(squadId == null) {
            viewModelScope.launch {
                audiosRepository.getMyAudios(audios.value?.lastOrNull()?.id).onEach { resource ->
                    when(resource) {
                        is Resource.Error -> {
                            loading.postValue(false)
                            error.postValue(resource.errorType)
                        }
                        is Resource.Loading -> {
                            loading.postValue(true)
                        }
                        is Resource.Success -> {
                            loading.postValue(false)
                            audios.postValue(resource.data ?: listOf())
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }
        }
    }
}