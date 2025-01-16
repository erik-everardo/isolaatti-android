package com.isolaatti.images.common.data.remote

import com.isolaatti.utils.SimpleData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ImagesApi {
    @GET("images/of_user/{userId}")
    fun getImagesOfUser(@Path("userId") userId: Int,
                        @Query("lastId") lastId: String?): Call<ImagesDto>

    @POST("images/create")
    @Multipart
    fun postImage(@Part file: MultipartBody.Part,
                  @Part postId: MultipartBody.Part,
                  @Part setAsProfile: MultipartBody.Part? = null,
                  @Part squadId: MultipartBody.Part? = null): Call<ImageDto>

    @POST("images/delete/delete_many")
    fun deleteImages(@Body deleteImagesDto: DeleteImagesDto): Call<Void>

    @GET("images/of_squad/{squadId}")
    fun getImagesOfSquad(@Path("squadId") squadId: String,
                         @Query("lastId") lastId: String?): Call<List<ImageDto>>

    @POST("/images/{imageId}/rename")
    fun renameImage(@Path("imageId") imageId: String, @Body newName: SimpleData<String>)
}