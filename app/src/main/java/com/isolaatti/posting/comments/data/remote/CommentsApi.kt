package com.isolaatti.posting.comments.data.remote

import com.isolaatti.utils.LongIdentificationWrapper
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentsApi {
    @POST("Posting/Post/{postId}/Comment")
    fun postComment(@Path("postId") postId: Long, @Body commentToPost: CommentToPostDto): Call<CommentDto>

    @GET("Fetch/Post/{postId}/Comments")
    fun getCommentsOfPosts(@Path("postId") postId: Long, @Query("lastId") lastId: Long?, @Query("take") count: Int): Call<FeedCommentsDto>

    @GET("Fetch/Comments/{commentId}")
    fun getComment(@Path("commentId") commentId: Long): Call<CommentDto>

    @PUT("Comments/{commentId}")
    fun editComment(@Path("commentId") commentId: Long, @Body commentDto: CommentToPostDto): Call<CommentDto>

    @DELETE("Comments/{commentId}")
    fun deleteComment(@Path("commentId") commentId: Long): Call<Void>

    @POST("Comments/{commentId}/reply")
    fun postReply(@Path("commentId") commentId: Long, @Body commentToPost: CommentToPostDto): Call<CommentDto>

    @GET("Comments/{commentId}/replies")
    fun getReplies(@Path("commentId") commentId: Long, @Query("lastId") lastId: Long?, @Query("take") count: Int): Call<FeedCommentsDto>
}