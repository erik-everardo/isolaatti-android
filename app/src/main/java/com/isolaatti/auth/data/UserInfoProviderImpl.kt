package com.isolaatti.auth.data

import com.isolaatti.auth.domain.UserInfoProvider
import com.isolaatti.profile.data.remote.ProfileApi
import com.isolaatti.profile.domain.entity.UserProfile
import retrofit2.HttpException
import retrofit2.awaitResponse

class UserInfoProviderImpl(private val profileApi: ProfileApi) : UserInfoProvider {
    override suspend fun fetchUserInfo(accessToken: String): UserProfile {
        val response = profileApi.getMyProfile().awaitResponse()

        if(response.isSuccessful) {
            val dto = response.body()
            val user = dto?.let { UserProfile.fromDto(it) }

            return user ?: error("Could not get user from response")
        } else {
            throw HttpException(response)
        }
    }
}