package com.isolaatti.squads.data

import android.util.Log
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.awaitResponse
import java.io.IOException

class SquadsRepositoryImpl(private val squadsApi: SquadsApi) : SquadsRepository {
    override fun createSquad(squad: CreateSquadDto): Flow<Resource<CreateSquadResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.createSquad(squad).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "createSquad: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }

    }

    override fun getMySquads(lastId: String?): Flow<Resource<List<SquadDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.getMySquads(lastId).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()?.squads))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "getMySquads: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } catch (e: HttpException) {
            Log.e("SquadsRepositoryImpl", "getMySquads: HttpException ${e.message}")
            emit(Resource.Error(Resource.Error.mapErrorCode(e.code())))
        }
    }

    override fun getSquad(squadId: String): Flow<Resource<SquadDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.getSquad(squadId).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()?.squad))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "getSquad: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } catch (e: HttpException) {
            Log.e("SquadsRepositoryImpl", "getSquad: HttpException ${e.message}")
            emit(Resource.Error(Resource.Error.mapErrorCode(e.code())))
        }
    }

    override fun deleteSquad(squadId: String): Flow<Resource<Void>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.deleteSquad(squadId).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "deleteSquad: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } catch (e: HttpException) {
            Log.e("SquadsRepositoryImpl", "deleteSquad: HttpException ${e.message}")
            emit(Resource.Error(Resource.Error.mapErrorCode(e.code())))
        }
    }

    override fun updateSquad(
        squadId: String,
        squad: CreateSquadDto
    ): Flow<Resource<Void>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.updateSquad(squadId, squad).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "updateSquad: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } catch (e: HttpException) {
            Log.e("SquadsRepositoryImpl", "updateSquad: HttpException ${e.message}")
            emit(Resource.Error(Resource.Error.mapErrorCode(e.code())))
        }
    }

    override fun updatePrivacy(
        squadId: String,
        privacy: Int
    ): Flow<Resource<Void>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.updatePrivacy(squadId, privacy).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "updatePrivacy: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } catch (e: HttpException) {
            Log.e("SquadsRepositoryImpl", "updatePrivacy: HttpException ${e.message}")
            emit(Resource.Error(Resource.Error.mapErrorCode(e.code())))
        }
    }

    override fun getPosts(
        squadId: String,
        lastId: Long?,
        length: Int
    ): Flow<Resource<FeedDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = squadsApi.getPosts(squadId, lastId ?: -1, length).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IOException) {
            Log.e("SquadsRepositoryImpl", "getPosts: IOException ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } catch (e: HttpException) {
            Log.e("SquadsRepositoryImpl", "getPosts: HttpException ${e.message}")
            emit(Resource.Error(Resource.Error.mapErrorCode(e.code())))
        }
    }


}