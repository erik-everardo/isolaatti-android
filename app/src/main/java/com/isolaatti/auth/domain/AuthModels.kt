package com.isolaatti.auth.domain

/**
 * Representa el estado de autenticación expuesto al resto de la app.
 * El tipo [UserInfo] lo defines tú según la respuesta de tu backend.
 */
sealed class AuthenticationState<out UserInfo> {

    /** Aún no se ha determinado el estado (app recién abierta). */
    data object Loading : AuthenticationState<Nothing>()

    /** No hay sesión activa. */
    data object Unauthenticated : AuthenticationState<Nothing>()

    /**
     * Sesión activa y válida.
     *
     * @param accessToken Token de acceso vigente.
     * @param userInfo    Datos del usuario obtenidos desde el backend (null mientras se cargan).
     */
    data class Authenticated<UserInfo>(
        val accessToken: String,
        val userInfo: UserInfo? = null,
    ) : AuthenticationState<UserInfo>()

    /** Error irrecuperable (e.g. tokens expirados y sin posibilidad de refresh). */
    data class Error(val cause: Throwable) : AuthenticationState<Nothing>()
}
