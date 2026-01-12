package com.isolaatti.settings

import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.database.AppDatabase
import com.isolaatti.settings.data.KeyValueDao
import com.isolaatti.settings.data.SettingsRepositoryImpl
import com.isolaatti.settings.data.remote.AccountSettingsApi
import com.isolaatti.settings.data.repository.AccountSettingsRepositoryImpl
import com.isolaatti.settings.domain.AccountSettingsRepository
import com.isolaatti.settings.domain.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideKeyValueDao(database: AppDatabase): KeyValueDao {
        return database.keyValueDao()
    }

    @Provides
    fun provideSettingsRepository(keyValueDao: KeyValueDao): SettingsRepository {
        return SettingsRepositoryImpl(keyValueDao)
    }

    @Provides
    fun provideAccountSettingsApi(retrofitClient: RetrofitClient): AccountSettingsApi {
        return retrofitClient.client.create(AccountSettingsApi::class.java)
    }

    @Provides
    fun provideAccountSettingsRepository(accountSettingsApi: AccountSettingsApi): AccountSettingsRepository {
        return AccountSettingsRepositoryImpl(accountSettingsApi)
    }
}