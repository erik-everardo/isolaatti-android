package com.isolaatti.settings.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.auth.domain.UserInfo
import com.isolaatti.auth.domain.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    val userInfo: MutableLiveData<UserInfo> = MutableLiveData()
    fun getUserInfo() {
        /*
        viewModelScope.launch {
            userInfoRepository.getCurrentUserInfo().onEach {
                userInfo.postValue(it)
            }.flowOn(Dispatchers.IO).launchIn(this)
        }

         */
    }
}