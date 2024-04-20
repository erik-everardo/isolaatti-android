package com.isolaatti.reports.data

import com.isolaatti.common.ResultDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReportsApi {

    @POST("reports")
    fun createReport(@Body createReportDto: CreateReportDto): Call<ReportDto>

    @GET("reports/by_me")
    fun gerReportsByMe(): Call<ResultDto<ReportDto>>
}