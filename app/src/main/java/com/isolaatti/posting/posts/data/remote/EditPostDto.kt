package com.isolaatti.posting.posts.data.remote

data class EditPostDto(
    val privacy: Int,
    val content: String,
    val squadId: String? = null,
    val postId: Long
) {
    companion object {
        const val PRIVACY_PRIVATE = 1
        const val PRIVACY_ISOLAATTI = 2
        const val PRIVACY_PUBLIC = 3
    }
}
