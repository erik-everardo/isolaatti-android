package com.isolaatti.posting.viewer.ui

import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.profile.domain.entity.ProfileListItem

data class PostViewerState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val noMoreComments: Boolean? = null,
    val loadingComments: Boolean = false,
    val refreshingComments: Boolean = false,
    val postLoading: Boolean = false,
    val uploadingComment: Boolean = false,
    val participantFollowStateInUpdate: ProfileListItem? = null,
    val showDeleteCommentDialog: Boolean = false,
    val deletingComment: Boolean = false,
    val showReplyCommentDialog: Boolean = true,
    val commentToReply: Comment? = null,
    val commentToEdit: Comment? = null,
    val commentBoxText: String = "",
    val commentPositionToHighlight: Int? = null
)
