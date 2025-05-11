package com.isolaatti.posting.posts.data.remote

import com.isolaatti.common.ResultDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi  {
    @POST("Posting/Make")
    fun makePost(@Body post: CreatePostDto): Call<FeedDto.PostDto>

    @POST("Posting/Edit")
    fun editPost(@Body editedPost: EditPostDto): Call<FeedDto.PostDto>

    @POST("Posting/Delete")
    fun deletePost(@Body postToDelete: DeletePostDto): Call<PostDeletedDto>

    @GET("Fetch/Post/{postId}")
    fun getPost(@Path("postId") postId: Long): Call<FeedDto.PostDto>

    @GET("Posting/Post/{postId}/Versions")
    fun getVersions(@Path("postId") postId: Long): Call<ResultDto<List<VersionDto>>>

    @POST("Posting/Post/{postId}/SetIsDraft")
    fun setIsDraft(@Path("postId") postId: Long, @Query("isDraft") isDraft: Boolean): Call<Void>

}