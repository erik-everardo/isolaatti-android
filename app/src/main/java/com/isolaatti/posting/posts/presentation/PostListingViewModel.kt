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
import kotlinx.coroutines.flow.update
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
                _postListingScreenState.update {
                    it.copy(posts = emptyList())
                }
            }
            if(squadId != null) {
                squadRepository.getPosts(squadId, getLastId()).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _postListingScreenState.update {
                                it.copy(
                                    loading = false,
                                    isRefreshing = false,
                                    posts = it.posts + (resource.data ?: emptyList()),
                                    noMoreContent = (resource.data?.size == 0)

                                )
                            }
                        }

                        is Resource.Loading -> {
                            if(!refresh) {
                                _postListingScreenState.update {
                                    it.copy(loading = true)
                                }
                            }
                            else {
                                _postListingScreenState.update {
                                    it.copy(isRefreshing = true)
                                }
                            }


                        }

                        is Resource.Error -> {
                            _postListingScreenState.update {
                                it.copy(error = resource.errorType)
                            }
                        }

                    }
                    isLoadingFromScrolling = false

                }
            } else {
                postsRepository.getFeed(getLastId(), hashtag).onEach { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _postListingScreenState.update {
                                it.copy(
                                    loading = false,
                                    isRefreshing = false,
                                    posts = it.posts + (resource.data ?: emptyList()),
                                    noMoreContent = (resource.data?.size == 0),
                                    error = null
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _postListingScreenState.update {
                                if(!refresh) {
                                    it.copy(loading = true, error = null)
                                }
                                else {
                                    it.copy(isRefreshing = true, error = null)
                                }
                            }
                        }

                        is Resource.Error -> {
                            _postListingScreenState.update {
                                it.copy(error = resource.errorType)
                            }
                        }

                    }
                    isLoadingFromScrolling = false
                }.flowOn(Dispatchers.IO).launchIn(this)
            }

        }
    }

    init {
        getFeed(true)
    }
}