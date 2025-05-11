package com.isolaatti.posting.posts.data.remote

import com.isolaatti.images.common.data.remote.ImageDto

data class FeedDto(
    val data: MutableList<PostDto>,
    var moreContent: Boolean
) {

    fun concatFeed(otherFeed: FeedDto?): FeedDto {
        if(otherFeed != null) {
            data.addAll(otherFeed.data)
            moreContent = otherFeed.moreContent
        }

        return this
    }

    data class PostDto(
        val post: Post,
        var numberOfLikes: Int,
        var numberOfComments: Int,
        val userName: String,
        val squadName: String?,
        var liked: Boolean,
    ){

        val list: MutableList<ImageDto> = mutableListOf()

        data class Post(
            val id: Long,
            var textContent: String,
            val userId: Int,
            val privacy: Int,
            val date: String,
            val hasAudio: Boolean,
            val squadId: String?,
            val linkedDiscussionId: Long,
            val linkedCommentId: Long,
            var images: List<ImageDto>
        )

    }
}