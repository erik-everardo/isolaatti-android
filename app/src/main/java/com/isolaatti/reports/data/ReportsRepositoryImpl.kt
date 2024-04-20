package com.isolaatti.reports.data

import android.util.Log
import com.isolaatti.reports.domain.ReportsRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(private val reportsApi: ReportsApi) : ReportsRepository {
    companion object {
        const val LOG_TAG = "ReportsRepositoryImpl"
    }
    override fun createReport(createReportDto: CreateReportDto): Flow<Resource<ReportDto>> = flow {
        try {
            emit(Resource.Loading())
            val response = reportsApi.createReport(createReportDto).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(e: Exception) {
            Log.e(LOG_TAG, "error creating report: ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.OtherError))
        }
    }

    override fun getReportsByMe(): Flow<Resource<List<ReportDto>>> {
        TODO("Not yet implemented")
    }
}