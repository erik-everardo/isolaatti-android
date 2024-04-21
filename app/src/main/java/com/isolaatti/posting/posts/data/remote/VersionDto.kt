package com.isolaatti.posting.posts.data.remote

import java.time.ZonedDateTime

data class VersionDto(
    val id: Long,
    val postId: Long,
    val textContent: String,
    val dateTime: ZonedDateTime,
    val audioId: String
)