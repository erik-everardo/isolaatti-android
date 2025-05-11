package com.isolaatti.posting.posts.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toFile
import com.google.gson.Gson
import com.isolaatti.audio.common.data.AudiosApi
import com.isolaatti.images.common.data.remote.DeleteImagesDto
import com.isolaatti.images.common.data.remote.ImageDto
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.CreatePostDto
import com.isolaatti.posting.posts.data.remote.DeletePostDto
import com.isolaatti.posting.posts.data.remote.EditPostDto
import com.isolaatti.posting.posts.data.remote.FeedFilterDto
import com.isolaatti.posting.posts.data.remote.FeedsApi
import com.isolaatti.posting.posts.data.remote.PostApi
import com.isolaatti.posting.posts.data.remote.PostDeletedDto
import com.isolaatti.posting.posts.data.remote.VersionDto
import com.isolaatti.posting.posts.domain.FeedPageSize
import com.isolaatti.posting.posts.domain.PostPrivacy
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import com.isolaatti.utils.ResourceWithSteps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

class PostsRepositoryImpl @Inject constructor(
    private val feedsApi: FeedsApi,
    private val postApi: PostApi,
    private val imagesApi: ImagesApi,
    private val audiosApi: AudiosApi,
    private val contentResolver: ContentResolver
) : PostsRepository {
    companion object {
        const val LOG_TAG = "PostsRepositoryImpl"
    }
    override fun getFeed(lastId: Long, hashtag: String?): Flow<Resource<MutableList<Post>>> = flow {
        emit(Resource.Loading())
        try {

            val result = if(hashtag == null) {
                feedsApi.getChronology(lastId, FeedPageSize).awaitResponse()
            } else {
                feedsApi.getHashtagPosts(hashtag, lastId).awaitResponse()
            }
            if(result.isSuccessful) {
                emit(Resource.Success(result.body()?.let { Post.fromFeedDto(it) }))
                return@flow
            }
            when(result.code()) {
                401 -> emit(Resource.Error(Resource.Error.ErrorType.AuthError))
                404 -> emit(Resource.Error(Resource.Error.ErrorType.NotFoundError))
                500 -> emit(Resource.Error(Resource.Error.ErrorType.ServerError))
                else -> emit(Resource.Error(Resource.Error.ErrorType.OtherError))
            }
        } catch(e: Exception) {
            Log.e(LOG_TAG, "Error $e")
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun getProfilePosts(userId: Int, lastId: Long, olderFirst: Boolean, filter: FeedFilterDto?): Flow<Resource<MutableList<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val gson = Gson()
            val response = feedsApi.postsOfUser(userId, FeedPageSize, lastId, olderFirst, gson.toJson(filter)).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()?.let { Post.fromFeedDto(it) }))
                return@flow
            }
            emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    /**
     * @param images list of images to upload
     * @return list of [ImageDto], which were uploaded correctly
     */
    private suspend fun uploadImages(postId: Long, images: List<LocalImage>): List<ImageDto> {
        val successfulImageUploads: MutableList<ImageDto> = mutableListOf()

        images.forEach { localImage ->
            var imageInputStream: InputStream? = null
            try {
                imageInputStream = contentResolver.openInputStream(localImage.uri)
                val bitmap = BitmapFactory.decodeStream(imageInputStream)

                if(bitmap == null) {
                    Log.e(LOG_TAG, "Error resolving image with content resolver")
                    return@forEach
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
                    response.body()?.let { successfulImageUploads.add(it) }

                }

            } catch(e: Exception) {
                Log.e("ImagesRepository", e.toString())

            } finally {
                imageInputStream?.close()
            }
        }

        return successfulImageUploads
    }

    override fun makePost(
        privacy: PostPrivacy,
        text: String,
        squadId: String?,
        images: List<LocalImage>,
        audio: Uri?
    ): Flow<ResourceWithSteps<Post, PostingSteps>> = flow<ResourceWithSteps<Post, PostingSteps>> {
        emit(ResourceWithSteps.Loading())
        val hasImages = images.isNotEmpty()
        val hasAudio = audio != null

        var imagesError = false
        var audioError = false

        val result = postApi.makePost(
            CreatePostDto(
                privacy = privacy.toInt(),
                content = text,
                squadId = squadId,
                isDraft = hasImages || hasAudio
            )
        ).awaitResponse()
        if(result.isSuccessful) {

            val postDto = result.body()

            var successfulImageUploads: List<ImageDto>? = null
            if(hasImages) {

                val postId = postDto?.post?.id
                if(postId == null) {
                    emit(ResourceWithSteps.Error())
                    return@flow
                }
                emit(ResourceWithSteps.Success(null, PostingSteps.UploadingPhotos))
                successfulImageUploads = uploadImages(postId, images)

                imagesError = successfulImageUploads.size != images.size
            }

            if(hasAudio) {
                emit(ResourceWithSteps.Success(null, PostingSteps.UploadingAudio))
                val file = audio.toFile()
                val postId = result.body()?.post?.id
                if(postId == null) {
                    audioError = true
                } else {
                    val response = audiosApi.uploadFile(
                        MultipartBody.Part.createFormData(
                            AudiosApi.AudioParam, // api parameter name
                            audio.lastPathSegment,
                            file.asRequestBody("audio/aac".toMediaType()) // actual file to be sent
                        ),
                        MultipartBody.Part.createFormData(AudiosApi.PostIdParam, postId.toString()),
                        MultipartBody.Part.createFormData(AudiosApi.DurationParam, "0")
                    ).awaitResponse()
                    audioError = !response.isSuccessful
                }

            }

            if((hasAudio && audioError) || (hasImages && imagesError)) {
                val postId = postDto?.post?.id
                if(postId != null) {
                    postApi.deletePost(DeletePostDto(postId)).awaitResponse()
                    emit(ResourceWithSteps.Error(ResourceWithSteps.Error.ErrorType.ServerError, "Some images were not processed correctly"))
                }
                return@flow
            }

            val postId = result.body()?.post?.id

            if(postId == null) {
                emit(ResourceWithSteps.Error())
                return@flow
            }

            val setIsDraftCall = postApi.setIsDraft(postId, false).awaitResponse()

            if(setIsDraftCall.isSuccessful) {
                emit(ResourceWithSteps.Success(null, PostingSteps.Finished))
            } else {
                val code = setIsDraftCall.code()
                Log.e(LOG_TAG, "Could not set as \"not draft\"")
                emit(ResourceWithSteps.Error(ResourceWithSteps.Error.mapErrorCode(code), "Could not set as \"not draft\""))
            }

            val completePost = postDto?.let {
                Post(
                    id = postId,
                    textContent = it.post.textContent,
                    userId = it.post.userId,
                    privacy = privacy,
                    date = it.post.date,
                    squadId = it.post.squadId,
                    numberOfLikes = it.numberOfLikes,
                    numberOfComments = it.numberOfComments,
                    userName = it.userName,
                    liked = it.liked,
                    images = successfulImageUploads?.map { RemoteImage(it.id) } ?: emptyList()
                )
            }

            emit(ResourceWithSteps.Success(completePost, PostingSteps.Finished))
            return@flow
        }
        emit(ResourceWithSteps.Error(ResourceWithSteps.Error.mapErrorCode(result.code())))
    }.catch { e ->
        Log.d(LOG_TAG, e.toString())
        emit(ResourceWithSteps.Error(ResourceWithSteps.Error.ErrorType.NetworkError))
    }

    override fun editPost(
        postId: Long,
        privacy: PostPrivacy,
        text: String,
        squadId: String?,
        images: List<LocalImage>,
        imagesToRemove: List<RemoteImage>,
        audio: Uri?,
        removeOldAudio: Boolean
    ): Flow<ResourceWithSteps<Post, PostingSteps>> = flow<ResourceWithSteps<Post, PostingSteps>>{
        emit(ResourceWithSteps.Loading())

        // It is necessary to set this as draft, new media will be uploaded
        if(images.isNotEmpty() || audio != null) {
            val setIsDraftResponse = postApi.setIsDraft(postId, true).awaitResponse()
            if(!setIsDraftResponse.isSuccessful) {
                emit(ResourceWithSteps.Error(ResourceWithSteps.Error.mapErrorCode(setIsDraftResponse.code()), "Failed to set as draft post $postId"))
                return@flow
            }
        }

        // Remove images user removed
        if(imagesToRemove.isNotEmpty()) {
            imagesApi.deleteImages(DeleteImagesDto(imagesToRemove.map { it.id })).awaitResponse()
        }

        // If there is a non-null "audio" Uri, it is uploaded and the server will
        // automatically replace it, deleting old audio, otherwise the intention to only delete the audio
        // is indicated with "willDeleteAudio" set to true, which is a flag to "edit post" request.
        var willDeleteAudio = removeOldAudio
        if(audio != null) {
            willDeleteAudio = false
            val file = audio.toFile()
            val uploadAudioResponse = audiosApi.uploadFile(
                file = MultipartBody.Part.createFormData(AudiosApi.AudioParam, audio.lastPathSegment, file.asRequestBody("audio/aac".toMediaType()) ),
                postId = MultipartBody.Part.createFormData(AudiosApi.PostIdParam, postId.toString()),
                duration = MultipartBody.Part.createFormData(AudiosApi.DurationParam, "0"),
            ).awaitResponse()

            if(!uploadAudioResponse.isSuccessful) {
                emit(ResourceWithSteps.Error(ResourceWithSteps.Error.mapErrorCode(uploadAudioResponse.code()), "Failed to upload audio postId $postId"))
                return@flow
            }
        }

        uploadImages(postId, images)

        val editPostResponse = postApi.editPost(
            EditPostDto(
                privacy = privacy.toInt(),
                content = text,
                squadId = squadId,
                postId = postId,
                removeAudio = willDeleteAudio,
                isDraft = false // isDraft set to false at the end in case this post is a draft
            )
        ).awaitResponse()


        if(editPostResponse.isSuccessful) {
            val editedPostDto = editPostResponse.body()

            if(editedPostDto != null) {
                val post = Post.fromPostDto(editedPostDto)

                emit(ResourceWithSteps.Success(post, PostingSteps.Finished))

                return@flow
            }
            emit(ResourceWithSteps.Error(ResourceWithSteps.Error.ErrorType.OtherError))
            return@flow
        }

        emit(ResourceWithSteps.Error(ResourceWithSteps.Error.mapErrorCode(editPostResponse.code())))

    }.catch { e ->
        Log.d(LOG_TAG, e.toString())
        emit(ResourceWithSteps.Error(ResourceWithSteps.Error.ErrorType.NetworkError))
    }

    override fun deletePost(postId: Long): Flow<Resource<PostDeletedDto>> = flow {
        emit(Resource.Loading())
        try {
            val result = postApi.deletePost(DeletePostDto(postId)).execute()
            if(result.isSuccessful) {
                emit(Resource.Success(result.body()))
                return@flow
            }
            emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun loadPost(postId: Long): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val result = postApi.getPost(postId).execute()
            if(result.isSuccessful) {
                val dto = result.body()
                if(dto != null) {
                    emit(Resource.Success(Post.fromPostDto(dto)))
                    return@flow
                }
            }
            emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun getUsersLikedPost(postId: Long): Flow<Resource<List<ProfileListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val response = feedsApi.getLikedBy(postId).awaitResponse()
            if(response.isSuccessful) {
                val dto = response.body()
                if(dto != null) {
                    emit(Resource.Success(dto.result.map { ProfileListItem.fromDto(it) }))
                    return@flow
                }
            }
            emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError))
        }
    }

    override fun getPostVersions(postId: Long): Flow<Resource<List<VersionDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = postApi.getVersions(postId).awaitResponse()
            if(response.isSuccessful) {
                val dto = response.body()
                if(dto != null) {
                    emit(Resource.Success(dto.result))
                }
                return@flow
            }

            emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
        } catch(e: Exception) {
            Log.e(LOG_TAG, "Error getting post versions. postId: $postId. \n ${e.message}")
            emit(Resource.Error(Resource.Error.ErrorType.OtherError))
        }
    }
}