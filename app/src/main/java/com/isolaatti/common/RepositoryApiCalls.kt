package com.isolaatti.common

import com.isolaatti.utils.Resource
import java.io.IOException

private suspend fun <T, R> handleCall(
    call: suspend () -> retrofit2.Response<T>,
    onSuccess: suspend (T) -> R
): Resource<R> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { Resource.Success(onSuccess(it)) }
                ?: Resource.Error(Resource.Error.ErrorType.OtherError)
        } else {
            Resource.Error(Resource.Error.mapErrorCode(response.code()))
        }
    } catch (e: IOException) {
        Resource.Error(Resource.Error.ErrorType.NetworkError)
    }
}
