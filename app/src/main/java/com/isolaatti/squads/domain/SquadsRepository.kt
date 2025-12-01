package com.isolaatti.squads.domain

import com.isolaatti.images.common.domain.entity.LocalImage
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.posting.posts.data.remote.FeedDto
import com.isolaatti.squads.data.CreateSquadDto
import com.isolaatti.squads.data.CreateSquadResponseDto
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SquadsRepository {
    fun createSquad(squad: CreateSquadDto): Flow<Resource<CreateSquadResponseDto>>
    fun getMySquads(lastId: String? = null): Flow<Resource<List<SquadDto>>>
    fun getSquad(squadId: String): Flow<Resource<SquadDto>>
    fun deleteSquad(squadId: String): Flow<Resource<Void>>
    fun updateSquad(squadId: String, squad: CreateSquadDto): Flow<Resource<Void>>
    fun updatePrivacy(squadId: String, privacy: Int): Flow<Resource<Void>>
    fun getPosts(squadId: String, lastId: Long? = null, length: Int = 10): Flow<Resource<FeedDto>>
    fun updateImage(squadId: String, image: LocalImage): Flow<Resource<RemoteImage>>
}