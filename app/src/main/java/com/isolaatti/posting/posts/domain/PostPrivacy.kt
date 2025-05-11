package com.isolaatti.posting.posts.domain

enum class PostPrivacy {
    /**
     * Only user will see the post in their profile
     */
    Private,

    /**
     * Any user inside can see this post
     */
    Isolaatti,

    /**
     * Any user, inside and outside can see this post
     */
    Public;

    fun toInt(): Int {
        return this.ordinal + 1
    }

    companion object {
        fun fromInt(value: Int): PostPrivacy {
            return when(value) {
                2 -> Isolaatti
                3 -> Public
                else -> Private
            }
        }
    }
}