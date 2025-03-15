package com.isolaatti.audio.player

import com.isolaatti.audio.common.domain.Audio

data class AudioPlaybackState(
    val currentAudio: Audio? = null,
    var isPlaying: Boolean = false,
    val duration: Long = 0L,
    val progress: Long = 0L
)
