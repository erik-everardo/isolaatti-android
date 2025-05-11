package com.isolaatti.utils

sealed class ResourceWithSteps<T, S> {
    class Success<T, S>(val data: T?, val step: S): ResourceWithSteps<T, S>()
    class Loading<T, S>:  ResourceWithSteps<T, S>()
    class Error<T, S>(val errorType: ErrorType? = null, val message: String? = null): ResourceWithSteps<T, S>() {
        enum class ErrorType {
            NetworkError, AuthError, NotFoundError, ServerError, InputError, OtherError
        }
        companion object {
            fun mapErrorCode(errorCode: Int): ErrorType {
                return when(errorCode) {
                    401 -> ErrorType.AuthError
                    404 -> ErrorType.NotFoundError
                    505 -> ErrorType.ServerError
                    else -> ErrorType.OtherError
                }
            }
        }
    }
}