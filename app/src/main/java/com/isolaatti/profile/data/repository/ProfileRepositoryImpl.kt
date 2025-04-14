package com.isolaatti.profile.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toFile
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.auth.data.local.UserInfoDao
import com.isolaatti.auth.data.local.UserInfoEntity
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.profile.data.remote.ProfileApi
import com.isolaatti.profile.data.remote.UpdateProfileDto
import com.isolaatti.profile.domain.ProfileRepository
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val userInfoDao: UserInfoDao,
    private val imagesApi: ImagesApi,
    private val contentResolver: ContentResolver
) : ProfileRepository {
    override fun getProfile(userId: Int): Flow<Resource<UserProfile>> = flow {
        try {
            val result = profileApi.userProfile(userId).awaitResponse()
            if(result.isSuccessful) {
                val dto = result.body()
                if(dto != null) {
                    userInfoDao.setUserInfo(UserInfoEntity(userId, dto.uniqueUsername, dto.name))

                    emit(Resource.Success(UserProfile.fromDto(dto)))
                }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
            }

        } catch(e: Exception) {
            Log.e("ProfileRepositoryImpl", e.message.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun setProfileImage(image: LocalImage): Flow<Resource<RemoteImage>> = flow {
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
                        file = MultipartBody.Part.createFormData("file", "profile-${System.currentTimeMillis()}",outputStream.toByteArray().toRequestBody()),
                        setAsProfile = true
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

    override fun updateProfile(newDisplayName: String, newDescription: String): Flow<Resource<Boolean>> = flow {
        try {
            val result = profileApi.updateProfile(UpdateProfileDto(newDescription, newDisplayName)).awaitResponse()

            if(result.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", e.message.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun removeProfileImage(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        try {
            val response = profileApi.removeCurrentProfilePicture().awaitResponse()

            if(response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepositoryImpl", e.message.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun setProfileAudio(audioFile: Uri, duration: Long): Flow<Resource<Audio>> = flow {
        emit(Resource.Loading())

        try {
            val file = audioFile.toFile()
            val response = profileApi.updateAudioDescription(
                file = MultipartBody.Part.createFormData("audioFile", "profile-audio", file.asRequestBody()),
                duration = MultipartBody.Part.createFormData("duration", duration.toString())
            ).awaitResponse()

            if(response.isSuccessful) {
                emit(Resource.Success(Audio.fromDto(response.body()!!)))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }



        } catch(e: IOException) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError))
        }
    }
}