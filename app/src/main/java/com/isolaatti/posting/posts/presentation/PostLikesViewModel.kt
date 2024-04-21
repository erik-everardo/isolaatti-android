package com.isolaatti.posting.posts.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.posting.posts.domain.use_case.GetPostLikedBy
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostLikesViewModel @Inject constructor(private val getPostLikedBy: GetPostLikedBy) : ViewModel() {
    var postId: Long = 0
    val users: MutableLiveData<List<ProfileListItem>> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()
    fun getUsers() {
        viewModelScope.launch {
            getPostLikedBy(postId).onEach { resource ->
                when(resource) {
                    is Resource.Error -> {}
                    is Resource.Loading -> loading.postValue(true)
                    is Resource.Success -> {
                        loading.postValue(false)
                        users.postValue(resource.data!!)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}