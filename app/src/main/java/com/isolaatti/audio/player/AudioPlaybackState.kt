package com.isolaatti.audio.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.isolaatti.audio.common.domain.Audio

class AudioPlaybackState(
    currentAudio: Audio? = null,
    isPlaying: Boolean = false,
    duration: Long = 0L,
    progress: Long = 0L
) {
    var currentAudio by mutableStateOf(currentAudio)
    var isPlaying by mutableStateOf(isPlaying)
    var duration by mutableStateOf(duration)
    var progress by mutableStateOf(progress)
}
