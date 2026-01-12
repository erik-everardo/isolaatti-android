package com.isolaatti.profile

import android.content.ContentResolver
import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.database.AppDatabase
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.profile.data.local.UserProfileDao
import com.isolaatti.profile.data.remote.ProfileApi
import com.isolaatti.profile.data.repository.ProfileRepositoryImpl
import com.isolaatti.profile.domain.ProfileRepository
import com.isolaatti.settings.domain.UserIdSetting
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideProfileApi(retrofitClient: RetrofitClient): ProfileApi {
        return retrofitClient.client.create(ProfileApi::class.java)
    }


    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideProfileRepository(profileApi: ProfileApi, imagesApi: ImagesApi, contentResolver: ContentResolver, userIdSetting: UserIdSetting, userProfileDao: UserProfileDao): ProfileRepository {
        return ProfileRepositoryImpl(profileApi, imagesApi, contentResolver, userIdSetting, userProfileDao)
    }


}