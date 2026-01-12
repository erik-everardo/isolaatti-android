package com.isolaatti.sign_up.domain.entity


enum class SignUpResultCode {
    EmailNotAvailable, ValidationProblems, Ok, Error, UsernameUnavailable
}

data class SignUpResult(
    val resultCode: SignUpResultCode,
)