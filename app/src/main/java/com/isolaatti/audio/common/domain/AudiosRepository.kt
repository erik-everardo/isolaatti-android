package com.isolaatti.audio.common.domain

import android.net.Uri
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AudiosRepository {
    fun getAudiosOfUser(userId: Int, lastId: String?): Flow<Resource<List<Audio>>>
    fun getMyAudios(lastId: String?): Flow<Resource<List<Audio>>>

    fun uploadAudio(uri: Uri, postId: Long): Flow<Resource<Audio>>

    fun deleteAudio(audioId: String): Flow<Resource<Boolean>>
}