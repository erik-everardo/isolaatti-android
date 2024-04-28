package com.isolaatti.profile.profile_listing.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.common.ListUpdateEvent
import com.isolaatti.common.UpdateEvent
import com.isolaatti.followers.domain.FollowersRepository
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.search.domain.SearchRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileListingViewModel @Inject constructor(
    private val followersRepository: FollowersRepository,
    private val postsRepository: PostsRepository,
    private val searchRepository: SearchRepository
): ViewModel() {
    // one of these values must be set from fragment, depending on what action will be performed
    var userId: Int = 0 // for followers and followings
    var postId: Long = 0 // for "liked by"

    private var usersList: List<ProfileListItem> = listOf()


    private val _users: MutableLiveData<Pair<List<ProfileListItem>, UpdateEvent>> = MutableLiveData()

    val users: LiveData<Pair<List<ProfileListItem>, UpdateEvent>> get() = _users

    private val toRetry: MutableList<Runnable> = mutableListOf()


    // runs the lists of "Runnable" one by one and clears list. After this is executed,
    // caller should report as handled
    fun retry() {
        toRetry.forEach {
            it.run()
        }

        toRetry.clear()
    }


    private fun getUsersLastId(): Int {
        if(usersList.isEmpty()) {
            return 0
        }

        return usersList.last().id
    }

    /**
     * fetchFollowers for user previously set. userId must be set beforehand
     */
    fun fetchFollowers(refresh: Boolean = false) {
        if(userId <= 0) {
            throw IllegalStateException("userId must not be 0. Be sure to set userId on view model before calling fetchFollowers method")
        }

        if(refresh) {
            usersList = mutableListOf()
        }

        val updateListEvent = if(refresh) ListUpdateEvent.Refresh else ListUpdateEvent.ItemsAdded

        viewModelScope.launch {
            followersRepository.getFollowersOfUser(userId, getUsersLastId()).onEach {
                when(it) {
                    is Resource.Success -> {
                        if(it.data != null) {
                            val prevCount = usersList.count()
                            usersList += it.data
                            _users.postValue(Pair(usersList, UpdateEvent(updateListEvent, arrayOf(prevCount))))
                        }
                    }
                    is Resource.Error -> {
                        toRetry.add {
                            fetchFollowers()
                        }
                    }
                    is Resource.Loading -> {}
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    /**
     * fetchFollowings for user previously set. userId must be set beforehand
     */
    fun fetchFollowings(refresh: Boolean = false) {
        if(userId <= 0) {
            throw IllegalStateException("userId must not be 0. Be sure to set userId on view model before calling fetchFollowings method")
        }

        if(refresh) {
            usersList = mutableListOf()
        }

        val updateListEvent = if(refresh) ListUpdateEvent.Refresh else ListUpdateEvent.ItemsAdded

        viewModelScope.launch {
            followersRepository.getFollowingsOfUser(userId, getUsersLastId()).onEach {
                when(it) {
                    is Resource.Error ->  {
                        toRetry.add {
                            fetchFollowings()
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if(it.data != null) {
                            val prevCount = usersList.count()
                            usersList += it.data
                            _users.postValue(Pair(usersList, UpdateEvent(updateListEvent, arrayOf(prevCount))))
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    /**
     * fetchPostLikedBy for post previously set. postId must be set beforehand
     */
    fun fetchPostLikedBy(refresh: Boolean = false) {
        if(postId == 0L) {
            throw IllegalStateException("postId must not be 0. Be sure to set postId on view model before calling fetchPostLikedBy method")
        }

        if(refresh) {
            usersList = mutableListOf()
        }

        val updateListEvent = if(refresh) ListUpdateEvent.Refresh else ListUpdateEvent.ItemsAdded

        viewModelScope.launch {
            postsRepository.getUsersLikedPost(postId).onEach { resource ->
                when(resource) {
                    is Resource.Error ->  {
                        toRetry.add {
                            fetchPostLikedBy(refresh)
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if(resource.data != null) {
                            val prevCount = usersList.count()
                            usersList += resource.data
                            _users.postValue(Pair(usersList, UpdateEvent(updateListEvent, arrayOf(prevCount))))
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun fetchProfiles(refresh: Boolean = false) {
        if(refresh) {
            usersList = mutableListOf()
        }
        val updateListEvent = if(refresh) ListUpdateEvent.Refresh else ListUpdateEvent.ItemsAdded

        viewModelScope.launch {
            searchRepository.getNewestUsers(getUsersLastId()).onEach { resource ->
                when(resource) {
                    is Resource.Error ->  {
                        toRetry.add {
                            fetchProfiles(refresh)
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if(resource.data != null) {
                            val prevCount = usersList.count()
                            usersList += resource.data
                            _users.postValue(Pair(usersList, UpdateEvent(updateListEvent, arrayOf(prevCount))))
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }

    }

    private fun replaceOnLists(user: ProfileListItem) {
        val userIndex = usersList.indexOf(user)


        if(userIndex >= 0) {
            usersList = usersList.toMutableList().apply {
                set(userIndex, user)
            }

            _users.postValue(Pair(usersList, UpdateEvent(ListUpdateEvent.ItemUpdated, arrayOf(userIndex))))
        }
    }

    fun followUser(user: ProfileListItem) {
        user.updatingFollowState = true

        replaceOnLists(user)

        viewModelScope.launch {
            followersRepository.followUser(user.id).onEach {
                when(it) {
                    is Resource.Error -> {
                        toRetry.add {
                            followUser(user)
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        user.following = true
                        user.updatingFollowState = false
                        replaceOnLists(user)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }


    fun unfollowUser(user: ProfileListItem) {
        user.updatingFollowState = true

        replaceOnLists(user)

        viewModelScope.launch {
            followersRepository.unfollowUser(user.id).onEach {
                when(it) {
                    is Resource.Error -> {
                        toRetry.add {
                            unfollowUser(user)
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        user.following = false
                        user.updatingFollowState = false
                        replaceOnLists(user)
                    }
                }

            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}