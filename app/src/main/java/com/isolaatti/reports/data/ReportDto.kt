package com.isolaatti.reports.data

data class ReportDto(
    val id: String,
    val reportReason: Int,
    val userId: Int,
    val userComment: String,
    val status: Int
)