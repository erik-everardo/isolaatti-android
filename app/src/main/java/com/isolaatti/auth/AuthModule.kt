package com.isolaatti.auth

import android.content.Context
import com.isolaatti.BuildConfig
import com.isolaatti.auth.data.AuthRepositoryImpl
import com.isolaatti.auth.data.AuthStateStorage
import com.isolaatti.auth.data.OidcConfig
import com.isolaatti.auth.data.UserInfoProviderImpl
import com.isolaatti.auth.domain.AuthRepository
import com.isolaatti.auth.domain.UserInfoProvider
import com.isolaatti.connectivity.AuthInterceptor
import com.isolaatti.profile.data.remote.ProfileApi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // -----------------------------------------------------------------------
    // OIDC Configuration
    // Reemplaza los valores con los de tu proveedor de identidad.
    // -----------------------------------------------------------------------

    @Provides
    @Singleton
    fun provideOidcConfig(): OidcConfig = OidcConfig(
        issuerUrl = BuildConfig.issuer,
        clientId = BuildConfig.ssoClientId,
        redirectUri = "com.isolaatti://auth-callback",
        scopes = listOf("openid", "profile", "email", "offline_access"),
        serviceConfiguration = null
    )

    @Provides
    @Singleton
    fun provideUserInfoProvider(profileApi: ProfileApi): UserInfoProvider {
        return UserInfoProviderImpl(profileApi)
    }

    @Provides
    @Singleton
    fun provideAuthStateStorage(
        @ApplicationContext context: Context,
    ): AuthStateStorage = AuthStateStorage(context)



    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        storage: AuthStateStorage,
        oidcConfig: OidcConfig,
        userInfoProvider: UserInfoProvider,
    ): AuthRepository = AuthRepositoryImpl(
        context = context,
        storage = storage,
        oidcConfig = oidcConfig,
        userInfoProvider = userInfoProvider,
    )

    @Provides
    @Singleton
    fun provideAuthInterceptor(authRepository: Lazy<AuthRepository>): AuthInterceptor = AuthInterceptor(authRepository)
}
