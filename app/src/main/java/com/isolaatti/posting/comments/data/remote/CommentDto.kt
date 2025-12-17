package com.isolaatti.posting.comments.data.remote

import com.isolaatti.posting.comments.domain.model.Comment
import java.time.ZonedDateTime

data class CommentDto(
    val id: Long,
    val textContent: String,
    val userId: Int,
    val postId: Long,
    val date: ZonedDateTime,
    val username: String,
    val isFromUser: Boolean,
    val repliesCount: Int
)
