package com.isolaatti.home.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.profile.domain.use_case.GetProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getProfileUseCase: GetProfile,
) : ViewModel() {

    // User profile
    private val _userProfile: MutableLiveData<UserProfile> = MutableLiveData()
    val userProfile: LiveData<UserProfile> get() = _userProfile
}