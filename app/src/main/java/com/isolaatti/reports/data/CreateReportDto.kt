package com.isolaatti.reports.data

data class CreateReportDto(
    val reason: Int,
    val comment: String,
    val contentType: Int,
    val contentId: String
)