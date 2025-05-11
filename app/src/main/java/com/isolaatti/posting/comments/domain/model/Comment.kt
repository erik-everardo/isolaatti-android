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
    val username: String
) : Ownable {
    companion object {
        fun fromCommentsDto(dtoList: FeedCommentsDto): List<Comment> {
            return dtoList.data.map {
                Comment(
                    id = it.comment.id,
                    textContent = it.comment.textContent,
                    userId = it.comment.userId,
                    postId = it.comment.postId,
                    date = it.comment.date,
                    username = it.username
                )
            }
        }

        fun fromCommentDto(dto: CommentDto): Comment {
            return Comment(
                id = dto.comment.id,
                textContent = dto.comment.textContent,
                userId = dto.comment.userId,
                postId = dto.comment.postId,
                date = dto.comment.date,
                username = dto.username
            )
        }
    }
}
