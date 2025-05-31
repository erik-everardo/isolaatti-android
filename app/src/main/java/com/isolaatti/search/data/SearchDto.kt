package com.isolaatti.search.data


enum class SearchResultType {
    Profile, Post, Hashtag, Unknown
}
data class ProfileSearchDto(val id: Int, val name: String, val following: Boolean)
data class SearchDto(val result: List<SearchResultDto>)

data class SearchResultDto(
    val origin: String,
    val resourceId: String,
    val title: String,
    val description: String
) {

    companion object {
        const val ORIGIN_POSTS = "posts"
        const val ORIGIN_HASHTAGS = "hashtags"
        const val ORIGIN_USERS = "users"
    }
    val type: SearchResultType get() {
        return when(origin) {
            ORIGIN_POSTS -> SearchResultType.Post
            ORIGIN_HASHTAGS -> SearchResultType.Hashtag
            ORIGIN_USERS -> SearchResultType.Profile
            else -> SearchResultType.Unknown
        }
    }
}

data class HashtagsDto(val result: List<String>)

data class NewestUsersDto(val result: List<ProfileSearchDto>)