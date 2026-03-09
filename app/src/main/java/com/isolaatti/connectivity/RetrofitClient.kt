package com.isolaatti.connectivity

import com.google.gson.GsonBuilder
import com.isolaatti.BuildConfig
import com.isolaatti.type_adapters.LocalDateTimeAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RetrofitClient(private val okHttpClient: OkHttpClient) {

    companion object {
        // These urls don't need auth header
        val excludedUrlsFromAuthentication = listOf(
            "/api/LogIn",
            "/api/signUp/get_code",
            "/api/signUp/validate_code",
            "/api/signUp/sign_up_with_code"
        )
        const val BASE_URL = "${BuildConfig.backend}/api/"
    }


    private val gson = GsonBuilder()
        .registerTypeAdapter(ZonedDateTime::class.java, LocalDateTimeAdapter())
        .create()


    val client: Retrofit get() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))

        .build()
}
