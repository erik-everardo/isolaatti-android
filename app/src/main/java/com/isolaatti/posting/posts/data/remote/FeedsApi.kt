package com.isolaatti.posting.posts.data.remote

import com.isolaatti.common.ResultDto
import com.isolaatti.profile.data.remote.ProfileListItemDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedsApi {
    @GET("Fetch/PostsOfUser/{userId}")
    fun postsOfUser(@Path("userId") userId: Int,
                    @Query("length") length: Int,
                    @Query("lastId") lastId: Long,
                    @Query("olderFirst") olderFirst: Boolean,
                    @Query(value = "filterJson", encoded = false) filter: String): Call<FeedDto>

    @GET("Fetch/Post/{postId}")
    fun getPost(@Path("postId") postId: Long): Call<FeedDto>

    @GET("Fetch/Post/{postId}/LikedBy")
    fun getLikedBy(@Path("postId") postId: Long): Call<List<ProfileListItemDto>>

    @GET("Feed")
    fun getChronology(@Query("lastId") lastId: Long, @Query("length") length: Int): Call<FeedDto>

    @GET("hashtags/hashtag/{hashtag}")
    fun getHashtagPosts(@Path("hashtag") hashtag: String, @Query("after") afterPost: Long? = null): Call<FeedDto>
}