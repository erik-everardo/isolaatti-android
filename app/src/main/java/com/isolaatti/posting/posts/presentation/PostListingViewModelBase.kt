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
import com.isolaatti.profile.domain.use_case.GetProfilePosts
import com.isolaatti.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

    val posts: MutableStateFlow<List<Post>> = MutableStateFlow(emptyList())

    val loadingPosts = MutableStateFlow(false)

    val refreshing = MutableStateFlow(false)

    val noMoreContent = MutableStateFlow(true)

    val errorLoading: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    var isLoadingFromScrolling = false

    val mediaControllerIsAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val audioState = AudioPlaybackState()

    fun getLastId(): Long = try { posts.value?.last()?.id ?: 0 } catch (e: NoSuchElementException) { 0 }

    val audioRecorderState = AudioRecorderState()
    var uploadingAudio: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var ended: Boolean = false

    abstract fun getFeed(refresh: Boolean, hashtag: String?)

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
                        val likedPost = posts.value?.find { post -> post.id == like.data?.postId }
                        likedPost?.liked = true
                        likedPost?.numberOfLikes = like.data!!.likesCount
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
                        val likedPost = posts.value?.find { post -> post.id == like.data?.postId }
                        likedPost?.liked = false
                        likedPost?.numberOfLikes = like.data!!.likesCount
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
                        val postDeleted = posts.value?.find { post -> post.id == postId }
                            ?: return@onEach
                        posts.value = posts.value.toMutableList().apply { remove(postDeleted) }
                    }
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                }

            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun onPostUpdate(post: Post) {
        viewModelScope.launch {
            posts.update {
                it.toMutableList().apply {
                    val index = indexOfFirst { it.id == post.id }
                    if(index != -1) {
                        set(index, post)
                    }
                }
            }
        }
    }

    fun onPostAddedAtTheBeginning(post: Post) {
        posts.value = listOf(post) + posts.value
    }
}