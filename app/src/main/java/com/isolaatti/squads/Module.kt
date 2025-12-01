package com.isolaatti.squads

import android.content.ContentResolver
import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.squads.data.SquadsApi
import com.isolaatti.squads.data.SquadsRepositoryImpl
import com.isolaatti.squads.domain.SquadsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    @Singleton
    fun provideSquadsApi(retrofit: RetrofitClient): SquadsApi {
        return retrofit.client.create(SquadsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSquadsRepository(squadsApi: SquadsApi, imagesApi: ImagesApi, contentResolver: ContentResolver): SquadsRepository {
        return SquadsRepositoryImpl(squadsApi, imagesApi, contentResolver)
    }
}