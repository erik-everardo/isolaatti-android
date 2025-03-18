package com.isolaatti.profile

import android.content.ContentResolver
import com.isolaatti.auth.data.local.UserInfoDao
import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.database.AppDatabase
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.profile.data.remote.ProfileApi
import com.isolaatti.profile.data.repository.ProfileRepositoryImpl
import com.isolaatti.profile.domain.ProfileRepository
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
    fun provideUserInfoDao(database: AppDatabase): UserInfoDao {
        return database.userInfoDao()
    }

    @Provides
    fun provideProfileRepository(profileApi: ProfileApi, userInfoDao: UserInfoDao, imagesApi: ImagesApi, contentResolver: ContentResolver): ProfileRepository {
        return ProfileRepositoryImpl(profileApi, userInfoDao, imagesApi, contentResolver)
    }


}