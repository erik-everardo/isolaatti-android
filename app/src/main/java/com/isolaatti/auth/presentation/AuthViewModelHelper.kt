package com.isolaatti.auth.presentation

import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.isolaatti.auth.domain.AuthRepository
import com.isolaatti.auth.domain.AuthenticationState
import com.isolaatti.profile.domain.entity.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

data class AuthDialogsState(
    val showAuthDialog: Boolean = false,
    val showAuthRequiredDialog: Boolean = false
)


class AuthViewModelHelper(private val authRepository: AuthRepository, private val viewModelScope: CoroutineScope) {

    val authState: StateFlow<AuthenticationState<UserProfile>> =
        authRepository.authState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AuthenticationState.Loading,
            )

    private val _authDialogsState = MutableStateFlow(AuthDialogsState())

    val authDialogsState: StateFlow<AuthDialogsState> = _authDialogsState.asStateFlow()

    /** Solicita el Intent para lanzar el flujo de autenticación OIDC. */
    fun onLoginRequested(onIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            runCatching { authRepository.buildAuthorizationIntent() }
                .onSuccess(onIntent)
                .onFailure { /* manejar error de construcción del intent */ }
        }
    }

    /**
     * Debe llamarse desde `onActivityResult` o el callback de
     * `registerForActivityResult` con los datos devueltos por AppAuth.
     */
    fun onAuthorizationResult(
        response: AuthorizationResponse?,
        exception: AuthorizationException?,
    ) {
        viewModelScope.launch {
            authRepository.handleAuthorizationResponse(response, exception)
        }
    }

    /** Cierra la sesión del usuario. */
    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun onCloseAuth() {
        viewModelScope.launch {
            _authDialogsState.update {
                it.copy(showAuthDialog = false)
            }
        }
    }

    fun onShowAuth() {
        viewModelScope.launch {
            _authDialogsState.update {
                it.copy(showAuthDialog = true)
            }
        }
    }

    fun closeAuthRequiredDialog() {
        viewModelScope.launch {
            _authDialogsState.update {
                it.copy(showAuthRequiredDialog = false)
            }
        }
    }

    fun onAuthRequired() {
        viewModelScope.launch {
            _authDialogsState.update {
                it.copy(showAuthRequiredDialog = true)
            }
        }
    }


}