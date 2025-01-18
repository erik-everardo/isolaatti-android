package com.isolaatti.posting.posts.data.repository

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.isolaatti.images.common.data.remote.ImageDto
import com.isolaatti.images.common.data.remote.ImagesApi
import com.isolaatti.posting.posts.data.remote.CreatePostDto
import com.isolaatti.posting.posts.data.remote.DeletePostDto
import com.isolaatti.posting.posts.data.remote.EditPostDto
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.posting.posts.data.remote.FeedFilterDto
import com.isolaatti.posting.posts.data.remote.FeedsApi
import com.isolaatti.posting.posts.data.remote.PostApi
import com.isolaatti.posting.posts.data.remote.PostDeletedDto
import com.isolaatti.posting.posts.data.remote.VersionDto
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

class PostsRepositoryImpl @Inject constructor(
    private val feedsApi: FeedsApi,
    private val postApi: PostApi,
    private val imagesApi: ImagesApi,
    private val contentResolver: ContentResolver
) : PostsRepository {
    companion object {
        const val LOG_TAG = "PostsRepositoryImpl"
    }
    override fun getFeed(lastId: Long, hashtag: String?): Flow<Resource<MutableList<Post>>> = flow {
        emit(Resource.Loading())
        try {

            val result = if(hashtag == null) {
                feedsApi.getChronology(lastId, 20).awaitResponse()
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
            val response = feedsApi.postsOfUser(userId, 20, lastId, olderFirst, gson.toJson(filter)).awaitResponse()
            if(response.isSuccessful) {
                emit(Resource.Success(response.body()?.let { Post.fromFeedDto(it) }))
                return@flow
            }
            emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun makePost(createPostDto: CreatePostDto, images: List<Uri>): Flow<Resource<PostingSteps>> = flow {
        emit(Resource.Loading())
        try {
            val hasImages = images.isNotEmpty()
            val result = postApi.makePost(
                if(hasImages) {
                    createPostDto.copy(isDraft = true)
                } else {
                    createPostDto
                }
            ).awaitResponse()
            if(result.isSuccessful) {

                if(hasImages) {
                    val successfulImageUploads: MutableList<ImageDto> = mutableListOf()

                    val postId = result.body()?.post?.id
                    if(postId == null) {
                        emit(Resource.Error())
                        return@flow
                    }
                    emit(Resource.Success(PostingSteps.UploadingPhotos))
                    images.forEach { imageUri ->
                        var imageInputStream: InputStream? = null
                        try {
                            imageInputStream = contentResolver.openInputStream(imageUri)
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

                    // undo post and emit error
                    if(successfulImageUploads.size != images.size) {
                        postApi.deletePost(DeletePostDto(postId)).awaitResponse()
                        emit(Resource.Error(Resource.Error.ErrorType.ServerError, "Some images were not processed correctly"))
                    } else {
                        val setIsDraftCall = postApi.setIsDraft(postId, false).awaitResponse()

                        if(setIsDraftCall.isSuccessful) {
                            emit(Resource.Success(PostingSteps.Finished))
                        } else {
                            val code = setIsDraftCall.code()
                            Log.e(LOG_TAG, "Could not set as \"not draft\"")
                            emit(Resource.Error(Resource.Error.mapErrorCode(code), "Could not set as \"not draft\""))
                        }

                    }
                } else {
                    emit(Resource.Success(PostingSteps.Finished))
                    return@flow
                }

            }
            emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
        } catch(e: Exception) {
            Log.d(LOG_TAG, e.toString())
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun editPost(editPostDto: EditPostDto): Flow<Resource<FeedDto.PostDto>> = flow {
        emit(Resource.Loading())
        try {
            val result = postApi.editPost(editPostDto).awaitResponse()
            if(result.isSuccessful) {
                emit(Resource.Success(result.body()))
                return@flow
            }
            emit(Resource.Error(Resource.Error.mapErrorCode(result.code())))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
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