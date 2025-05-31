package com.isolaatti.posting.posts.viewer.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.comments.domain.CommentsRepository
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.comments.domain.use_case.GetComments
import com.isolaatti.posting.likes.domain.repository.LikesRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.domain.use_case.LoadSinglePost
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewerViewModel @Inject constructor(
    private val loadSinglePost: LoadSinglePost,
    private val likesRepository: LikesRepository,
    private val commentsRepository: CommentsRepository
) : ViewModel() {
    val error: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    val post: StateFlow<Post?> get() = _post
    private val _post: MutableStateFlow<Post?> = MutableStateFlow(null)
    var postId: Long = 0

    private val _comments: MutableStateFlow<List<Comment>> = MutableStateFlow(emptyList())
    val comments: StateFlow<List<Comment>> get() = _comments

    private val toRetry: MutableList<Runnable> = mutableListOf()

    private val _noMoreComments: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val noMoreComments: StateFlow<Boolean?> get() = _noMoreComments

    val commentText: MutableStateFlow<String> = MutableStateFlow("")
    private val _loadingComments: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingComments: StateFlow<Boolean> get() = _loadingComments

    private val _postLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val postLoading: StateFlow<Boolean> get() = _postLoading

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
                        _postLoading.value = false
                        error.postValue(it.errorType)
                        toRetry.add {
                            getPost()
                        }
                    }
                    is Resource.Loading -> {
                        _postLoading.value = true
                    }
                    is Resource.Success -> {
                        _postLoading.value = false
                        if(it.data != null) {
                            _post.value = it.data
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun getComments(refresh: Boolean) {
        viewModelScope.launch {
            val lastId = _comments.value.takeIf { it.isNotEmpty() && !refresh }?.last()?.id
            commentsRepository.getComments(postId, lastId)
                .onEach { resource ->
                    when(resource) {
                        is Resource.Error -> {
                            _loadingComments.value = false
                        }
                        is Resource.Loading -> {
                            _loadingComments.value = true
                        }
                        is Resource.Success -> {
                            _loadingComments.value = false
                            if(refresh) {
                                _comments.value = resource.data ?: emptyList()
                            } else {
                                _comments.value = (_comments.value) + (resource.data ?: emptyList())
                            }

                            _noMoreComments.value = _comments.value.size >= (_post.value?.numberOfComments ?: 0)

                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .launchIn(this)
        }
    }

    fun likeDislikePost() {
        viewModelScope.launch {
            if(_post.value?.liked == true) {
                likesRepository.unLikePost(postId).onEach {
                    when(it) {
                        is Resource.Error -> {

                        }
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            _post.value?.liked = false
                            it.data?.likesCount?.let { _post.value?.numberOfLikes = it }
                        }
                    }

                }.flowOn(Dispatchers.IO).launchIn(this)
            } else {
                likesRepository.likePost(postId).onEach {
                    when(it) {
                        is Resource.Error -> {

                        }
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            _post.value?.liked = true
                            it.data?.likesCount?.let { _post.value?.numberOfLikes = it }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }
        }
    }

    fun refresh() {
        getPost()
        getComments(true)
    }
}