package com.isolaatti.profile.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApi {

    @GET("Fetch/UserProfile/{userId}")
    fun userProfile(@Path("userId") userId: Int): Call<UserProfileDto>

    @POST("EditProfile/SetProfilePhoto")
    fun setProfileImage(@Query("imageId") imageId: String): Call<Void>

    @POST("EditProfile/UpdateProfile")
    fun updateProfile(@Body updateProfileDto: UpdateProfileDto): Call<UserProfileDto>

    @POST("EditProfile/RemoveProfileImage")
    fun removeCurrentProfilePicture(): Call<Void>

}