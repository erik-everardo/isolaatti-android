package com.isolaatti.utils

import java.lang.StringBuilder
import kotlin.time.Duration

fun Duration.clockFormat(): String {
    return toComponents { hours, minutes, seconds, nanoseconds ->
        val sb = StringBuilder().apply {
            if(hours > 0) {
                append(hours.toString().padStart(2, '0'))
                append(":")
            }
            append(minutes.toString().padStart(2, '0'))
            append(":")
            append(seconds.toString().padStart(2, '0'))
        }

        sb.toString()
    }
}