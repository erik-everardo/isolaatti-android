package com.isolaatti.auth.domain

import android.content.Intent
import com.isolaatti.profile.domain.entity.UserProfile
import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

/**
 * Contrato desacoplado del manejador de autenticación.
 *
 * [UserInfo] es el tipo que tú defines para los datos del usuario
 * obtenidos desde tu backend.
 */
interface AuthRepository {

    /**
     * Estado reactivo de autenticación.
     * Emite [AuthenticationState.Loading] al inicio, luego evoluciona
     * según las acciones realizadas.
     */
    val authState: StateFlow<AuthenticationState<UserProfile>>

    /**
     * Construye el [Intent] que lanza el flujo de autenticación OIDC.
     * Úsalo con `registerForActivityResult` o `startActivityForResult`.
     */
    suspend fun buildAuthorizationIntent(): Intent

    /**
     * Debe llamarse con el resultado recibido tras el flujo de autorización
     * (en `onActivityResult` o el callback de `ActivityResultLauncher`).
     *
     * @param response  Respuesta exitosa de AppAuth (puede ser null si hubo error).
     * @param exception Excepción de AppAuth (puede ser null si fue exitoso).
     */
    suspend fun handleAuthorizationResponse(
        response: AuthorizationResponse?,
        exception: AuthorizationException?,
    )

    /**
     * Intercambia el código de autorización por tokens y actualiza el estado.
     * Se llama internamente desde [handleAuthorizationResponse], pero queda
     * expuesto por si necesitas forzarlo.
     */
    suspend fun exchangeCodeForTokens(response: AuthorizationResponse)

    /**
     * Refresca el access token usando el refresh token almacenado.
     * El interceptor OkHttp lo invoca automáticamente ante un 401.
     *
     * @return Nuevo access token o null si el refresh falló.
     */
    suspend fun refreshAccessToken(): String?

    /**
     * Obtiene un access token válido. Si el actual expiró, intenta refrescarlo.
     * Útil para construir peticiones manualmente.
     */
    suspend fun getFreshAccessToken(): String?

    /**
     * Cierra la sesión: limpia el estado persistido y emite [AuthenticationState.Unauthenticated].
     */
    suspend fun logout()

    fun handleUserProfileUpdate(userProfile: UserProfile)
}
