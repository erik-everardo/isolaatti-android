package com.isolaatti.search.data

import android.util.Log
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.search.domain.SearchRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse

class SearchRepositoryImpl(private val searchApi: SearchApi, private val searchDao: SearchDao) : SearchRepository {
    companion object {
        const val LOG_TAG = "SearchRepositoryImpl"
    }
    override fun search(query: String): Flow<Resource<SearchDto>> = flow {
        searchDao.insertTermTransaction(SearchHistoryEntity(0, query, System.currentTimeMillis()))

        try {
            val response = searchApi.quickSearch(query).awaitResponse()
            if(response.isSuccessful) {
                val dto = response.body()
                if(dto == null) {
                    Log.e(LOG_TAG, "Emitting error, could not get body")
                    emit(Resource.Error(Resource.Error.ErrorType.OtherError))
                    return@flow
                }
                emit(Resource.Success(dto))

            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(e: Exception) {
            Log.e(LOG_TAG, "Error searching: ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun getSuggestions(query: String): Flow<Resource<List<SearchHistoryEntity>>> = flow {
        emit(Resource.Loading())

        if(query.isEmpty()) {
            emit(Resource.Success(searchDao.getTerms()))
        } else {
            emit(Resource.Success(searchDao.getTermsForQuery(query)))
        }

    }

    override fun removeSuggestion(query: String): Flow<Resource<Boolean>> = flow {
        searchDao.deleteTerm(query)
        emit(Resource.Success(true))
    }

    override fun getTrendingHashtags(): Flow<Resource<HashtagsDto>> = flow {
        emit(Resource.Loading())

        try {
            val result = searchApi.getTrendingHashtags().awaitResponse()
            if(result.isSuccessful) {
                val dto = result.body()
                dto?.also { emit(Resource.Success(it)) }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
            }
        } catch(e: Exception) {
            Log.e(LOG_TAG, "Error getting trending hashtags: ${e.message}")
        }
    }

    override fun getNewestUsers(after: Int?): Flow<Resource<List<ProfileListItem>>> = flow {
        emit(Resource.Loading())

        try {
            val result = searchApi.getNewestUsers(10, after).awaitResponse()

            if(result.isSuccessful) {
                val dto = result.body()
                if(dto != null) {
                    emit(Resource.Success(dto.result.map { ProfileListItem.fromDto(it) }))
                }

            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
            }
        } catch(e: Exception) {
            Log.e(LOG_TAG, "Error getting newest users: ${e.message}")
        }
    }
}