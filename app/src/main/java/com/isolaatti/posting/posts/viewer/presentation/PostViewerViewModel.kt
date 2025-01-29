package com.isolaatti.posting.posts.viewer.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.comments.domain.use_case.GetComments
import com.isolaatti.posting.likes.domain.repository.LikesRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.domain.use_case.LoadSinglePost
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewerViewModel @Inject constructor(private val loadSinglePost: LoadSinglePost, private val likesRepository: LikesRepository) : ViewModel() {
    val error: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    val post: MutableLiveData<Post> = MutableLiveData()
    var postId: Long = 0

    private val toRetry: MutableList<Runnable> = mutableListOf()

    val postLiked: MutableLiveData<Boolean> = MutableLiveData()


    // runs the lists of "Runnable" one by one and clears list. After this is executed,
    // caller should report as handled
    fun retry() {
        toRetry.forEach {
            it.run()
        }

        toRetry.clear()
    }

    fun getPost() {
        viewModelScope.launch {
            loadSinglePost(postId).onEach {
                when(it) {
                    is Resource.Error -> {
                        error.postValue(it.errorType)
                        toRetry.add {
                            getPost()
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if(it.data != null) {
                            post.postValue(it.data!!)
                            postLiked.postValue(it.data.liked)
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    private fun updateLikesCount(likesCount: Int) {
//        val updatedPost = post.value?.copy(numberOfLikes = likesCount)
//        if(updatedPost != null) {
//            post.postValue(updatedPost!!)
//        }
    }

    fun likeDislikePost() {
        viewModelScope.launch {
            if(postLiked.value == true) {
                likesRepository.unLikePost(postId).onEach {
                    when(it) {
                        is Resource.Error -> TODO()
                        is Resource.Loading -> TODO()
                        is Resource.Success -> {
                            updateLikesCount(it.data?.likesCount ?: 0)
                            postLiked.postValue(false)
                        }
                    }

                }.flowOn(Dispatchers.IO).launchIn(this)
            } else {
                likesRepository.likePost(postId).onEach {
                    when(it) {
                        is Resource.Error -> TODO()
                        is Resource.Loading -> TODO()
                        is Resource.Success -> {
                            updateLikesCount(it.data?.likesCount ?: 0)
                            postLiked.postValue(true)
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }
        }
    }
}