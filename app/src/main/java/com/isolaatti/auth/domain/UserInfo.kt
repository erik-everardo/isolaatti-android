package com.isolaatti.auth.domain

import com.isolaatti.utils.UrlGen

data class UserInfo(val id: Int, val username: String, val displayName: String) {

    val imageUrl get() = UrlGen.userProfileImage(id, false)


}