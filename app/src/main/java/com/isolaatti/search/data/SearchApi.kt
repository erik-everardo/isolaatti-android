package com.isolaatti.search.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {
    @GET("Search/Quick")
    fun quickSearch(@Query("q") query: String): Call<SearchDto>

    @GET("hashtags/trending")
    fun getTrendingHashtags(): Call<HashtagsDto>

    @GET("Search/newestUsers")
    fun getNewestUsers(@Query("limit") limit: Int, @Query("after") after: Int?): Call<NewestUsersDto>
}