package com.isolaatti.auth.data

import android.content.Context
import androidx.security.crypto.MasterKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthState
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// DataStore cifrado con EncryptedFile + Preferences DataStore
// ---------------------------------------------------------------------------
// Nota: Preferences DataStore no soporta cifrado nativo aún.
// La estrategia aquí es cifrar el JSON del AuthState con EncryptedSharedPreferences
// y usar DataStore para la reactividad. Para un cifrado robusto se usa
// EncryptedSharedPreferences directamente como almacén subyacente.
// ---------------------------------------------------------------------------

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_prefs"
)

/**
 * Persistencia segura del [AuthState] de AppAuth.
 *
 * Utiliza [androidx.security.crypto.EncryptedSharedPreferences] para cifrar
 * el JSON del estado con una clave manejada por Android Keystore, y expone
 * un [Flow] reactivo a través de DataStore.
 */
@Singleton
class AuthStateStorage constructor(private val context: Context) {

    private val authStateKey = stringPreferencesKey("oidc_auth_state")

    // -----------------------------------------------------------------------
    // EncryptedSharedPreferences como capa de cifrado
    // -----------------------------------------------------------------------

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            "secure_auth_state",
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /**
     * Flujo reactivo del [AuthState] almacenado.
     * Emite `null` si no hay estado persistido.
     */
    val authStateFlow: Flow<AuthState?> = context.authDataStore.data.map { prefs ->
        // DataStore actúa como trigger de cambios; el valor real viene de EncryptedPrefs
        prefs[authStateKey]   // usamos solo como señal de versión
        readFromEncryptedPrefs()
    }

    /** Persiste el [AuthState] de forma segura. */
    suspend fun save(authState: AuthState) {
        val json = authState.jsonSerializeString()
        encryptedPrefs.edit().putString(ENCRYPTED_KEY, json).apply()
        // Actualiza DataStore con un token de versión para disparar el Flow
        context.authDataStore.edit { prefs ->
            prefs[authStateKey] = System.currentTimeMillis().toString()
        }
    }

    /** Elimina el estado almacenado (logout). */
    suspend fun clear() {
        encryptedPrefs.edit().remove(ENCRYPTED_KEY).apply()
        context.authDataStore.edit { prefs ->
            prefs.remove(authStateKey)
        }
    }

    /** Lee sincrónicamente desde EncryptedSharedPreferences. */
    fun readFromEncryptedPrefs(): AuthState? {
        val json = encryptedPrefs.getString(ENCRYPTED_KEY, null) ?: return null
        return runCatching { AuthState.jsonDeserialize(json) }.getOrNull()
    }

    private companion object {
        const val ENCRYPTED_KEY = "auth_state_json"
    }
}
