package com.isolaatti.posting.viewer.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.audio.player.AudioPlaybackState
import com.isolaatti.followers.domain.FollowersRepository
import com.isolaatti.posting.viewer.presentation.PostEvents
import com.isolaatti.posting.comments.domain.CommentsRepository
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.likes.domain.repository.LikesRepository
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.presentation.PostListingViewModelBase.Companion.TAG
import com.isolaatti.posting.viewer.ui.PostViewerState
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewerViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
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


    val audioState = AudioPlaybackState()


    private var commentToDelete: Comment? = null

    var initialized = false


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
            postsRepository.loadPost(postId).onEach { resrouce ->
                when (resrouce) {
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
            val lastId = _state.value.comments.takeIf { it.isNotEmpty() && !refresh }?.last{ it.parentCommentId == null }?.id
            commentsRepository.getComments(postId, lastId)
                .onEach { resource ->
                    when (resource) {
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
                            if (refresh) {
                                _state.update {
                                    it.copy(
                                        comments = resource.data ?: emptyList(),
                                        refreshingComments = false
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        comments = (it.comments) + (resource.data ?: emptyList()),
                                        loadingComments = false
                                    )
                                }
                            }

                            _state.update {
                                it.copy(
                                    noMoreComments = it.comments.size >= (it.post?.numberOfComments
                                        ?: 0)
                                )
                            }

                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .launchIn(this)
        }
    }

    fun loadReplies(comment: Comment) {
        viewModelScope.launch {
            val prevComment = _state.value.comments.findLast { it.id == comment.id || it.id == comment.parentCommentId }
            val index = _state.value.comments.indexOf(prevComment)

            commentsRepository.getReplies(comment.id, null).collect { resource ->
                when(resource) {
                    is Resource.Error -> {

                        if(index != -1) {
                            _state.update {
                                it.copy(comments = it.comments.toMutableList().apply {
                                    set(index, comment.copy(loadingReplies = false))
                                })
                            }
                        }

                    }

                    is Resource.Loading -> {
                        if(index != -1) {
                            _state.update {
                                it.copy(comments = it.comments.toMutableList().apply {
                                    set(index, comment.copy(loadingReplies = true, enableRepliesButton = false))
                                })
                            }
                        }
                    }

                    is Resource.Success -> {
                        if(index != -1) {
                            _state.update {
                                it.copy(comments = it.comments.toMutableList().apply {
                                    set(index, comment.copy(loadingReplies = false))
                                })
                            }
                        }

                        _state.update {
                            val commentsMutableList = it.comments.toMutableList()
                            commentsMutableList[index] =
                                comment.copy(loadingReplies = false, enableRepliesButton = false)
                            commentsMutableList
                                .addAll(
                                    index = index + 1,
                                    elements = resource.data!!
                                        .map { newComment ->
                                            newComment
                                                .copy(
                                                    parentCommentId = comment.id,
                                                    nestedLevel = 2,
                                                    allowReplies = false
                                                )
                                        }
                                )
                            it.copy(comments = commentsMutableList)
                        }



                    }
                }
            }
        }
    }

    fun likePost() {
        viewModelScope.launch {
            likesRepository.likePost(postId).onEach { like ->

                when (like) {
                    is Resource.Error -> {
                        Log.e(TAG, "Error likePost($postId) ${like.errorType} ${like.message}")
                    }

                    is Resource.Loading -> {
                        Log.i(TAG, "Loading likePost($postId)")
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                post = it.post?.copy(
                                    liked = true,
                                    numberOfLikes = like.data!!.likesCount
                                )
                            )
                        }
                    }
                }


            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun unLikePost() {
        viewModelScope.launch {
            likesRepository.unLikePost(postId).onEach { like ->

                when (like) {
                    is Resource.Error -> {
                        Log.e(TAG, "Error unLikePost($postId) ${like.errorType} ${like.message}")
                    }

                    is Resource.Loading -> {
                        Log.i(TAG, "Loading unLikePost($postId)")
                    }

                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                post = it.post?.copy(
                                    liked = false,
                                    numberOfLikes = like.data!!.likesCount
                                )
                            )
                        }
                    }
                }


            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun postComment() {
        viewModelScope.launch {
            commentsRepository.postComment(_state.value.commentBoxText, postId).onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                uploadingComment = false,
                                comments = (listOf(resource.data!!)) + it.comments,
                                commentBoxText = ""
                            )
                        }

                        _events.send(PostEvents.CommentPosted(_state.value.comments.indexOf(resource.data!!)))
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

    fun reply() {
        val commentToReply = _state.value.commentToReply ?: return
        viewModelScope.launch {

            commentsRepository.replyToComment(commentToReply.id, _state.value.commentBoxText)
                .collect { resource ->
                    when (resource) {
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


                        }
                    }
                }
        }
    }

    fun editComment() {
        val comment = _state.value.commentToEdit
        if (comment == null) {
            return
        }
        viewModelScope.launch {
            commentsRepository.editComment(comment.id, _state.value.commentBoxText)
                .onEach { resource ->
                    when (resource) {
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
                            val index = _state.value.comments.indexOf(comment)
                            _state.update {
                                it.copy(
                                    commentToEdit = null,
                                    commentBoxText = ""
                                )
                            }
                            _state.update {

                                it.copy(
                                    uploadingComment = false,
                                    comments = if (index == -1) {
                                        it.comments
                                    } else {
                                        it.comments.toMutableList().apply {
                                            val editedComment = resource.data
                                            if(editedComment != null) {
                                                set(index, comment.copy(textContent = editedComment.textContent, date = editedComment.date))
                                            }

                                        }
                                    }
                                )
                            }
                            index.takeIf { it != -1 }?.let { _events.send(PostEvents.CommentEdited(it)) }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun deleteComment() {
        val comment = commentToDelete
        if (comment == null) {
            return
        }
        viewModelScope.launch {
            commentsRepository.deleteComment(comment.id).onEach { resource ->
                when (resource) {
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
            if (profile.following == true) {
                followersRepository.unfollowUser(profile.id).onEach { resource ->
                    when (resource) {
                        is Resource.Error -> {

                        }

                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    post = it.post?.copy(
                                        participants = it.post.participants.toMutableList().apply {
                                            val index = indexOf(profile)
                                            if (index != -1) {
                                                set(index, profile.copy(following = false))
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            } else {
                followersRepository.followUser(profile.id).onEach { resource ->
                    when (resource) {
                        is Resource.Error -> {

                        }

                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    post = it.post?.copy(
                                        participants = it.post.participants.toMutableList().apply {
                                            val index = indexOf(profile)
                                            if (index != -1) {
                                                set(index, profile.copy(following = true))
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }
        }
    }


    fun setCommentToEdit(comment: Comment) {
        _state.update {
            it.copy(
                commentToEdit = comment,
                commentBoxText = comment.textContent
            )
        }
    }

    fun setCommentToReply(comment: Comment?) {
        cancelCommentToEdit()
        _state.update {
            it.copy(commentToReply = comment)
        }
    }


    fun cancelCommentToEdit() {
        _state.update {
            it.copy(
                commentToEdit = null,
                commentBoxText = ""
            )
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

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            postsRepository.deletePost(postId).onEach { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _events.send(PostEvents.ErrorDeletingPost)
                    }

                    is Resource.Loading -> {
                        Log.d(TAG, "Removing post...")
                    }

                    is Resource.Success -> {
                        _events.send(PostEvents.PostDeleted)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun onPostUpdate(post: Post) {
        _state.update {
            it.copy(post = post)
        }
    }

    fun onCommentBoxTextChange(text: String) {
        _state.update {
            it.copy(commentBoxText = text)
        }
    }

    fun setCommentToHighlight(position: Int?) {
        _state.update {
            it.copy(commentPositionToHighlight = position)
        }
    }

}