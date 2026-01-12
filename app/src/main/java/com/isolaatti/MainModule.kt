package com.isolaatti

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.isolaatti.auth.AuthStateStorage
import com.isolaatti.auth.AuthStateStorageImpl
import com.isolaatti.connectivity.AuthenticationInterceptor
import com.isolaatti.connectivity.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {
    @Provides
    fun provideDataStorePreferences(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideAuthStateStorage(dataStore: DataStore<Preferences>): AuthStateStorage {
        return AuthStateStorageImpl(dataStore)
    }

    @Provides
    fun provideRetrofitClient(authenticationInterceptor: AuthenticationInterceptor) : RetrofitClient {
        return RetrofitClient(authenticationInterceptor)
    }

    @Provides
    fun provideAuthorizationService(@ApplicationContext context: Context) : AuthorizationService {
        return AuthorizationService(context)
    }
}