package com.isolaatti.audio

import androidx.media3.common.Player
import com.isolaatti.audio.common.data.AudiosApi
import com.isolaatti.audio.common.data.AudiosRepositoryImpl
import com.isolaatti.audio.common.domain.AudiosRepository
import com.isolaatti.audio.drafts.data.AudioDraftsRepositoryImpl
import com.isolaatti.audio.drafts.data.AudiosDraftsDao
import com.isolaatti.audio.drafts.domain.repository.AudioDraftsRepository
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

    @Provides
    fun provideAudiosRepository(audiosApi: AudiosApi, audiosDraftsDao: AudiosDraftsDao, userIdSetting: UserIdSetting): AudiosRepository {
        return AudiosRepositoryImpl(audiosApi, audiosDraftsDao, userIdSetting)
    }

    @Provides
    fun provideAudioDraftsDao(database: AppDatabase): AudiosDraftsDao {
        return database.audioDrafts()
    }

    @Provides
    fun provideAudioDraftsRepository(audiosDraftsDao: AudiosDraftsDao): AudioDraftsRepository {
        return AudioDraftsRepositoryImpl(audiosDraftsDao)
    }
}