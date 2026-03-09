package com.isolaatti.auth.data

import net.openid.appauth.AuthorizationServiceConfiguration

data class OidcConfig(
    /** URL base del emisor (issuer). Se usa para discovery automático. */
    val issuerUrl: String,

    /** Client ID registrado en el proveedor de identidad. */
    val clientId: String,

    /**
     * URI de redirección registrada en el IdP y declarada en tu AndroidManifest.
     * Ejemplo: `"com.yourapp:/oauth2callback"`
     */
    val redirectUri: String,

    /**
     * Scopes solicitados. Siempre incluye "openid".
     * Ejemplo: `listOf("openid", "profile", "email", "offline_access")`
     */
    val scopes: List<String> = listOf("openid", "profile", "email", "offline_access"),

    /**
     * Configuración del servicio OIDC.
     * Se inyecta una vez obtenida (por discovery o manual).
     */
    var serviceConfiguration: AuthorizationServiceConfiguration? = null,
)
