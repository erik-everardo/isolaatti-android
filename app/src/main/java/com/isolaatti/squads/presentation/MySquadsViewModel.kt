package com.isolaatti.squads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.squads.data.CreateSquadDto
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MySquadsScreenState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val squads: List<SquadDto> = emptyList(),
    val error: Boolean = false,
    val showNewSquadDialog: Boolean = false,
    val creatingSquad: Boolean = false,
    val errorCreatingSquad: Boolean = false
)

sealed class MySquadsScreenEvent {
    data object Created : MySquadsScreenEvent()
    data class Error(val error: String) : MySquadsScreenEvent()
}

@HiltViewModel
class MySquadsViewModel @Inject constructor(private val squadsRepository: SquadsRepository) : ViewModel() {

    private val _state = MutableStateFlow(MySquadsScreenState())
    val state = _state.asStateFlow()

    private val _events = Channel<MySquadsScreenEvent>()
    val events = _events.receiveAsFlow()

    fun showNewSquadDialog() {
        _state.update { it.copy(showNewSquadDialog = true) }
    }

    fun hideNewSquadDialog() {
        _state.update { it.copy(showNewSquadDialog = false) }
    }

    fun getSquads(lastId: String? = null) {
        squadsRepository.getMySquads(lastId).onEach { resource ->
            when(resource) {
                is Resource.Error -> _state.update { it.copy(error = true, loading = false) }
                is Resource.Loading -> _state.update { it.copy(loading = lastId == null, refreshing = lastId != null, error = false) }
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            squads = if (lastId == null) resource.data
                                ?: emptyList() else it.squads + (resource.data ?: emptyList())
                        )
                    }
                }
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)

    }

    fun createSquad(name: String, description: String, extendedDescription: String, privacy: Int) {
        squadsRepository.createSquad(
            CreateSquadDto(
                name,
                description,
                extendedDescription,
                privacy
            )
        )
            .onEach { resource ->
                when(resource) {
                    is Resource.Error -> _state.update { it.copy(errorCreatingSquad = true, creatingSquad = false) }
                    is Resource.Loading -> _state.update { it.copy(creatingSquad = true, errorCreatingSquad = false) }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(showNewSquadDialog = false, creatingSquad = false, squads = listOf(resource.data!!.squad) + it.squads)
                        }

                    }
                }
            }.launchIn(viewModelScope)
    }

    init {
        getSquads()
    }
}