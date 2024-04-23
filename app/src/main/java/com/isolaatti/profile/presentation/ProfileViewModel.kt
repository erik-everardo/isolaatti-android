package com.isolaatti.profile.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isolaatti.followers.domain.FollowingState
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.posting.posts.presentation.PostListingViewModelBase
import com.isolaatti.posting.posts.presentation.UpdateEvent
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.profile.domain.use_case.FollowUser
import com.isolaatti.profile.domain.use_case.GetProfile
import com.isolaatti.profile.domain.use_case.GetProfilePosts
import com.isolaatti.profile.domain.use_case.RemoveProfilePicture
import com.isolaatti.profile.domain.use_case.SetProfileImage
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfile,
    private val getProfilePostsUseCase: GetProfilePosts,
    private val setProfileImageUC: SetProfileImage,
    private val followUserUC: FollowUser,
    private val removeProfilePictureUC: RemoveProfilePicture
) : PostListingViewModelBase() {
    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> get() = _profile

    var profileId: Int  = 0

    val followingState: MutableLiveData<FollowingState> = MutableLiveData()

    private val toRetry: MutableList<Runnable> = mutableListOf()

    val followingLoading: MutableLiveData<Boolean> = MutableLiveData()


    // runs the lists of "Runnable" one by one and clears list. After this is executed,
    // caller should report as handled
    fun retry() {
        toRetry.forEach {
            it.run()
        }

        toRetry.clear()
    }

    fun getProfile() {
        viewModelScope.launch {
            getProfileUseCase(profileId).onEach {
                when(it) {
                    is Resource.Error -> {
                        errorLoading.postValue(it.errorType)
                        toRetry.add {
                            getProfile()
                        }
                    }
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _profile.postValue(it.data!!)
                        followingState.postValue(
                            it.data.let { user-> getFollowingState(user.followingThisUser, user.thisUserIsFollowingMe) }
                        )
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    private fun getFollowingState(followingThisUser: Boolean, userIsFollowingMe: Boolean): FollowingState {
        return when {
            followingThisUser && userIsFollowingMe -> FollowingState.MutuallyFollowing
            followingThisUser -> FollowingState.FollowingThisUser
            userIsFollowingMe -> FollowingState.ThisUserIsFollowingMe
            else -> FollowingState.NotMutuallyFollowing
        }
    }

    fun setProfileImage(image: Image) {
        viewModelScope.launch {
            setProfileImageUC(image).onEach {
                _profile.postValue(_profile.value?.copy(profileImageId = image.id))
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun followUser() {
        val currentProfile = _profile.value ?: return
        val following = currentProfile.followingThisUser
        viewModelScope.launch {
            followUserUC(profileId, !following).onEach {
                when(it) {
                    is Resource.Error -> {
                        followingLoading.postValue(false)
                    }
                    is Resource.Loading -> {
                        followingLoading.postValue(true)
                    }
                    is Resource.Success -> {
                        followingLoading.postValue(false)
                        _profile.postValue(currentProfile.apply { followingThisUser = !following })
                        followingState.postValue(getFollowingState(!following, currentProfile.thisUserIsFollowingMe))
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    override fun getFeed(refresh: Boolean, hashtag: String?) {
        viewModelScope.launch {
            if(refresh) {
                posts.value = Pair(null, UpdateEvent(UpdateEvent.UpdateType.REFRESH, null))
                getLastId()
            }
            getProfilePostsUseCase(profileId, getLastId(), false, null).onEach { feedDtoResource ->
                when (feedDtoResource) {
                    is Resource.Success -> {
                        loadingPosts.postValue(false)
                        posts.postValue(Pair(posts.value?.first?.apply { addAll(feedDtoResource.data ?: listOf()) } ?: feedDtoResource.data, UpdateEvent(if(refresh) UpdateEvent.UpdateType.REFRESH else UpdateEvent.UpdateType.PAGE_ADDED, null)))
                        noMoreContent.postValue(feedDtoResource.data?.size == 0)
                    }

                    is Resource.Loading -> {
                        loadingPosts.postValue(true)
                    }

                    is Resource.Error -> {
                        errorLoading.postValue(feedDtoResource.errorType)
                        toRetry.add {
                            getFeed(refresh, hashtag)
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun setProfile(profile: UserProfile) {
        _profile.value = profile
    }

    fun removeCurrentProfileImage() {
        viewModelScope.launch {
            removeProfilePictureUC().onEach {
                when(it) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _profile.postValue(_profile.value?.copy(profileImageId = null))
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}