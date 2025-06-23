package com.isolaatti.posting.posts.viewer.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.player.AudioPlaybackState
import com.isolaatti.followers.domain.FollowersRepository
import com.isolaatti.posting.PostEvents
import com.isolaatti.posting.comments.domain.CommentsRepository
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.likes.domain.repository.LikesRepository
import com.isolaatti.posting.posts.domain.use_case.LoadSinglePost
import com.isolaatti.posting.posts.presentation.PostListingViewModelBase.Companion.TAG
import com.isolaatti.posting.posts.viewer.ui.PostViewerState
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewerViewModel @Inject constructor(
    private val loadSinglePost: LoadSinglePost,
    private val likesRepository: LikesRepository,
    private val commentsRepository: CommentsRepository,
    private val followersRepository: FollowersRepository
) : ViewModel() {
    val error: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    var postId: Long = 0

    private val _state = MutableStateFlow(PostViewerState())
    val state: StateFlow<PostViewerState> = _state


    private val toRetry: MutableList<Runnable> = mutableListOf()

    private val _events: Channel<PostEvents> = Channel()
    val events = _events.receiveAsFlow()

    val commentText: MutableStateFlow<String> = MutableStateFlow("")


    val audioState = AudioPlaybackState()


    private var commentToEdit: Comment? = null
    val commentInEdit: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var commentToDelete: Comment? = null



    var ended: Boolean = false
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
            loadSinglePost(postId).onEach { resrouce ->
                when(resrouce) {
                    is Resource.Error -> {
                        _state.update {
                            it.copy(postLoading = false)
                        }
                        error.postValue(resrouce.errorType)
                        toRetry.add {
                            getPost()
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(postLoading = true)
                        }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(postLoading = false, post = resrouce.data)
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun getComments(refresh: Boolean) {
        viewModelScope.launch {
            val lastId = _state.value.comments.takeIf { it.isNotEmpty() && !refresh }?.last()?.id
            commentsRepository.getComments(postId, lastId)
                .onEach { resource ->
                    when(resource) {
                        is Resource.Error -> {
                            _state.update {
                                it.copy(loadingComments = false)
                            }
                        }
                        is Resource.Loading -> {
                            _state.update {
                                it.copy(loadingComments = !refresh, refreshingComments = refresh)
                            }
                        }
                        is Resource.Success -> {
                            if(refresh) {
                                _state.update {
                                    it.copy(comments = resource.data ?: emptyList(), refreshingComments = false)
                                }
                            } else {
                                _state.update {
                                    it.copy(comments = (it.comments) + (resource.data ?: emptyList()), loadingComments = false)
                                }
                            }

                            _state.update {
                                it.copy(noMoreComments = it.comments.size >= (it.post?.numberOfComments ?: 0))
                            }

                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .launchIn(this)
        }
    }

    fun likePost() {
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
                        _state.update {
                            it.copy(
                                post = it.post?.apply {
                                    liked = true
                                    numberOfLikes = like.data!!.likesCount
                                }
                            )
                        }
                    }
                }


            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun unLikePost() {
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
                        _state.update {
                            it.copy(
                                post = it.post?.apply {
                                    liked = false
                                    numberOfLikes = like.data!!.likesCount
                                }
                            )
                        }
                    }
                }


            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun postComment() {
        viewModelScope.launch {
            commentsRepository.postComment(commentText.value, postId).onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                uploadingComment = false,
                                comments = (listOf(resource.data!!)) + it.comments
                            )
                        }

                        _events.send(PostEvents.CommentPosted(_state.value.comments.indexOf(resource.data!!)))

                        commentText.value = ""
                    }

                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                uploadingComment = true
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(uploadingComment = false)
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun editComment() {
        val comment = commentToEdit
        if(comment == null) {
            return
        }
        viewModelScope.launch {
            commentsRepository.editComment(comment.id, commentText.value).onEach { resource ->
                when(resource) {
                    is Resource.Error -> {
                        _state.update {
                            it.copy(uploadingComment = false)
                        }
                        _events.send(PostEvents.CommentEditError)
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(uploadingComment = true)
                        }
                    }
                    is Resource.Success -> {
                        commentToEdit = null
                        commentText.value = ""
                        commentInEdit.value = false
                        _state.update {
                            val index = it.comments.indexOf(comment)
                            it.copy(
                                uploadingComment = false,
                                comments = if(index == -1) {
                                    it.comments
                                } else {
                                    it.comments.toMutableList().apply {
                                        set(index, resource.data!!)
                                    }
                                }
                            )
                        }
                        _events.send(PostEvents.CommentEdited(_state.value.comments.indexOf(resource.data!!)))
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun deleteComment() {
        val comment = commentToDelete
        if(comment == null) {
            return
        }
        viewModelScope.launch {
            commentsRepository.deleteComment(comment.id).onEach { resource ->
                when(resource) {
                    is Resource.Error -> {
                        _events.send(PostEvents.CommentRemoveError)
                        _state.update {
                            it.copy(deletingComment = false)
                        }
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "Deleting comment $comment")
                        _state.update {
                            it.copy(deletingComment = true)
                        }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                comments = it.comments.toMutableList().apply { remove(comment) },
                                showDeleteCommentDialog = false,
                                deletingComment = false
                            )
                        }
                        _events.send(PostEvents.CommentRemoved)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun toggleFollowParticipant(profile: ProfileListItem) {
        viewModelScope.launch {
            if(profile.following == true) {
                followersRepository.unfollowUser(profile.id).onEach { resource ->
                    when(resource) {
                        is Resource.Error -> {

                        }
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            _state.value.post?.also {
                                it.participants = it.participants.toMutableList().apply {
                                    val index = indexOf(profile)
                                    if(index != -1) {
                                        set(index, profile.copy(following = false))
                                    }
                                }
                            }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            } else {
                followersRepository.followUser(profile.id).onEach { resource ->
                    when(resource) {
                        is Resource.Error -> {

                        }
                        is Resource.Loading -> {

                        }
                        is Resource.Success -> {
                            _state.value.post?.also {
                                it.participants = it.participants.toMutableList().apply {
                                    val index = indexOf(profile)
                                    if(index != -1) {
                                        set(index, profile.copy(following = true))
                                    }
                                }
                            }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }
        }
    }


    fun setCommentToEdit(comment: Comment) {
        viewModelScope.launch {
            commentToEdit = comment
            commentInEdit.value = true

            commentText.value = comment.textContent
        }
    }

    fun cancelCommentToEdit() {
        viewModelScope.launch {
            commentToEdit = null
            commentInEdit.value = false
            commentText.value = ""
        }
    }

    fun setCommentToDelete(comment: Comment) {
        commentToDelete = comment
        viewModelScope.launch {
            _state.update {
                it.copy(showDeleteCommentDialog = true)
            }
        }

    }

    fun cancelCommentToDelete() {
        commentToDelete = null
        viewModelScope.launch {
            _state.update {
                it.copy(showDeleteCommentDialog = false)
            }
        }
    }

    fun refresh() {
        getPost()
    }
}