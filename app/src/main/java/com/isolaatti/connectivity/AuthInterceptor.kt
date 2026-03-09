package com.isolaatti.connectivity

import com.isolaatti.auth.domain.AuthRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor OkHttp que:
 * 1. Añade el `Authorization: Bearer <token>` a cada petición.
 * 2. Si recibe un `401`, refresca el access token y reintenta **una sola vez**.
 *
 * Debe registrarse en tu [OkHttpClient] como `.addInterceptor(authInterceptor)`.
 */
class AuthInterceptor constructor(
    private val authRepository: Lazy<AuthRepository>,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authRepository.get().getFreshAccessToken() }

        val originalRequest = chain.request()

        // Si no hay token, dejamos pasar la petición sin header (el servidor decidirá)
        val authenticatedRequest = if (token != null) {
            originalRequest.newBuilder()
                .header(AUTHORIZATION, "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // Si el servidor devuelve 401, intentamos refrescar y reintentar una vez
        if (response.code == HTTP_UNAUTHORIZED) {
            response.close()

            val newToken = runBlocking { authRepository.get().refreshAccessToken() }
                ?: return response // No se pudo refrescar → devolver el 401 original

            val retryRequest = originalRequest.newBuilder()
                .header(AUTHORIZATION, "Bearer $newToken")
                .build()

            return chain.proceed(retryRequest)
        }

        return response
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
        const val HTTP_UNAUTHORIZED = 401
    }
}
