package com.isolaatti.squads.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.squads.SquadAction
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.squads.domain.Squad
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class SquadViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val squadRepository: SquadsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SquadState())
    val state = _state.asStateFlow()

    val addedPosts = Channel<Post>()


    val squadId: String? = savedStateHandle["id"]

    init {
        getSquad()
    }


    private var _lastId: Long? = null

    fun getSquad(refresh: Boolean = false) {
        if(squadId == null) {
            return
        }
        squadRepository.getSquad(squadId).onEach { resource ->
            when(resource) {
                is Resource.Success -> {
                    _state.update {
                        val squad = resource.data
                        it.copy(
                            squad = resource.data?.let { squadDto -> Squad.fromDto(squadDto) },
                            loadingSquad = false,
                            refreshing = false,
                            tabs = buildList {
                                if(squad?.description?.isNotEmpty() == true) {
                                    add(SquadTabs.Description)
                                } else if(squad?.membership == SquadDto.Membership.Owner) {
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

    fun onClickImage() {
        _state.update { it.copy(showImageDialog = true) }
    }

    fun dismissImageDialog() {
        _state.update { it.copy(showImageDialog = false) }
    }

    private fun startUploadingImage() {
        val imageToUpload = _state.value.squadPictureToUpload
        if(squadId == null || imageToUpload == null) {
            return
        }
        viewModelScope.launch {
            val prevImage = state.value.squad?.image
            squadRepository.updateImage(squadId, imageToUpload).collect { resource ->

                when(resource) {
                    is Resource.Error -> {
                        _state.update { it.copy(
                            squad = it.squad?.copy(image = prevImage),
                            uploadingSquadPicture = false
                        ) }
                    }
                    is Resource.Loading -> {
                        _state.update { it.copy(uploadingSquadPicture = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(
                            squad = it.squad?.copy(image = resource.data?.let { resource.data }),
                            uploadingSquadPicture = false,
                            showConfirmImageChange = false
                        ) }
                    }
                }

            }
        }
    }

    fun setSquad(squad: Squad) {
        _state.update {
            it.copy(
                squad = squad
            )
        }
    }

    fun onAction(action: SquadAction) {
        viewModelScope.launch {
            when(action) {
                is SquadAction.PostCreated -> {
                    addedPosts.send(action.post)
                }
                is SquadAction.ImageForSquadSelected -> {
                    _state.update {
                        it.copy(showConfirmImageChange = true, squadPictureToUpload = action.image)
                    }
                }
                SquadAction.AcceptChangePictureDialog -> {
                    startUploadingImage()
                }

                SquadAction.DismissConfirmChangePictureDialog -> {
                    _state.update { it.copy(showConfirmImageChange = false) }
                }
                SquadAction.TakeSquadPictureFromCamera, SquadAction.UploadSquadPictureFromGallery -> {
                    _state.update { it.copy(showImageDialog = false) }
                }
                is SquadAction.ExtendedDescriptionEdited -> {
                    _state.update {
                        it.copy(squad = it.squad?.copy(extendedDescription = action.description))
                    }
                }
                else -> Unit
            }
        }
    }
}
