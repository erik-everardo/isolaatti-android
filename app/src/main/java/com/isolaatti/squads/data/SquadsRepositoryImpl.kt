package com.isolaatti.squads.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.squads.domain.SquadsRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.awaitResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class SquadsRepositoryImpl(
    private val squadsApi: SquadsApi,
    private val imagesApi: ImagesApi,
    private val contentResolver: ContentResolver
) : SquadsRepository {
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

    override fun updateImage(
        squadId: String,
        image: LocalImage,

    ): Flow<Resource<RemoteImage>> = flow {
        emit(Resource.Loading())

        try {
            var imageInputStream: InputStream? = null

            contentResolver.openInputStream(image.uri).use {
                imageInputStream = it

                val bitmap = BitmapFactory.decodeStream(imageInputStream)

                val outputStream = ByteArrayOutputStream()

                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, outputStream)
                }


                val result = imagesApi
                    .postImage(
                        file = MultipartBody.Part.createFormData("file", "squad-${squadId}-${System.currentTimeMillis()}",outputStream.toByteArray().toRequestBody()),
                        setAsProfile = true,
                        squadId = MultipartBody.Part.createFormData("squadId", squadId)
                    )
                    .awaitResponse()

                if(result.isSuccessful) {
                    val imageDto = result.body()
                    emit(Resource.Success(RemoteImage(imageDto!!.id)))
                } else {
                    emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
                }
            }


        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", e.message.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }

    }


}