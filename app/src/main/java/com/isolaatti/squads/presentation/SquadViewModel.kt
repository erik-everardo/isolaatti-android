package com.isolaatti.squads.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class SquadViewModel @Inject constructor(
    private val squadRepository: SquadsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SquadState())
    val state = _state.asStateFlow()

    private var _lastId: Long? = null

    var requested = false

    fun getSquad(id: String, refresh: Boolean = false) {
        squadRepository.getSquad(id).onEach { resource ->
            when(resource) {
                is Resource.Success -> {
                    _state.update {
                        val squad = resource.data
                        it.copy(
                            squad = resource.data,
                            loadingSquad = false,
                            refreshing = false,
                            tabs = buildList {
                                // TODO add this tab conditionally if user is owner
                                if(squad?.description?.isNotEmpty() == true) {
                                    add(SquadTabs.Description)
                                } else if(squad?.isOwned == true) {
                                    add(SquadTabs.Description)
                                }
                                add(SquadTabs.Posts)
                                add(SquadTabs.Members)
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    _state.value = SquadState(squad = null)
                }

                is Resource.Loading -> {
                    _state.update { it.copy(refreshing = refresh, loadingSquad = !refresh) }

                }
            }
        }.flowOn(Dispatchers.IO).launchIn(viewModelScope)
    }

    fun getPosts(squadId: String, refresh: Boolean = false) {
        if(refresh) {
            _lastId = null
        }
        viewModelScope.launch {
            squadRepository.getPosts(squadId, _lastId).collect { resource ->
                when(resource) {
                    is Resource.Error -> {
                        _state.update { it.copy(loadingPosts = false) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(loadingPosts = !refresh, refreshing = refresh) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                posts = resource.data?.data?.map { Post.fromPostDto(it) } ?: emptyList(),
                                loadingPosts = false,
                                moreContent = resource.data?.moreContent == true
                            )
                        }
                    }
                }
            }
        }
    }
}
