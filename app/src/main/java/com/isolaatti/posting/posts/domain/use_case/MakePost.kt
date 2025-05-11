package com.isolaatti.posting.posts.domain.use_case

import android.net.Uri
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.posting.posts.domain.PostPrivacy
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.utils.Resource
import com.isolaatti.utils.ResourceWithSteps
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MakePost @Inject constructor(private val postsRepository: PostsRepository) {
    operator fun invoke(
        privacy: PostPrivacy,
        content: String,
        images: List<LocalImage>,
        audio: Uri?,
        squadId: String?
    ): Flow<ResourceWithSteps<Post, PostingSteps>>  {
        return postsRepository.makePost(privacy,content,squadId, images, audio)
    }
}