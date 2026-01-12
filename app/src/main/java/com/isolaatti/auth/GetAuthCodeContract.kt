package com.isolaatti.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

data class AuthorizationResponseWrapper(
    val response: AuthorizationResponse?,
    val exception: AuthorizationException?
)

class GetAuthCodeContract() : ActivityResultContract<AuthorizationRequest, AuthorizationResponseWrapper>() {
    override fun createIntent(
        context: Context,
        input: AuthorizationRequest
    ): Intent {
        val authorizationService = AuthorizationService(context)
        return authorizationService.getAuthorizationRequestIntent(input)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): AuthorizationResponseWrapper {
        if(intent != null) {
            val response = AuthorizationResponse.fromIntent(intent)
            val exception = AuthorizationException.fromIntent(intent)
            return AuthorizationResponseWrapper(response, exception)
        } else {
            return AuthorizationResponseWrapper(null, null)
        }
    }
}