package com.isolaatti.search.data

import com.isolaatti.posting.posts.data.remote.FeedDto


data class ProfileSearchDto(val id: Int, val name: String, val imageId: String?, val following: Boolean)
data class SearchDto(
    val profiles: List<ProfileSearchDto>,
    val posts: List<FeedDto.PostDto>
    // TODO add the other types
)

data class HashtagsDto(val result: List<String>)

data class NewestUsersDto(val result: List<ProfileSearchDto>)