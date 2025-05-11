package com.isolaatti.audio.common.data

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AudiosApi {
    companion object {
        const val AudioParam= "audioFile"
        const val DurationParam = "duration"
        const val PostIdParam = "postId"
    }

    @POST("/api/Audios/Create")
    @Multipart
    fun uploadFile(@Part file: MultipartBody.Part, @Part postId: MultipartBody.Part, @Part duration: MultipartBody.Part): Call<Void>
}