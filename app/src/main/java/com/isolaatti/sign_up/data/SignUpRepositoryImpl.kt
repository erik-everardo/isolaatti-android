package com.isolaatti.sign_up.data

import com.isolaatti.BuildConfig
import com.isolaatti.sign_up.data.dto.DataDto
import com.isolaatti.sign_up.data.dto.SignUpWithCodeDto
import com.isolaatti.sign_up.domain.SignUpRepository
import com.isolaatti.sign_up.domain.entity.GetCodeResult
import com.isolaatti.sign_up.domain.entity.SignUpResult
import com.isolaatti.sign_up.domain.entity.SignUpResultCode
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject

class SignUpRepositoryImpl @Inject constructor(private val signUpApi: SignUpApi): SignUpRepository {
    override fun getCode(email: String): Flow<Resource<GetCodeResult>> = flow {
        emit(Resource.Loading())
        try {
            val response = signUpApi.getCode(BuildConfig.clientId, BuildConfig.secret, DataDto(email)).awaitResponse()
            if(response.isSuccessful){
                response.body()?.let { emit(Resource.Success(GetCodeResult.valueOf(it.result)))}
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IllegalArgumentException) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError, "Could not map response. $e"))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun validateCode(code: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = signUpApi.validateCode(BuildConfig.clientId, BuildConfig.secret, DataDto(code)).awaitResponse()
            if(response.isSuccessful) {
                response.body()?.let { emit(Resource.Success(it.valid)) }
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun signUpWithCode(
        username: String,
        displayName: String,
        password: String,
        code: String
    ): Flow<Resource<SignUpResult>> = flow {
        emit(Resource.Loading())
        try {
            val response = signUpApi.signUpWithCode(
                BuildConfig.clientId,
                BuildConfig.secret,
                SignUpWithCodeDto(username, password, displayName, code)
            ).awaitResponse()
            if(response.isSuccessful){
                // TODO ver que hacer cuando se complete el registro, seguramente redirigir a login
                //response.body()?.let { emit(Resource.Success(SignUpResult(SignUpResultCode.valueOf(it.accountMakingResult), it.session)))}
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IllegalArgumentException) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError, "Could not map response. $e"))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }

    override fun checkUsernameAvailability(username: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = signUpApi.checkNameAvailability(username).awaitResponse()
            if(response.isSuccessful){
                response.body()?.let { emit(Resource.Success(it.available))}
            } else {
                emit(Resource.Error(Resource.Error.mapErrorCode(response.code())))
            }
        } catch (e: IllegalArgumentException) {
            emit(Resource.Error(Resource.Error.ErrorType.OtherError, "Could not map response. $e"))
        } catch(_: Exception) {
            emit(Resource.Error(Resource.Error.ErrorType.NetworkError))
        }
    }
}