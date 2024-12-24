package com.isolaatti.posting.posts.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.posting.posts.data.remote.CreatePostDto
import com.isolaatti.posting.posts.data.remote.EditPostDto
import com.isolaatti.posting.posts.data.remote.EditPostDto.Companion.PRIVACY_ISOLAATTI
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.domain.use_case.EditPost
import com.isolaatti.posting.posts.domain.use_case.LoadSinglePost
import com.isolaatti.posting.posts.domain.use_case.MakePost
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    val posted: MutableLiveData<Post?> = MutableLiveData()
    val error: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    val sendingPost: MutableLiveData<Boolean> = MutableLiveData(false)
    val postToEdit: MutableLiveData<EditPostDto> = MutableLiveData()
    val liveContent: MutableLiveData<String> = MutableLiveData()
    var content: String = ""
        set(value) {field = value; liveContent.value = value} // TODO remove this and use only liveContent
    private val _photos: MutableStateFlow<List<Uri>> = MutableStateFlow(emptyList())
    val photos: StateFlow<List<Uri>> get() = _photos

    val audioAttachment: MutableLiveData<Playable?> = MutableLiveData()

    private var audioDraft: Long? = null
    private var audioId: String? = null

    /**
     * postDiscussion() and editDiscussion() will check for audios pending to upload (drafts). It will
     * upload it (if any) and then send the request to post.
     */

    private fun sendDiscussion() {
        Log.d(LOG_TAG, "postDiscussion#send()")
        viewModelScope.launch {
            makePost(EditPostDto.PRIVACY_ISOLAATTI, content, audioId, null).onEach {
                when(it) {
                    is Resource.Success -> {
                        sendingPost.postValue(false)
                        posted.postValue(Post.fromPostDto(it.data!!))
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
            editPost(postId, EditPostDto.PRIVACY_ISOLAATTI, content, audioId, null).onEach {
                when(it) {
                    is Resource.Success -> {
                        sendingPost.postValue(false)
                        posted.postValue(Post.fromPostDto(it.data!!))
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
                        postToEdit.postValue(EditPostDto(PRIVACY_ISOLAATTI, content = it.textContent, postId = it.id))
                        it.audio?.let { audio -> audioAttachment.postValue(audio) }
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

    fun addPicture(uri: Uri) {
        _photos.value = listOf(uri) + _photos.value
    }

    fun removePicture(index: Int) {
        _photos.value = _photos.value.toMutableList().apply {
            removeAt(index)
        }
    }
}