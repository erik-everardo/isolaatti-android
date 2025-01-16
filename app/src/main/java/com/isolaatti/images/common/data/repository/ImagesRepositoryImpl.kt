package com.isolaatti.images.common.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.isolaatti.images.common.data.dao.ImagesDraftsDao
import com.isolaatti.images.common.data.entity.ImageDraftEntity
import com.isolaatti.images.common.data.remote.DeleteImagesDto
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.common.domain.repository.ImagesRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(private val imagesApi: ImagesApi, private val imagesDraftsDao: ImagesDraftsDao, private val contentResolver: ContentResolver) :
    ImagesRepository {

        companion object {
            const val TAG = "ImagesRepositoryImpl"
        }
    override fun getImagesOfUser(userId: Int, lastId: String?): Flow<Resource<List<Image>>> = flow {
        emit(Resource.Loading())
        try {
            val response = imagesApi.getImagesOfUser(userId, lastId).awaitResponse()
            if(response.isSuccessful) {
                val imagesDto = response.body()
                val images = imagesDto?.data?.map { Image.fromDto(it) }

                emit(Resource.Success(images))

            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun deleteImages(images: List<Image>): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val dto = DeleteImagesDto(images.map { it.id })
        try {
            val response = imagesApi.deleteImages(dto).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun uploadImage(imageUri: Uri, postId: Long, squadId: String?): Flow<Resource<Image>> = flow {
        emit(Resource.Loading())
        var imageInputStream: InputStream? = null
        try {
            imageInputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(imageInputStream)

            if(bitmap == null) {
                emit(Resource.Error(Resource.Error.ErrorType.InputError))
                return@flow
            }

            val outputStream = ByteArrayOutputStream()

            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, outputStream)
            }


            val response = imagesApi.postImage(
                MultipartBody.Part.createFormData("file", "$postId-${System.currentTimeMillis()}",outputStream.toByteArray().toRequestBody()),
                MultipartBody.Part.createFormData("postId", postId.toString())
            ).awaitResponse()
            if(response.isSuccessful) {
                val imageDto = response.body()
                if(imageDto == null) {
                    emit(Resource.Error(Resource.Error.ErrorType.ServerError))
                    return@flow
                }
                val image = Image.fromDto(imageDto)
                emit(Resource.Success(image))
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }

        } catch(e: Exception) {
            Log.e("ImagesRepository", e.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        } finally {
            imageInputStream?.close()
        }
    }
}