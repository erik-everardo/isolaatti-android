package com.isolaatti.images

import android.content.ContentResolver
import android.content.Context
import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.database.AppDatabase
import com.isolaatti.images.common.data.dao.ImagesDraftsDao
import com.isolaatti.images.common.data.remote.ImagesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideImagesApi(retrofitClient: RetrofitClient): ImagesApi {
        return retrofitClient.client.create(ImagesApi::class.java)
    }

    @Provides
    fun provideContentResolver(@ApplicationContext application: Context): ContentResolver {
        return application.contentResolver
    }

    @Provides
    fun provideImagesDraftDao(database: AppDatabase): ImagesDraftsDao {
        return database.imagesDraftsDao()
    }
}