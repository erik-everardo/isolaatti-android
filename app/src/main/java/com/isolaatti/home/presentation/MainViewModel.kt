package com.isolaatti.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.auth.domain.AuthRepository
import com.isolaatti.auth.presentation.AuthViewModelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(authRepository: AuthRepository) : ViewModel() {


    val authViewModelHelper: AuthViewModelHelper = AuthViewModelHelper(authRepository, viewModelScope)



}