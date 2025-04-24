package com.isolaatti.audio

import com.isolaatti.audio.common.data.AudiosApi
import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.database.AppDatabase
import com.isolaatti.settings.domain.UserIdSetting
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideAudiosApi(retrofitClient: RetrofitClient): AudiosApi {
        return retrofitClient.client.create(AudiosApi::class.java)
    }
}