package com.isolaatti.audio.common.data

import com.isolaatti.common.ResultDto
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AudiosApi {
    companion object {
        const val AudioParam= "audioFile"
        const val DurationParam = "duration"
    }
    @GET("/api/Audios/OfUser/{userId}")
    fun getAudiosOfUser(@Path("userId") userId: Int, @Query("lastAudioId") lastAudioId: String?): Call<AudiosDto>

    @POST("/api/Audios/Create")
    @Multipart
    fun uploadFile(@Part file: MultipartBody.Part, @Part duration: MultipartBody.Part): Call<AudioDto>

    @POST("/api/Audios/{audioId}/Delete")
    fun deleteAudio(@Path("audioId") audioId: String): Call<ResultDto<String>>
}