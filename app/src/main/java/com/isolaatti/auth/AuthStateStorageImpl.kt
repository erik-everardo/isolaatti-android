package com.isolaatti.auth

import android.util.Log
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.isolaatti.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationResponse
import net.openid.appauth.TokenResponse

/**
 * Global storage for authentication state.
 */
class AuthStateStorageImpl(val dataStore: DataStore<Preferences>) : AuthStateStorage {

    companion object {
        const val LOG_TAG = "AuthStateStorageImpl"
        val preferencesKey = stringPreferencesKey("auth_state")
    }

    private var _authState = AuthState()

    override val authState: AuthState
        get() = _authState

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated = _isAuthenticated.asStateFlow()

    /**
     * Load authentication state from persistent storage into this singleton.
     */
    override fun jsonDeserialize(json: String) {
        val authState = AuthState.jsonDeserialize(json)
        this._authState = authState
        _isAuthenticated.update {
            authState.isAuthorized
        }
    }

    override fun update(authResponse: AuthorizationResponse?, authException: AuthorizationException?) {
        _authState.update(authResponse, authException)
        persist()
        _isAuthenticated.update {
            authState.isAuthorized
        }
    }

    override fun update(tokenResponse: TokenResponse?, authException: AuthorizationException?) {
        _authState.update(tokenResponse, authException)
        persist()
        _isAuthenticated.update {
            authState.isAuthorized
        }
    }

    override fun update(regResponse: RegistrationResponse?) {
        _authState.update(regResponse)
        persist()
        _isAuthenticated.update {
            authState.isAuthorized
        }
    }

    override fun persist() {
        MyApplication.myApp.coroutineScope.launch {
            dataStore.edit {
                it[preferencesKey] = _authState.jsonSerializeString()
            }
        }
    }

    private var _configuration: AuthorizationServiceConfiguration? = null


    override suspend fun getConfiguration(): AuthorizationServiceConfiguration? {
        if(_configuration != null) {
            return _configuration!!
        }

        val configurationWrapped = fetchAuthorizationServiceConfigurationFromIssuerAsync(
            "https://sso.isolaatti.com/realms/Isolaatti/".toUri()
        )

        if(configurationWrapped.configuration != null) {
            _configuration = configurationWrapped.configuration
            return configurationWrapped.configuration
        } else {
            Log.e(LOG_TAG, "Error getting authorization configuration ${configurationWrapped.exception}")
            return null
        }
    }
}