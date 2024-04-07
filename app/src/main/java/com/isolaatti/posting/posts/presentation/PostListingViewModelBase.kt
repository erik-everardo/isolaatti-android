package com.isolaatti.posting.posts.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.likes.domain.repository.LikesRepository
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.domain.use_case.DeletePost
import com.isolaatti.profile.domain.use_case.GetProfilePosts
import com.isolaatti.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    val posts: MutableLiveData<Pair<MutableList<Post>?, UpdateEvent>?> = MutableLiveData()

    val postsList get() = posts.value?.first

    val loadingPosts = MutableLiveData(false)

    val noMoreContent = MutableLiveData(false)

    val errorLoading: MutableLiveData<Resource.Error.ErrorType?> = MutableLiveData()
    var isLoadingFromScrolling = false

    fun getLastId(): Long = try { posts.value?.first?.last()?.id ?: 0 } catch (e: NoSuchElementException) { 0 }


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
                        val likedPost = posts.value?.first?.find { post -> post.id == like.data?.postId }
                        val index = posts.value?.first?.indexOf(likedPost)
                        if(index != null){
                            val temp = posts.value?.first?.toMutableList()
                            Log.d("***", temp.toString())
                            temp?.set(index, likedPost!!.apply {
                                liked = true
                                numberOfLikes = like.data?.likesCount ?: 0

                            })
                        }
                        posts.postValue(posts.value?.copy(second = UpdateEvent(UpdateEvent.UpdateType.POST_LIKED, index)))
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
                        val likedPost = posts.value?.first?.find { post -> post.id == like.data?.postId }
                        val index = posts.value?.first?.indexOf(likedPost)
                        if(index != null){
                            val temp = posts.value?.first?.toMutableList()
                            temp?.set(index, likedPost!!.apply {
                                liked = false
                                numberOfLikes = like.data?.likesCount ?: 0
                            })
                        }
                        posts.postValue(posts.value?.copy(second = UpdateEvent(UpdateEvent.UpdateType.POST_LIKED, index)))
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
                        val postDeleted = posts.value?.first?.find { post -> post.id == postId }
                            ?: return@onEach
                        val index = posts.value?.first?.indexOf(postDeleted)

                        posts.value?.first?.removeAt(index!!)
                        posts.postValue(posts.value?.copy(second = UpdateEvent(UpdateEvent.UpdateType.POST_REMOVED, index)))
                    }
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                }

            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun onPostUpdate(post: Post) {

    }

    fun onPostAddedAtTheBeginning(post: Post) {

    }
}