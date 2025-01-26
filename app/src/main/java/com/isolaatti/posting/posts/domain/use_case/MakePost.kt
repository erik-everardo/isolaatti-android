package com.isolaatti.posting.posts.domain.use_case

import android.net.Uri
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.posting.posts.data.remote.CreatePostDto
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MakePost @Inject constructor(private val postsRepository: PostsRepository) {
    operator fun invoke(
        privacy: Int,
        content: String,
        images: List<LocalImage>,
        audioId: String?,
        squadId: String?
    ): Flow<Resource<PostingSteps>>  {
        return postsRepository.makePost(CreatePostDto(privacy, content, audioId, squadId), images)
    }
}