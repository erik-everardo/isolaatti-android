package com.isolaatti.posting.posts.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.posting.posts.ui.PostListingFragment.Companion.ARG_HASHTAG
import com.isolaatti.posting.posts.ui.PostListingFragment.Companion.ARG_SQUAD_ID
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class PostListingViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val postsRepository: PostsRepository,
    private val squadRepository: SquadsRepository
) : PostListingViewModelBase() {
    private val hashtag: String? = handle.get<String>(ARG_HASHTAG)
    private val squadId: String? = handle.get<String>(ARG_SQUAD_ID)

    override fun getFeed(refresh: Boolean) {
        viewModelScope.launch {
            if (refresh) {
                posts.value = emptyList()
            }
            if(squadId != null) {
                squadRepository.getPosts(squadId, getLastId()).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            loadingPosts.value = false
                            refreshing.value = false
                            posts.value += resource.data ?: emptyList()

                            noMoreContent.value = (resource.data?.size == 0)
                        }

                        is Resource.Loading -> {
                            if(!refresh)
                                loadingPosts.value = true
                            else
                                refreshing.value = true
                        }

                        is Resource.Error -> {
                            errorLoading.postValue(resource.errorType)
                        }

                    }
                    isLoadingFromScrolling = false

                }
            } else {
                postsRepository.getFeed(getLastId(), hashtag).onEach { listResource ->
                    when (listResource) {
                        is Resource.Success -> {
                            loadingPosts.value = false
                            refreshing.value = false
                            posts.value += listResource.data ?: emptyList()

                            noMoreContent.value = (listResource.data?.size == 0)
                        }

                        is Resource.Loading -> {
                            if(!refresh)
                                loadingPosts.value = true
                            else
                                refreshing.value = true
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
}