package com.isolaatti.posting.posts.domain.use_case

import com.isolaatti.posting.posts.domain.PostsRepository
import javax.inject.Inject

class GetPostLikedBy @Inject constructor(private val postsRepository: PostsRepository) {
    operator fun invoke(postId: Long) = postsRepository.getUsersLikedPost(postId)
}