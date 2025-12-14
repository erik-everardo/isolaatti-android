package com.isolaatti.posting.post_creator.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.common.components.AudioRecorderState
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.post_creator.ui.CreatePostActivity.Companion.EXTRA_INPUT
import com.isolaatti.posting.posts.domain.PostPrivacy
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.use_case.EditPost
import com.isolaatti.posting.posts.domain.use_case.LoadSinglePost
import com.isolaatti.posting.posts.domain.use_case.MakePost
import com.isolaatti.utils.Resource
import com.isolaatti.utils.ResourceWithSteps
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val makePost: MakePost,
    private val editPost: EditPost,
    private val loadPost: LoadSinglePost,
    saveStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val LOG_TAG = "CreatePostViewModel"
    }

    private val input = saveStateHandle.get<CreatePostContract.Input>(EXTRA_INPUT)

    private val _state = MutableStateFlow(CreatePostScreenState(squadName = input?.squadName))
    val state = _state.asStateFlow()

    val error: MutableLiveData<ResourceWithSteps.Error.ErrorType?> = MutableLiveData()
    val sendingPost: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val liveContent: MutableLiveData<String> = MutableLiveData()
    private val _photos: MutableStateFlow<List<Image>> = MutableStateFlow(emptyList())
    val photos: StateFlow<List<Image>> get() = _photos
    var audioToUpload: Uri? = null

    val audioRecorderIsVisible = MutableStateFlow(false)

    private val _postingStep = MutableStateFlow(PostingSteps.Unspecified)
    val postingStep: StateFlow<PostingSteps> get() = _postingStep

    val audioRecorderState = AudioRecorderState()
    var ended: Boolean = false

    val thereIsContent: Boolean get() {
        return true
    }



    private val _events = Channel<CreatePostEvents>()
    val events = _events.receiveAsFlow()

    // put existing audio when editing post
    val existingAudio: MutableStateFlow<Audio?> = MutableStateFlow(null)

    // this value is checked when sending edited post.
    // if new audio is going to uploaded, do not send this to backend, uploading new audio will trigger
    // old audio deletion
    private var mustRemoveAudio: Boolean = false

    private val imagesToRemove: MutableList<RemoteImage> = mutableListOf()

    val loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    /**
     * postDiscussion() and editDiscussion() will check for audios pending to upload (drafts). It will
     * upload it (if any) and then send the request to post.
     */

    private fun sendDiscussion() {
        Log.d(LOG_TAG, "postDiscussion#send()")
        viewModelScope.launch {
            makePost(
                privacy = PostPrivacy.Isolaatti,
                content = liveContent.value ?: "",
                images = photos.value.filterIsInstance<LocalImage>(),
                audio = audioToUpload,
                squadId = input?.squadId
            ).onEach {
                when(it) {
                    is ResourceWithSteps.Success -> {
                        if(it.step == PostingSteps.Finished) {
                            _events.send(CreatePostEvents.PostCreatedOrEdited(it.data!!))
                            sendingPost.value = false
                        }

                        _postingStep.value = it.step

                    }
                    is ResourceWithSteps.Error -> {
                        sendingPost.value = false
                        error.postValue(it.errorType)
                    }
                    is ResourceWithSteps.Loading -> {
                        sendingPost.value = true
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
    fun postDiscussion() {

        viewModelScope.launch {

            sendDiscussion()
        }
    }

    private fun sendEditDiscussion(postId: Long) {
        viewModelScope.launch {
            editPost(
                postId = postId,
                privacy = PostPrivacy.Isolaatti,
                images = photos.value.filterIsInstance<LocalImage>(),
                imagesToRemove = imagesToRemove,
                audio = audioToUpload,
                content = liveContent.value ?: "",
                removeOldAudio = mustRemoveAudio  && audioToUpload == null,
                squadId = null
            ).onEach {
                when(it) {
                    is ResourceWithSteps.Success -> {
                        if(it.step == PostingSteps.Finished) {
                            sendingPost.value = false
                            _events.send(CreatePostEvents.PostCreatedOrEdited(it.data!!))
                        }
                        _postingStep.value = it.step
                    }
                    is ResourceWithSteps.Error -> {
                        sendingPost.value = false
                        error.postValue(it.errorType)
                    }
                    is ResourceWithSteps.Loading -> {
                        sendingPost.value = true
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun editDiscussion(postId: Long) {

        viewModelScope.launch {
            sendEditDiscussion(postId)
        }
    }

    fun loadDiscussion(postId: Long) {
        viewModelScope.launch {
            loadPost(postId).onEach { postRes ->
                when(postRes) {
                    is Resource.Error -> {
                        loading.value = false
                    }
                    is Resource.Loading -> {
                        loading.value = true
                    }
                    is Resource.Success -> {
                        postRes.data?.let {

                            _photos.value = it.images

                            if(it.audio != null) {
                                existingAudio.value = it.audio
                            }

                            liveContent.postValue(it.textContent)
                            loading.value = false
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun markCurrentAudioForDeletion() {
        viewModelScope.launch {
            existingAudio.value = null
            mustRemoveAudio = true
        }
    }

    fun setToReplaceAudio() {
        viewModelScope.launch {
            existingAudio.value = null
            mustRemoveAudio = true
            audioRecorderIsVisible.value = true
        }
    }

    fun addPicture(uri: LocalImage) {
        _photos.value = listOf(uri) + _photos.value
    }

    fun removePicture(image: Image) {
        viewModelScope.launch {
            when(image) {
                is LocalImage -> {
                    _photos.value = _photos.value.toMutableList().apply {
                        remove(image)
                    }
                }
                is RemoteImage -> {
                    _photos.update {
                        it.toMutableList().apply {
                            remove(image)
                        }
                    }
                    imagesToRemove.add(image)
                }
            }
        }


    }
}