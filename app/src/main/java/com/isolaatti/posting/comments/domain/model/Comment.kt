package com.isolaatti.posting.comments.domain.model

import com.isolaatti.posting.comments.data.remote.CommentDto
import com.isolaatti.posting.comments.data.remote.FeedCommentsDto
import com.isolaatti.common.Ownable
import java.time.ZonedDateTime

data class Comment(
    val id: Long,
    val textContent: String,
    override val userId: Int,
    val postId: Long,
    val date: ZonedDateTime,
    val username: String,
    val isFromUser: Boolean,
    val repliesCount: Int,
    val nestedLevel: Int = 0,
    val parentCommentId: Long? = null,
    val loadingReplies: Boolean = false,
    val enableRepliesButton: Boolean = true,
    val allowReplies: Boolean = true
) : Ownable {
    companion object {
        fun fromCommentsDto(dtoList: FeedCommentsDto): List<Comment> {
            return dtoList.data.map {
                Comment(
                    id = it.id,
                    textContent = it.textContent,
                    userId = it.userId,
                    postId = it.postId,
                    date = it.date,
                    username = it.username,
                    isFromUser = it.isFromUser,
                    repliesCount = it.repliesCount
                )
            }
        }

        fun fromCommentDto(dto: CommentDto): Comment {
            return Comment(
                id = dto.id,
                textContent = dto.textContent,
                userId = dto.userId,
                postId = dto.postId,
                date = dto.date,
                username = dto.username,
                isFromUser = dto.isFromUser,
                repliesCount = dto.repliesCount
            )
        }
    }
}
