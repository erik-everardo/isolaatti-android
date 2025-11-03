package com.isolaatti.squads.data

import com.google.gson.annotations.SerializedName

data class CreateSquadDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("extendedDescription") val extendedDescription: String,
    @SerializedName("privacy") val privacy: Int
)

data class SquadsDto(@SerializedName("squads") val squads: List<SquadDto>)

data class SquadWrapperDto(@SerializedName("squad") val squad: SquadDto)

data class SquadDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("extendedDescription") val extendedDescription: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("privacy") val privacy: Int,
    @SerializedName("creationTime") val creationTime: String,
    @SerializedName("imageId") val imageId: String?,
    @SerializedName("isOwned") val isOwned: Boolean
)

data class CreateSquadResponseDto(
    @SerializedName("creationResult") val creationResult: Int,
    @SerializedName("squad") val squad: SquadDto
)




