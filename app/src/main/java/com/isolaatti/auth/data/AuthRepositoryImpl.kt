package com.isolaatti.auth.data

import android.content.Context
import android.content.Intent
import com.isolaatti.auth.domain.AuthRepository
import com.isolaatti.auth.domain.AuthenticationState
import com.isolaatti.auth.domain.UserInfoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import com.isolaatti.profile.domain.entity.UserProfile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume

class AuthRepositoryImpl constructor(
    private val context: Context,
    private val storage: AuthStateStorage,
    private val oidcConfig: OidcConfig,
    private val userInfoProvider: UserInfoProvider,
) : AuthRepository {

    // -----------------------------------------------------------------------
    // Estado interno
    // -----------------------------------------------------------------------

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authState =
        MutableStateFlow<AuthenticationState<UserProfile>>(AuthenticationState.Loading)
    override val authState: StateFlow<AuthenticationState<UserProfile>> = _authState.asStateFlow()

    /** Mutex para evitar refrescos de token concurrentes. */
    private val refreshMutex = Mutex()

    private val authService: AuthorizationService by lazy {
        AuthorizationService(context)
    }

    // -----------------------------------------------------------------------
    // Inicialización: restaura estado desde DataStore
    // -----------------------------------------------------------------------

    init {
        scope.launch {
            val persisted = storage.readFromEncryptedPrefs()
            if (persisted != null && persisted.isAuthorized) {
                val token = persisted.accessToken
                if (token != null) {
                    _authState.value = AuthenticationState.Authenticated(accessToken = token)
                    fetchAndUpdateUserInfo(token)
                } else {
                    // Tenemos refresh token pero no access token → intentar refresh
                    val fresh = refreshAccessTokenInternal(persisted)
                    if (fresh != null) {
                        _authState.value = AuthenticationState.Authenticated(accessToken = fresh)
                        fetchAndUpdateUserInfo(fresh)
                    } else {
                        _authState.value = AuthenticationState.Unauthenticated
                    }
                }
            } else {
                _authState.value = AuthenticationState.Unauthenticated
            }
        }
    }

    // -----------------------------------------------------------------------
    // AuthRepository API
    // -----------------------------------------------------------------------

    override suspend fun buildAuthorizationIntent(): Intent {
        val config = requireServiceConfig()
        val request = AuthorizationRequest.Builder(
            config,
            oidcConfig.clientId,
            ResponseTypeValues.CODE,
            oidcConfig.redirectUri.toUri(),
        )
            .setScopes(oidcConfig.scopes)
            .build()

        return authService.getAuthorizationRequestIntent(request)
    }

    override suspend fun handleAuthorizationResponse(
        response: AuthorizationResponse?,
        exception: AuthorizationException?,
    ) {
        val currentState = storage.readFromEncryptedPrefs() ?: AuthState()
        currentState.update(response, exception)

        if (exception != null) {
            _authState.value = AuthenticationState.Error(exception)
            return
        }

        if (response != null) {
            storage.save(currentState)
            exchangeCodeForTokens(response)
        }
    }

    override suspend fun exchangeCodeForTokens(response: AuthorizationResponse) {
        val tokenRequest = response.createTokenExchangeRequest()
        val (tokenResponse, exception) = performTokenRequest(tokenRequest)

        val state = storage.readFromEncryptedPrefs() ?: AuthState()
        state.update(tokenResponse, exception)

        if (exception != null) {
            _authState.value = AuthenticationState.Error(exception)
            return
        }

        storage.save(state)

        val token = state.accessToken
        if (token != null) {
            _authState.value = AuthenticationState.Authenticated(accessToken = token)
            fetchAndUpdateUserInfo(token)
        } else {
            _authState.value = AuthenticationState.Error(
                IllegalStateException("Token exchange succeeded but access token is null")
            )
        }
    }

    override suspend fun refreshAccessToken(): String? = refreshMutex.withLock {
        val state = storage.readFromEncryptedPrefs() ?: return null
        refreshAccessTokenInternal(state)
    }

    override suspend fun getFreshAccessToken(): String? {
        val state = storage.readFromEncryptedPrefs() ?: return null
        return if (state.needsTokenRefresh) {
            refreshAccessToken()
        } else {
            state.accessToken
        }
    }

    override suspend fun logout() {
        storage.clear()
        _authState.value = AuthenticationState.Unauthenticated
    }

    override fun handleUserProfileUpdate(userProfile: UserProfile) {
        _authState.update {
            if(it is AuthenticationState.Authenticated) {
                it.copy(userInfo = userProfile)
            } else {
                it
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /**
     * Refresca el token sin adquirir el mutex (ya debe estar tomado por el caller).
     */
    private suspend fun refreshAccessTokenInternal(authState: AuthState): String? {
        val refreshRequest = authState.createTokenRefreshRequest() ?: return null
        val (tokenResponse, exception) = performTokenRequest(refreshRequest)

        authState.update(tokenResponse, exception)

        return if (exception != null || tokenResponse == null) {
            // Refresh falló → forzar re-login
            storage.clear()
            _authState.value = AuthenticationState.Unauthenticated
            null
        } else {
            storage.save(authState)
            authState.accessToken
        }
    }

    /**
     * Llama a [UserInfoProvider.fetchUserInfo] y actualiza el estado con los datos.
     */
    private fun fetchAndUpdateUserInfo(accessToken: String) {
        scope.launch {
            runCatching { userInfoProvider.fetchUserInfo(accessToken) }
                .onSuccess { userInfo ->
                    _authState.value = AuthenticationState.Authenticated(
                        accessToken = accessToken,
                        userInfo = userInfo,
                    )
                }
                .onFailure { error ->
                    // Mantener autenticado aunque falle la carga de datos del usuario
                    // Puedes cambiar este comportamiento según tus necesidades
                    val current = _authState.value
                    if (current is AuthenticationState.Authenticated) {
                        _authState.value = current.copy(userInfo = null)
                    }
                }
        }
    }

    /**
     * Wrapper suspendido para [AuthorizationService.performTokenRequest].
     */
    private suspend fun performTokenRequest(
        request: TokenRequest,
    ): Pair<TokenResponse?, AuthorizationException?> =
        suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request) { response, exception ->
                cont.resume(response to exception)
            }
        }

    /**
     * Obtiene la configuración del servicio OIDC.
     * Lanza [IllegalStateException] si aún no está disponible.
     */
    private fun requireServiceConfig(): AuthorizationServiceConfiguration =
        oidcConfig.serviceConfiguration
            ?: error(
                "OidcConfig.serviceConfiguration is null. " +
                        "Fetch it via AuthorizationServiceConfiguration.fetchFromIssuer() " +
                        "before calling buildAuthorizationIntent()."
            )
}
