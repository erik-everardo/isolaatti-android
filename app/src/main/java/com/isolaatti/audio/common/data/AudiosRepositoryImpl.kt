package com.isolaatti.audio.common.data

import android.util.Log
import com.isolaatti.MyApplication
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.AudiosRepository
import com.isolaatti.audio.drafts.data.AudiosDraftsDao
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.awaitResponse
import java.io.File
import javax.inject.Inject

class AudiosRepositoryImpl @Inject constructor(private val audiosApi: AudiosApi, private val audiosDraftsDao: AudiosDraftsDao) : AudiosRepository {
    companion object {
        const val LOG_TAG = "AudiosRepositoryImpl"
    }
    override fun getAudiosOfUser(userId: Int, lastId: String?): Flow<Resource<List<Audio>>> = flow {
        emit(Resource.Loading())
        try {
            val response = audiosApi.getAudiosOfUser(userId, lastId).awaitResponse()

            if(response.isSuccessful) {
                val body = response.body()
                if(body == null) {
                    emit(Resource.Error(Resource.Error.ErrorType.ServerError))
                    return@flow
                }

                emit(Resource.Success(body.data.map { Audio.fromDto(it) }))
            }
        } catch(e: Exception) {
            Log.e("AudiosRepositoryImpl", e.message.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun uploadAudio(draftId: Long): Flow<Resource<Audio>> = flow {
        val audioDraftEntity = audiosDraftsDao.getAudioDraftById(draftId)
        if(audioDraftEntity == null) {
            emit(Resource.Error(Resource.Error.ErrorType.NotFoundError, "draft not found"))
            return@flow
        }

        val file = File(MyApplication.myApp.filesDir, audioDraftEntity.audioLocalPath)

        if(!file.exists()) {
            // remove draft, file was removed from file system for some reason
            audiosDraftsDao.deleteDrafts(arrayOf(audioDraftEntity))
            emit(Resource.Error(Resource.Error.ErrorType.OtherError, "File not found"))
            return@flow
        }

        try {
            val response = audiosApi.uploadFile(
                MultipartBody.Part.createFormData(
                    AudiosApi.AudioParam, // api parameter name
                    audioDraftEntity.audioLocalPath.split("/")[1],
                    file.asRequestBody("audio/3gpp".toMediaType()) // actual file to be sent
                ),
                MultipartBody.Part.createFormData(AudiosApi.NameParam, audioDraftEntity.name),
                MultipartBody.Part.createFormData(AudiosApi.DurationParam, "0")
            ).awaitResponse()

            if(response.isSuccessful) {
                val audioDto = response.body()
                if(audioDto != null) {
                    Log.d(LOG_TAG, "emit audio dto")
                    audiosDraftsDao.deleteDrafts(arrayOf(audioDraftEntity))
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