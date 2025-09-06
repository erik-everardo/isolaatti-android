package com.isolaatti.squads.data

import com.isolaatti.posting.posts.data.remote.FeedDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SquadsApi {
    @POST("Squads/Create")
    fun createSquad(@Body squad: CreateSquadDto): Call<CreateSquadResponseDto>

    @GET("Squads")
    fun getMySquads(@Query("lastId") lastId: String? = null): Call<SquadsDto>

    @GET("Squads/{squadId}")
    fun getSquad(@Path("squadId") squadId: String): Call<SquadWrapperDto>

    @DELETE("Squads/{squadId}")
    fun deleteSquad(squadId: String): Call<Void>

    @POST("Squads/{squadId}/Update")
    fun updateSquad(squadId: String, squad: CreateSquadDto): Call<Void>

    @POST("Squads/{squadId}/UpdatePrivacyTo/{privacy}")
    fun updatePrivacy(@Path("squadId") squadId: String, @Path("privacy") privacy: Int): Call<Void>

    @GET("Squads/{squadId}/Posting/Posts")
    fun getPosts(@Path("squadId") squadId: String, @Query("lastId") lastId: Long? = null, @Query("length") length: Int = 10): Call<FeedDto>

}