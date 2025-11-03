package com.isolaatti.squads.presentation

import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.squads.data.SquadDto

data class SquadState(
    val squad: SquadDto? = null,
    val loadingSquad: Boolean = false,
    val refreshing: Boolean = false,
    val loadingPosts: Boolean = false,
    val posts: List<Post> = emptyList(),
    val moreContent: Boolean = false,
    val tabs: List<SquadTabs> = emptyList()
)