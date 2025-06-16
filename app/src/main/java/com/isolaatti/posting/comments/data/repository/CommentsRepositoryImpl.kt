package com.isolaatti.posting.comments.data.repository

import com.isolaatti.posting.comments.data.remote.CommentDto
import com.isolaatti.posting.comments.data.remote.CommentToPostDto
import com.isolaatti.posting.comments.data.remote.CommentsApi
import com.isolaatti.posting.comments.domain.CommentsRepository
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.utils.LongIdentificationWrapper
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject

class CommentsRepositoryImpl @Inject constructor(private val commentsApi: CommentsApi) :
    CommentsRepository {
    override fun getComments(postId: Long, lastId: Long?): Flow<Resource<MutableList<Comment>>> = flow {
        try {
            emit(Resource.Loading())
            val response = commentsApi.getCommentsOfPosts(postId, lastId, 15).awaitResponse()
            if(response.isSuccessful){
                response.body()?.let { emit(Resource.Success(Comment.fromCommentsDto(it).toMutableList())) }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun getComment(commentId: Long): Flow<Resource<CommentDto>> = flow {
        try {
            emit(Resource.Loading())
            val response = commentsApi.getComment(commentId).awaitResponse()
            if(response.isSuccessful) {
                response.body()?.let { emit(Resource.Success(it)) }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun postComment(content: String, postId: Long): Flow<Resource<Comment>> = flow {
        try {
            emit(Resource.Loading())
            val commentToPostDto = CommentToPostDto(content)
            val response = commentsApi.postComment(postId, commentToPostDto).awaitResponse()
            if(response.isSuccessful) {
                val responseBody = response.body()
                if(responseBody != null) {
                    emit(Resource.Success(Comment.fromCommentDto(responseBody)))
                    return@flow
                }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }

        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun editComment(commentId: Long, content: String, audioId: String?): Flow<Resource<Comment>> = flow {
        try {
            emit(Resource.Loading())
            val commentToPostDto = CommentToPostDto(content)
            val response = commentsApi.editComment(commentId, commentToPostDto).awaitResponse()

            if(response.isSuccessful) {
                val responseBody = response.body()
                if(responseBody != null) {
                    emit(Resource.Success(Comment.fromCommentDto(responseBody)))
                    return@flow
                }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }

    }

    override fun deleteComment(commentId: Long): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val response = commentsApi.deleteComment(LongIdentificationWrapper(commentId)).awaitResponse()

        if(response.isSuccessful) {
            emit(Resource.Success(true))
            return@flow
        }
        emit(Resource.Error())
    }
}