package com.isolaatti.video_streaming.domain

import java.util.UUID

data class VideoStreamingStation(
    val id: UUID,
    val name: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val description: String?
)

