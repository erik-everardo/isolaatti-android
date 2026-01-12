package com.isolaatti.posting.posts.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.common.components.AudioRecorderState
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.player.AudioPlaybackState
import com.isolaatti.posting.likes.domain.repository.LikesRepository
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.domain.use_case.DeletePost
import com.isolaatti.posting.posts.ui.PostListingScreenState
import com.isolaatti.profile.domain.use_case.GetProfilePosts
import com.isolaatti.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class PostListingViewModelBase : ViewModel() {

    companion object {
        const val TAG = "PostListingViewModelBase"
    }

    @Inject
    lateinit var likesRepository: LikesRepository
    @Inject
    lateinit var getProfilePosts: GetProfilePosts
    @Inject
    lateinit var deletePostUseCase: DeletePost

    protected val _postListingScreenState = MutableStateFlow(PostListingScreenState())
    val postListingScreenState = _postListingScreenState.asStateFlow()
    var isLoadingFromScrolling = false

    val mediaControllerIsAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val audioState = AudioPlaybackState()

    fun getLastId(): Long = try { _postListingScreenState.value.posts.lastOrNull()?.id ?: -1 } catch (e: NoSuchElementException) { -1 }

    val audioRecorderState = AudioRecorderState()
    var uploadingAudio: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var ended: Boolean = false

    abstract fun getFeed(refresh: Boolean)

    fun likePost(postId: Long) {
        viewModelScope.launch {
            likesRepository.likePost(postId).onEach {like ->

                when(like) {
                    is Resource.Error -> {
                        Log.e(TAG, "Error likePost($postId) ${like.errorType} ${like.message}")
                    }
                    is Resource.Loading -> {
                        Log.i(TAG, "Loading likePost($postId)")
                    }
                    is Resource.Success -> {
                        _postListingScreenState.update {
                            it.copy(
                                posts = it.posts.toMutableList().apply {
                                    val index = indexOfFirst { post -> post.id == like.data?.postId }
                                    if(index != -1) {
                                        val post = get(index)
                                        set(index, post.copy(liked = true, numberOfLikes = like.data!!.likesCount))
                                    }

                                }
                            )
                        }
                    }
                }


            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun unLikePost(postId: Long) {
        viewModelScope.launch {
            likesRepository.unLikePost(postId).onEach {like ->

                when(like) {
                    is Resource.Error -> {
                        Log.e(TAG, "Error unLikePost($postId) ${like.errorType} ${like.message}")
                    }
                    is Resource.Loading -> {
                        Log.i(TAG, "Loading unLikePost($postId)")
                    }
                    is Resource.Success -> {
                        _postListingScreenState.update {
                            it.copy(
                                posts = it.posts.toMutableList().apply {
                                    val index = indexOfFirst { post -> post.id == like.data?.postId }
                                    if(index != -1) {
                                        val post = get(index)
                                        set(index, post.copy(liked = true, numberOfLikes = like.data!!.likesCount))
                                    }

                                }
                            )
                        }
                    }
                }


            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            deletePostUseCase(postId).onEach { res ->
                when(res) {
                    is Resource.Success -> {
                        val postDeleted = _postListingScreenState.value.posts.find { post -> post.id == postId }
                            ?: return@onEach

                        _postListingScreenState.update {
                            it.copy(
                                posts = it.posts.toMutableList().apply { remove(postDeleted) }
                            )
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                }

            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun onPostUpdate(post: Post) {
        viewModelScope.launch {
            _postListingScreenState.update {
                it.copy(
                    posts = it.posts.toMutableList().apply {
                        val index = indexOfFirst { p -> p.id == post.id }
                        if(index != -1) {
                            set(index, post)
                        }
                    }
                )
            }
        }
    }

    fun onPostAddedAtTheBeginning(post: Post) {
        _postListingScreenState.update {
            it.copy(
                posts = listOf(post) + it.posts
            )
        }
    }
}