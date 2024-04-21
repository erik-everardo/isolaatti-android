package com.isolaatti.posting.posts.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.posts.data.remote.VersionDto
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
class PostVersionsViewModel @Inject constructor(private val postsRepository: PostsRepository) : ViewModel() {
    val versions: MutableLiveData<List<VersionDto>> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()
    var postId = 0L
    fun getPostVersions() {
        viewModelScope.launch {
            postsRepository.getPostVersions(postId).onEach { resource ->
                when(resource) {
                    is Resource.Error -> {}
                    is Resource.Loading -> loading.postValue(true)
                    is Resource.Success -> {
                        loading.postValue(false)
                        versions.postValue(resource.data!!)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}