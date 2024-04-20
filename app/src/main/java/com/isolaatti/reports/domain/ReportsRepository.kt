package com.isolaatti.reports.domain

import com.isolaatti.reports.data.CreateReportDto
import com.isolaatti.reports.data.ReportDto
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {
    fun createReport(createReportDto: CreateReportDto): Flow<Resource<ReportDto>>

    fun getReportsByMe(): Flow<Resource<List<ReportDto>>>
}