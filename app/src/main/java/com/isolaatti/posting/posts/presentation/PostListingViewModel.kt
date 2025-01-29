package com.isolaatti.posting.posts.presentation

import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListingViewModel @Inject constructor(private val postsRepository: PostsRepository) : PostListingViewModelBase() {
    override fun getFeed(refresh: Boolean, hashtag: String?) {
        viewModelScope.launch {
            if (refresh) {
                posts.value = emptyList()
            }
            postsRepository.getFeed(getLastId(), hashtag).onEach { listResource ->
                when (listResource) {
                    is Resource.Success -> {
                        loadingPosts.postValue(false)
                        posts.value += listResource.data ?: emptyList()

                        noMoreContent.postValue(listResource.data?.size == 0)
                    }

                    is Resource.Loading -> {
                        if(!refresh)
                            loadingPosts.postValue(true)
                    }

                    is Resource.Error -> {
                        errorLoading.postValue(listResource.errorType)
                    }

                }
                isLoadingFromScrolling = false
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}