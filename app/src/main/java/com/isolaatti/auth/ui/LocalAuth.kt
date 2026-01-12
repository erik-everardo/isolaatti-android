package com.isolaatti.auth.ui

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

data class Auth(
    val isAuthenticated: Boolean,
    val doAuth: () -> Unit
)

val LocalAuth = staticCompositionLocalOf { Auth(false, doAuth = {}) }