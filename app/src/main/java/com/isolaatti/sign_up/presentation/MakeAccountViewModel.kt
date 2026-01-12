package com.isolaatti.sign_up.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.sign_up.domain.SignUpRepository
import com.isolaatti.sign_up.domain.entity.SignUpResult
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MakeAccountViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository,
) : ViewModel() {

    val formIsValid: MutableLiveData<Boolean> = MutableLiveData(false)
    val passwordIsValid: MutableLiveData<Boolean> = MutableLiveData()
    var usernameIsValid: MutableLiveData<Boolean> = MutableLiveData()
    val displayNameIsValid: MutableLiveData<Boolean> = MutableLiveData()
    val signUpResult: MutableLiveData<Resource<SignUpResult>?> = MutableLiveData()
    val sessionSaved: MutableLiveData<Boolean?> = MutableLiveData()
    var code: String? = null

    private fun validateForm() {
        formIsValid.value = passwordIsValid.value == true && usernameIsValid.value == true
    }

    var password: String = ""
        set(value) {
            field = value
            passwordIsValid.value = value.length > 7
            validateForm()
        }

    var username: String = ""
        set(value) {
            field = value
            checkUsernameAvailability(value)
        }

    var displayName: String = ""
        set(value) {
            field = value
            displayNameIsValid.value = value.isNotBlank()
        }

    private fun checkUsernameAvailability(username: String) {
        if(username.length < 3) {
            usernameIsValid.value = false
            validateForm()
            return
        }
        viewModelScope.launch {
            signUpRepository.checkUsernameAvailability(username).onEach {
                when(it) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        usernameIsValid.postValue(it.data!!)
                    }
                }

            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun makeAccount() {
        if(code==null) {
            return
        }
        viewModelScope.launch {
            signUpRepository.signUpWithCode(username, displayName, password, code!!).onEach {
                signUpResult.postValue(it)
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}