package com.isolaatti.squads.presentation

import android.net.Uri
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.squads.domain.Squad

data class SquadState(
    val squad: Squad? = null,
    val loadingSquad: Boolean = false,
    val refreshing: Boolean = false,
    val loadingPosts: Boolean = false,
    val posts: List<Post> = emptyList(),
    val moreContent: Boolean = false,
    val tabs: List<SquadTabs> = emptyList(),
    val showImageDialog: Boolean = false,
    val uploadingSquadPicture: Boolean = false,
    val showConfirmImageChange: Boolean = false,
    val squadPictureToUpload: Uri = Uri.EMPTY,
)