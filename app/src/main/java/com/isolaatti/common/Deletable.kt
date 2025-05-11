package com.isolaatti.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

open class Deletable {
    var delete by mutableStateOf(false)
}