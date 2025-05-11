package com.isolaatti.profile.data.remote

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProfileApi {

    @GET("Fetch/UserProfile/{userId}")
    fun userProfile(@Path("userId") userId: Int): Call<UserProfileDto>


    @POST("EditProfile/UpdateProfile")
    fun updateProfile(@Body updateProfileDto: UpdateProfileDto): Call<UserProfileDto>

    @POST("EditProfile/RemoveProfileImage")
    fun removeCurrentProfilePicture(): Call<Void>

    @DELETE("EditProfile/Audio")
    fun deleteAudioDescription(): Call<Void>

    @Multipart
    @POST("EditProfile/SetAudioDescription")
    fun updateAudioDescription(@Part file: MultipartBody.Part, @Part duration: MultipartBody.Part): Call<Void>

}