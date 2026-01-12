package com.isolaatti.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.isolaatti.BuildConfig
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val RecoverPasswordRelativePath = "/recuperacion_cuenta"
fun openForgotPassword(context: Context) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
        .launchUrl(context, Uri.parse("${BuildConfig.backend}$RecoverPasswordRelativePath"))
}

data class AuthorizationServiceConfigurationWrapper(
    val configuration: AuthorizationServiceConfiguration?,
    val exception: Exception?
)

suspend fun fetchAuthorizationServiceConfigurationFromIssuerAsync(issuer: Uri): AuthorizationServiceConfigurationWrapper {
    return suspendCoroutine { continuation ->
        AuthorizationServiceConfiguration.fetchFromIssuer(
            issuer
        ) { configuration, exception ->
            continuation.resume(AuthorizationServiceConfigurationWrapper(configuration, exception))
        }
    }
}

data class TokenResponseWrapper(
    val response: TokenResponse?,
    val exception: AuthorizationException?
)


