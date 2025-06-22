package com.isolaatti.posting.comments.domain.use_case

import com.isolaatti.posting.comments.domain.CommentsRepository
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EditComment @Inject constructor(private val commentsRepository: CommentsRepository) {
    operator fun invoke(commentId: Long, content: String, audioId: String?): Flow<Resource<Comment>> {
        return commentsRepository.editComment(commentId, content)
    }
}