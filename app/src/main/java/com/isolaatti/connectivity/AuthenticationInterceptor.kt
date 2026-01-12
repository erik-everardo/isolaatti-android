package com.isolaatti.connectivity

import com.isolaatti.auth.AuthStateStorage
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class PerformActionWithFreshTokensResult(
    val accessToken: String?,
    val idToken: String?,
    val ex: AuthorizationException?
)
class AuthenticationInterceptor @Inject constructor(private val authStateStorage: AuthStateStorage, private val authorizationService: AuthorizationService) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        val path = url.toUrl().path
        if(RetrofitClient.excludedUrlsFromAuthentication.contains(path)){
            return chain.proceed(chain.request())
        }

        val performActionWithFreshTokensResult = runBlocking {
            suspendCoroutine { continuation ->
                authStateStorage.authState.performActionWithFreshTokens(authorizationService) { accessToken, idToken, ex ->

                    continuation.resume(PerformActionWithFreshTokensResult(accessToken, idToken, ex))
                }
            }
        }

        if(performActionWithFreshTokensResult.accessToken != null) {
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${performActionWithFreshTokensResult.accessToken}")
                .addHeader("client-id", ClientId.guid.toString())
                .build()
            return chain.proceed(request)
        }

        return chain.proceed(chain.request())
    }

}