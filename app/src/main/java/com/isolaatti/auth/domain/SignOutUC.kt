package com.isolaatti.auth.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class SignOutUC @Inject constructor(
    @ActivityContext private val context: Context,
) {
    operator fun invoke() {


    }
}