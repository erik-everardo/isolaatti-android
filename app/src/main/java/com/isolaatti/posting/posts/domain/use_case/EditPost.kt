package com.isolaatti.posting.posts.domain.use_case

import android.net.Uri
import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.domain.PostPrivacy
import com.isolaatti.posting.posts.domain.PostingSteps
import com.isolaatti.posting.posts.domain.PostsRepository
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.utils.ResourceWithSteps
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EditPost @Inject constructor(private val postsRepository: PostsRepository) {
    operator fun invoke(
        postId: Long,
        privacy: PostPrivacy,
        images: List<LocalImage>,
        imagesToRemove: List<RemoteImage>,
        audio: Uri?,
        removeOldAudio: Boolean,
        content: String,
        squadId: String?
    ): Flow<ResourceWithSteps<Post, PostingSteps>> {
        return postsRepository.editPost(
            postId,
            privacy,
            content,
            squadId,
            images,
            imagesToRemove,
            audio,
            removeOldAudio
        )
    }
}