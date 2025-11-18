package com.isolaatti.squads.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.squads.data.CreateSquadDto
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.squads.data.SquadsRepositoryImpl
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.squads.ui.EditExtendedDescriptionActivity
import com.isolaatti.squads.ui.SquadMainNavigation
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditExtendedDescriptionScreenState(
    val squad: SquadDto? = null,
    val loading: Boolean = true
)

@HiltViewModel
class EditExtendedDescriptionScreenViewModel @Inject constructor(
    private val squadsRepository: SquadsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _squad: MutableStateFlow<EditExtendedDescriptionScreenState> = MutableStateFlow(EditExtendedDescriptionScreenState())
    val squad: StateFlow<EditExtendedDescriptionScreenState> = _squad


    private val squadId: String = savedStateHandle[EditExtendedDescriptionActivity.EXTRA_SQUAD_ID] ?: ""

    init {
        viewModelScope.launch {
            squadsRepository.getSquad(squadId).collect { squad ->
                when(squad) {
                    is Resource.Success -> {
                        _squad.update {
                            it.copy(
                                squad = squad.data,
                                loading = false
                            )
                        }
                    }
                    is Resource.Error -> {
                        _squad.update {
                            it.copy(
                                loading = false,
                                squad = null
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _squad.update {
                            it.copy(
                                loading = true,
                                squad = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun onUpdateDescription(description: String) {
        
    }


    fun saveDescription() {
        viewModelScope.launch {
            squadsRepository.updateSquad(squadId, squad = CreateSquadDto(
                description = _squad.value.squad?.description ?: "",
                name = _squad.value.squad?.name ?: "",
                extendedDescription = _squad.value.squad?.extendedDescription ?: ""
            )).collect {
                when(it) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {}
                }
            }
        }
    }

}
