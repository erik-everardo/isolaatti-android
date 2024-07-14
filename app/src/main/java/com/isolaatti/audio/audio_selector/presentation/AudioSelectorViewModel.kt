package com.isolaatti.audio.audio_selector.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.AudiosRepository
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.drafts.domain.AudioDraft
import com.isolaatti.audio.drafts.domain.repository.AudioDraftsRepository
import com.isolaatti.common.SortingEnum
import dagger.hilt.android.lifecycle.HiltViewModel
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
        viewModelScope.launch {

        }
    }
}