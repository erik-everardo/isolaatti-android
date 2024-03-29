package com.isolaatti.search

import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.database.AppDatabase
import com.isolaatti.search.data.SearchApi
import com.isolaatti.search.data.SearchDao
import com.isolaatti.search.data.SearchRepositoryImpl
import com.isolaatti.search.domain.SearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideSearchApi(retrofitClient: RetrofitClient): SearchApi {
        return retrofitClient.client.create(SearchApi::class.java)
    }

    @Provides
    fun provideSearchDao(database: AppDatabase): SearchDao {
        return database.searchHistoryDao()
    }

    @Provides
    fun provideSearchRepository(searchApi: SearchApi, searchDao: SearchDao): SearchRepository {
        return SearchRepositoryImpl(searchApi, searchDao)
    }
}