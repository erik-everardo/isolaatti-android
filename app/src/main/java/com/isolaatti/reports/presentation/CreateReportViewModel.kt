package com.isolaatti.reports.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.reports.data.ContentType
import com.isolaatti.reports.data.CreateReportDto
import com.isolaatti.reports.data.ReportDto
import com.isolaatti.reports.domain.ReportsRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(private val reportsRepository: ReportsRepository) : ViewModel() {
    val createReportResponse: MutableLiveData<Resource<ReportDto>> = MutableLiveData()

    fun createReport(reason: Int, comment: String, contentType: ContentType, contentId: String) {
        viewModelScope.launch {
            reportsRepository.createReport(CreateReportDto(reason, comment, contentType.ordinal, contentId)).onEach {
                createReportResponse.postValue(it)
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }
}