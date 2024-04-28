package com.isolaatti.profile.domain.entity

import com.isolaatti.profile.data.remote.ProfileListItemDto
import com.isolaatti.search.data.ProfileSearchDto

class ProfileListItem(
    val id: Int,
    val name: String,
    val profileImageId: String?,
    var following: Boolean?
) {

    var updatingFollowState = false

    companion object {
        fun fromDto(dto: ProfileListItemDto) = ProfileListItem(
            dto.id,dto.name, dto.profileImageId, dto.following
        )

        fun fromDto(dto: ProfileSearchDto) = ProfileListItem(
            dto.id, dto.name, dto.imageId, dto.following
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileListItem

        if (id != other.id) return false
        if (name != other.name) return false
        if (profileImageId != other.profileImageId) return false
        return following == other.following
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + (profileImageId?.hashCode() ?: 0)
        result = 31 * result + (following?.hashCode() ?: 0)
        return result
    }
}