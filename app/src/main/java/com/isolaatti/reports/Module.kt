package com.isolaatti.reports

import com.isolaatti.connectivity.RetrofitClient
import com.isolaatti.reports.data.ReportsApi
import com.isolaatti.reports.data.ReportsRepositoryImpl
import com.isolaatti.reports.domain.ReportsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Module {
    @Provides
    fun provideReportsApi(retrofitClient: RetrofitClient): ReportsApi {
        return retrofitClient.client.create(ReportsApi::class.java)
    }

    @Provides
    fun provideReportsRepository(reportsApi: ReportsApi): ReportsRepository {
        return ReportsRepositoryImpl(reportsApi)
    }
}