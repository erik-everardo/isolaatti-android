package com.isolaatti.squads.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.squads.data.CreateSquadDto
import com.isolaatti.squads.domain.Squad
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SquadSettingScreenState(
    val squad: Squad? = null,
    val loading: Boolean = false,
    val updating: Boolean = false,
    val error: Boolean = false,
    val imageToUpload: LocalImage? = null,
    val uploadingPicture: Boolean = false
)

sealed interface SquadSettingsEvent {
    data class SquadUpdated(val squad: Squad) : SquadSettingsEvent
}

@HiltViewModel
class SquadSettingsViewModel @Inject constructor(
    private val squadsRepository: SquadsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState: MutableStateFlow<SquadSettingScreenState> = MutableStateFlow(SquadSettingScreenState())
    val uiState = _uiState.asStateFlow()
    val squadId: String? = savedStateHandle["id"]

    private val _events: Channel<SquadSettingsEvent> = Channel()
    val events = _events.consumeAsFlow()



    init {

        squadId?.let { getSquad(it) }
    }

    fun getSquad(squadId: String) {
        viewModelScope.launch {
            squadsRepository.getSquad(squadId).collect {
                when(it) {
                    is Resource.Success -> {
                        it.data?.let { squad ->
                            _uiState.value = SquadSettingScreenState(squad = Squad.fromDto(squad), loading = false, error = false)
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(loading = true)
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(loading = false, error = true)
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(squad = it.squad?.copy(name = name))
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update {
            it.copy(squad = it.squad?.copy(description = description))
        }
    }

    fun save() {
        if(squadId == null) {
            return
        }
        var squad = _uiState.value.squad ?: return
        viewModelScope.launch {

            val image = _uiState.value.imageToUpload
            if(image != null) {
                squadsRepository.updateImage(squadId, image).collect { resource ->
                    when(resource) {
                        is Resource.Error -> {

                        }
                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(uploadingPicture = true)
                            }
                        }
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(uploadingPicture = false)
                            }
                            squad = squad.copy(image = resource.data)
                        }

                    }
                }
            }

            squadsRepository.updateSquad(
                squadId = squadId,
                squad = CreateSquadDto(
                    name = squad.name,
                    description = squad.description,
                    extendedDescription = squad.extendedDescription
                )
            ).collect { resource ->
                when(resource) {
                    is Resource.Error -> {

                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(loading = false, error = false)
                        }
                        _events.send(SquadSettingsEvent.SquadUpdated(squad))
                    }
                }
            }
        }
    }

    fun undoImageSelected() {
        _uiState.update {
            it.copy(imageToUpload = null)
        }

    }

    fun onImageSelected(image: LocalImage) {
        _uiState.update {
            it.copy(imageToUpload = image)
        }
    }

}