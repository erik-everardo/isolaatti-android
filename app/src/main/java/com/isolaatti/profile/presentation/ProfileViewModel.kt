package com.isolaatti.profile.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isolaatti.followers.domain.FollowingState
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.domain.FeedPageSize
import com.isolaatti.posting.posts.presentation.PostListingViewModelBase
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.profile.domain.use_case.DeleteProfileAudio
import com.isolaatti.profile.domain.use_case.FollowUser
import com.isolaatti.profile.domain.use_case.GetProfile
import com.isolaatti.profile.domain.use_case.GetProfilePosts
import com.isolaatti.profile.domain.use_case.RemoveProfilePicture
import com.isolaatti.profile.domain.use_case.SetProfileAudio
import com.isolaatti.profile.domain.use_case.SetProfileImage
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfile,
    private val getProfilePostsUseCase: GetProfilePosts,
    private val setProfileImageUC: SetProfileImage,
    private val followUserUC: FollowUser,
    private val removeProfilePictureUC: RemoveProfilePicture,
    private val setProfileAudio: SetProfileAudio,
    private val deleteProfileAudio: DeleteProfileAudio
) : PostListingViewModelBase() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> get() = _profile
    val loadingProfile: MutableStateFlow<Boolean> = MutableStateFlow(true)

    var profileId: Int  = 0

    val followingState: MutableLiveData<FollowingState> = MutableLiveData()

    private val toRetry: MutableList<Runnable> = mutableListOf()

    val followingLoading: MutableLiveData<Boolean> = MutableLiveData()

    val isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val showConfirmChangeProfilePictureBottomSheet: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val newProfileImage: MutableStateFlow<Uri?> = MutableStateFlow(null)

    val uploadingProfilePicture: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val errorProfilePicture: MutableStateFlow<Resource.Error<RemoteImage>?> = MutableStateFlow(null)

    var audioDescriptionToUpload: Uri? = null


    val showAudioRecorder = MutableStateFlow(false)
    val setAudioDescriptionError = MutableStateFlow(false)

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
                        loadingProfile.value = false
                        errorLoading.postValue(it.errorType)
                        toRetry.add {
                            getProfile()
                        }
                    }
                    is Resource.Loading -> {
                        loadingProfile.value = true
                    }
                    is Resource.Success -> {
                        loadingProfile.value = false
                        _profile.value = it.data!!
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

    fun setProfileImage() {
        viewModelScope.launch {
            newProfileImage.value?.let {
                setProfileImageUC(LocalImage(it)).onEach { resource ->
                    when(resource) {
                        is Resource.Error<RemoteImage> -> {
                            errorProfilePicture.value = resource
                            uploadingProfilePicture.value = false
                        }
                        is Resource.Loading<RemoteImage> -> {
                            errorProfilePicture.value = null
                            uploadingProfilePicture.value = true
                        }
                        is Resource.Success<RemoteImage> -> {
                            errorProfilePicture.value = null
                            uploadingProfilePicture.value = false
                            showConfirmChangeProfilePictureBottomSheet.value = false
                            _profile.value = _profile.value?.copy(profileImageId = resource.data?.id)
                        }
                    }

                }.flowOn(Dispatchers.IO).launchIn(this)
            }
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
                        _profile.value = currentProfile.apply { followingThisUser = !following }
                        followingState.postValue(getFollowingState(!following, currentProfile.thisUserIsFollowingMe))
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    override fun getFeed(refresh: Boolean, hashtag: String?) {
        viewModelScope.launch {
            if(refresh) {
                posts.value = emptyList()
            }
            getProfilePostsUseCase(profileId, getLastId(), false, null).onEach { feedDtoResource ->
                when (feedDtoResource) {
                    is Resource.Success -> {
                        if(refresh) {
                            isRefreshing.value = false
                        } else {
                            loadingPosts.value = false
                        }

                        posts.value += feedDtoResource.data ?: emptyList()
                        noMoreContent.value = ((feedDtoResource.data?.size ?: 0) < FeedPageSize)
                    }

                    is Resource.Loading -> {
                        if(refresh) {
                            isRefreshing.value = true
                        } else {
                            loadingPosts.value = true
                        }

                    }

                    is Resource.Error -> {
                        if(refresh) {
                            isRefreshing.value = true
                        } else {
                            loadingPosts.value = true
                        }
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
                        _profile.value = _profile.value?.copy(profileImageId = null)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun setAudioDescriptionProfile() {
        viewModelScope.launch {
            audioDescriptionToUpload?.let {
                setProfileAudio(it, 0).onEach { response ->
                    when(response) {
                        is Resource.Error -> {
                            Log.d(TAG, "Error")
                            setAudioDescriptionError.value = true
                            uploadingAudio.value = false
                        }
                        is Resource.Loading -> {
                            setAudioDescriptionError.value = false
                            uploadingAudio.value = true
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "Success audio = ${response.data}")
                            setAudioDescriptionError.value = false
                            uploadingAudio.value = false
                            showAudioRecorder.value = false
                            _profile.update {
                                it?.copy(descriptionAudio = response.data)
                            }
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }

        }
    }

    fun removeAudioProfile() {
        viewModelScope.launch {
            deleteProfileAudio().onEach { resource ->
                when(resource) {
                    is Resource.Error<*> -> {}
                    is Resource.Loading<*> -> {}
                    is Resource.Success<*> -> {
                        _profile.update {
                            it?.copy(descriptionAudio = null)
                        }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun hideConfirmPictureSheet() {
        viewModelScope.launch {
            showConfirmChangeProfilePictureBottomSheet.value = false
            newProfileImage.value = null
        }
    }

    fun onProfilePictureSelected(uri: Uri) {
        viewModelScope.launch {
            showConfirmChangeProfilePictureBottomSheet.value = true
            newProfileImage.value = uri
        }
    }
}