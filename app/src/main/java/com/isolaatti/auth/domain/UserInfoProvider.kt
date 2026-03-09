package com.isolaatti.auth.domain

import com.isolaatti.profile.domain.entity.UserProfile

/**
 * Contrato para obtener la información del usuario desde tu backend.
 *
 * Implementa esta interfaz con tu lógica de red (Retrofit, Ktor, etc.).
 * El tipo de retorno [UserInfo] lo defines tú.
 *
 * Ejemplo de implementación:
 * ```kotlin
 * class MyUserInfoProvider @Inject constructor(
 *     private val api: MyBackendApi
 * ) : UserInfoProvider<MyUser> {
 *     override suspend fun fetchUserInfo(accessToken: String): MyUser {
 *         return api.getMe("Bearer $accessToken")
 *     }
 * }
 * ```
 */
interface UserInfoProvider {

    /**
     * Realiza la petición al backend para obtener datos del usuario.
     *
     * @param accessToken Token de acceso vigente.
     * @return Datos del usuario del tipo que tú definas.
     * @throws Exception si la petición falla.
     */
    suspend fun fetchUserInfo(accessToken: String): UserProfile
}
