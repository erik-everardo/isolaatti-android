package com.isolaatti.search.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isolaatti.search.data.HashtagsDto
import com.isolaatti.search.data.NewestUsersDto
import com.isolaatti.search.data.SearchDto
import com.isolaatti.search.data.SearchHistoryEntity
import com.isolaatti.search.domain.SearchRepository
import com.isolaatti.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) : ViewModel() {
    companion object {
        const val LOG_TAG = "SearchViewModel"
    }

    val searchSuggestions: MutableLiveData<List<SearchHistoryEntity>> = MutableLiveData()
    val searchResults: MutableLiveData<SearchDto> = MutableLiveData()
    val trendingHashtags: MutableLiveData<HashtagsDto> = MutableLiveData()
    val newestUsers: MutableLiveData<NewestUsersDto> = MutableLiveData()

    var searchQuery = ""

    fun search() {
        Log.d(LOG_TAG, searchQuery)
        viewModelScope.launch {
            if(searchQuery.isNotEmpty()) {
                searchRepository.search(searchQuery).onEach {
                    when(it) {
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            searchResults.postValue(it.data!!)
                        }
                    }
                }.flowOn(Dispatchers.IO).launchIn(this)
            }



        }
    }

    fun getSuggestions(query: String) {
        searchQuery = query
        viewModelScope.launch {
            searchRepository.getSuggestions(query).onEach {
                when(it) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        searchSuggestions.postValue(it.data!!)
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun deleteSuggestion(suggestion: SearchHistoryEntity) {
        viewModelScope.launch {
            searchRepository.removeSuggestion(suggestion.query).onEach { resource ->
                if(resource is Resource.Success && resource.data == true) {
                    val searchSuggestionsMutable = searchSuggestions.value?.toMutableList()
                    searchSuggestionsMutable?.remove(suggestion)

                    searchSuggestionsMutable?.also { searchSuggestions.postValue(it) }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun loadTendingHashtags() {
        viewModelScope.launch {
            searchRepository.getTrendingHashtags().onEach { resource ->
                when(resource) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        resource.data?.also { trendingHashtags.postValue(it) }
                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun loadNewestUsers() {
        viewModelScope.launch {
            searchRepository.getNewestUsers().onEach { resource ->
                when(resource) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        resource.data?.also { newestUsers.postValue(it) }

                    }
                }
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

}