package com.isolaatti.posting.posts.presentation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.EditPostDto
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.use_case.EditPost
import com.isolaatti.posting.posts.domain.use_case.LoadSinglePost
import com.isolaatti.posting.posts.domain.use_case.MakePost
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val makePost: MakePost,
    private val editPost: EditPost,
    private val loadPost: LoadSinglePost
) : ViewModel() {

    companion object {
        const val LOG_TAG = "CreatePostViewModel"
    }

    val validation: MutableLiveData<Boolean> = MutableLiveData(false)
    val error: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    val sendingPost: MutableLiveData<Boolean?> = MutableLiveData(null)
    val postToEdit: MutableLiveData<EditPostDto> = MutableLiveData()
    val liveContent: MutableLiveData<String> = MutableLiveData()
    private val _photos: MutableStateFlow<List<Image>> = MutableStateFlow(emptyList())
    val photos: StateFlow<List<Image>> get() = _photos
    var audioToUpload: Uri? = null

    private val _postingStep = MutableStateFlow(PostingSteps.Unspecified)
    val postingStep: StateFlow<PostingSteps> get() = _postingStep

    val audioAttachment: MutableLiveData<Playable?> = MutableLiveData()

    private var audioDraft: Long? = null
    private var audioId: String? = null

    // region Player state
    val isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var position: MutableStateFlow<Long> = MutableStateFlow(0L)
    var duration: MutableStateFlow<Long> = MutableStateFlow(0L)
    var isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var ended: Boolean = false
    // endregion

    // region Recorder state
    var isRecording: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var recordingIsPaused: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var recodingIsStopped: MutableStateFlow<Boolean> = MutableStateFlow(false)
    // endregion


    /**
     * postDiscussion() and editDiscussion() will check for audios pending to upload (drafts). It will
     * upload it (if any) and then send the request to post.
     */

    private fun sendDiscussion() {
        Log.d(LOG_TAG, "postDiscussion#send()")
        viewModelScope.launch {
            makePost(
                privacy = EditPostDto.PRIVACY_ISOLAATTI,
                content = liveContent.value ?: "",
                images = photos.value.filterIsInstance<LocalImage>(),
                audioId = audioId,
                squadId = null
            ).onEach {
                when(it) {
                    is Resource.Success -> {
                        if(it.data == PostingSteps.Finished) {
                            sendingPost.postValue(false)
                        }

                        _postingStep.value = it.data!!

                    }
                    is Resource.Error -> {
                        sendingPost.postValue(false)
                        error.postValue(it.errorType)
                    }
                    is Resource.Loading -> {
                        sendingPost.postValue(true)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
    fun postDiscussion() {

        viewModelScope.launch {

            if(audioDraft != null) {

            } else {
                sendDiscussion()
            }
        }
    }

    private fun sendEditDiscussion(postId: Long) {
        viewModelScope.launch {
            editPost(postId, EditPostDto.PRIVACY_ISOLAATTI, liveContent.value ?: "", audioId, null).onEach {
                when(it) {
                    is Resource.Success -> {
                        sendingPost.postValue(false)
                    }
                    is Resource.Error -> {
                        sendingPost.postValue(null)
                        error.postValue(it.errorType)
                    }
                    is Resource.Loading -> {
                        sendingPost.postValue(true)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun editDiscussion(postId: Long) {

        viewModelScope.launch {
            if(audioDraft != null) {

            } else {
                sendEditDiscussion(postId)
            }
        }
    }

    fun loadDiscussion(postId: Long) {
        viewModelScope.launch {
            loadPost(postId).onEach { postRes ->
                if(postRes is Resource.Success) {
                    postRes.data?.let {
                        //postToEdit.postValue(EditPostDto(PRIVACY_ISOLAATTI, content = it.textContent, postId = it.id))
                        //it.audio?.let { audio -> audioAttachment.postValue(audio) }

                        _photos.value = it.images

                        liveContent.postValue(it.textContent)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }


    // call this when user selects an existing audio (not a draft)
    fun putAudio(audio: Audio) {
        audioAttachment.value = audio
    }

    // clear user input
    fun removeAudio() {
        audioAttachment.value = null
    }

    fun addPicture(uri: LocalImage) {
        _photos.value = listOf(uri) + _photos.value
    }

    fun removePicture(index: Int) {
        val image = _photos.value.getOrNull(index)
        if(image == null) {
            return
        }

        when(image) {
            is LocalImage -> {
                _photos.value = _photos.value.toMutableList().apply {
                    removeAt(index)
                }
            }
            is RemoteImage -> {
                image.delete = true
            }
        }

    }
}