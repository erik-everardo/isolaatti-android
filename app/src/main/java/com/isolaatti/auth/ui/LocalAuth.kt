package com.isolaatti.auth.ui

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.isolaatti.auth.domain.AuthenticationState
import com.isolaatti.profile.domain.entity.UserProfile

data class Auth(
    val authState: AuthenticationState<UserProfile> = AuthenticationState.Loading,
    val doAuth: () -> Unit = {},
    val onAuthRequired: () -> Unit = {},
    val showAuthDialog: () -> Unit = {}
) {
    fun withAuth(block: () -> Unit) {
       if(authState is AuthenticationState.Authenticated) {
           block()
       } else {
           onAuthRequired()
       }
    }
}

val LocalAuth = staticCompositionLocalOf { Auth() }