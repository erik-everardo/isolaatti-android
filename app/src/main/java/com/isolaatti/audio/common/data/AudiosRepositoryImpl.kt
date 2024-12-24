package com.isolaatti.audio.common.data

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.AudiosRepository
import com.isolaatti.settings.domain.UserIdSetting
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.awaitResponse
import javax.inject.Inject

class AudiosRepositoryImpl @Inject constructor(
    private val audiosApi: AudiosApi,
    private val userIdSetting: UserIdSetting
) : AudiosRepository {
    companion object {
        const val LOG_TAG = "AudiosRepositoryImpl"
    }

    private suspend fun _getAudiosOfUser(userId: Int, lastId: String?): Resource<List<Audio>> {
        try {
            val response = audiosApi.getAudiosOfUser(userId, lastId).awaitResponse()

            if(response.isSuccessful) {
                val body = response.body() ?: return Resource.Error(Resource.Error.ErrorType.ServerError)

                return Resource.Success(body.data.map { Audio.fromDto(it) })
            } else {
                return Resource.Error(Resource.Error.ErrorType.ServerError)
            }
        } catch(e: Exception) {
            Log.e("AudiosRepositoryImpl", e.message.toString())
            return Resource.Error(Resource.Error.ErrorType.NetworkError)
        }
    }
    override fun getAudiosOfUser(userId: Int, lastId: String?): Flow<Resource<List<Audio>>> = flow {
        emit(Resource.Loading())
        emit(_getAudiosOfUser(userId, lastId))
    }

    override fun getMyAudios(lastId: String?): Flow<Resource<List<Audio>>> = flow {
        emit(Resource.Loading())
        val userId = userIdSetting.getUserIdAsync()
        if(userId == null) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError))
            return@flow
        }

        emit(_getAudiosOfUser(userId, lastId))
    }

    override fun uploadAudio(uri: Uri, postId: Long): Flow<Resource<Audio>> = flow {


        val file = uri.toFile()

        if(!file.exists()) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError, "File not found"))
            return@flow
        }

        try {
            val response = audiosApi.uploadFile(
                MultipartBody.Part.createFormData(
                    AudiosApi.AudioParam, // api parameter name
                    uri.lastPathSegment,
                    file.asRequestBody("audio/aac".toMediaType()) // actual file to be sent
                ),
                MultipartBody.Part.createFormData(AudiosApi.DurationParam, "0")
            ).awaitResponse()

            if(response.isSuccessful) {
                val audioDto = response.body()
                if(audioDto != null) {
                    Log.d(LOG_TAG, "emit audio dto")
                    emit(Resource.Success(Audio.fromDto(audioDto)))
                }

            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(e: Exception) {
            Log.d(LOG_TAG, e.message.toString())
        }
    }

    override fun deleteAudio(audioId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = audiosApi.deleteAudio(audioId).awaitResponse()

            if(response.isSuccessful) {
                emit(Resource.Success(response.isSuccessful))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(e: Exception) {
            Log.e(LOG_TAG, e.message.toString())
        }
    }
}