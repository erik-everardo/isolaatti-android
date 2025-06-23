package com.isolaatti.posting.post_creator.presentation

import com.isolaatti.posting.posts.domain.entity.Post

sealed class CreatePostEvents {
    data class PostCreatedOrEdited(val post: Post) : CreatePostEvents()
}