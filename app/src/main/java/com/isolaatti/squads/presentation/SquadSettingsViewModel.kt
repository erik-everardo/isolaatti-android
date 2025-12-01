package com.isolaatti.squads.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.squads.domain.Squad
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SquadSettingScreenState(
    val squad: Squad? = null,
    val loading: Boolean = false,
    val error: Boolean = false
)

@HiltViewModel
class SquadSettingsViewModel @Inject constructor(
    private val squadsRepository: SquadsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState: MutableStateFlow<SquadSettingScreenState> = MutableStateFlow(SquadSettingScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        val squadId: String? = savedStateHandle["id"]
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

}