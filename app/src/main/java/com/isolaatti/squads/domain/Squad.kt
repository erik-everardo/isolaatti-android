package com.isolaatti.squads.domain

import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.squads.data.SquadDto

data class Squad(
    val id: String,
    val name: String,
    val description: String,
    val image: Image?,
    val extendedDescription: String = "",
) {
    companion object {
        fun fromDto(dto: SquadDto): Squad {
            return Squad(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                image = dto.imageId?.let { RemoteImage(it) },
                extendedDescription = dto.extendedDescription
            )
        }
    }
}
