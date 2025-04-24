package com.isolaatti.posting.posts.data.remote

data class CreatePostDto(
    val privacy: Int,
    val content: String,
    val squadId: String?,
    val isDraft: Boolean = false
)