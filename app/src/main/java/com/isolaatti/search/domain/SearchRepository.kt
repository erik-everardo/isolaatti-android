package com.isolaatti.search.domain

import com.isolaatti.search.data.HashtagsDto
import com.isolaatti.search.data.NewestUsersDto
import com.isolaatti.search.data.SearchDto
import com.isolaatti.search.data.SearchHistoryEntity
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun search(query: String): Flow<Resource<SearchDto>>

    fun getSuggestions(query: String): Flow<Resource<List<SearchHistoryEntity>>>

    fun removeSuggestion(query: String): Flow<Resource<Boolean>>

    fun getTrendingHashtags(): Flow<Resource<HashtagsDto>>
    fun getNewestUsers(): Flow<Resource<NewestUsersDto>>
}