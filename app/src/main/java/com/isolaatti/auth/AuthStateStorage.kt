package com.isolaatti.auth

import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.TokenResponse

interface AuthStateStorage {
    val authState: AuthState
    val isAuthenticated: StateFlow<Boolean>

    fun jsonDeserialize(json: String)

    fun update(authResponse: AuthorizationResponse?, authException: AuthorizationException?)

    fun update(tokenResponse: TokenResponse?, authException: AuthorizationException?)

    fun update(regResponse: RegistrationResponse?)

    fun persist()

    suspend fun getConfiguration(): AuthorizationServiceConfiguration?
}