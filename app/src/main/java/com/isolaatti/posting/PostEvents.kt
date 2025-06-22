package com.isolaatti.posting

import com.isolaatti.posting.comments.domain.model.Comment

sealed class PostEvents {
    data class CommentEdited(val position: Int) : PostEvents()
    data object CommentEditError : PostEvents()
    data object CommentRemoved : PostEvents()
    data object CommentRemoveError : PostEvents()
    data class CommentPosted(val position: Int) : PostEvents()
    data object PostingComment : PostEvents()
    data object EditingComment : PostEvents()
}