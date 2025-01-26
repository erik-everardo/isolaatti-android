package com.isolaatti.images.common.domain.repository

import android.net.Uri
import com.isolaatti.images.common.domain.entity.RemoteImage
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ImagesRepository {
    fun getImagesOfUser(userId: Int, lastId: String? = null): Flow<Resource<List<RemoteImage>>>
    fun deleteImages(images: List<RemoteImage>): Flow<Resource<Boolean>>
    fun uploadImage(imageUri: Uri, postId: Long, squadId: String?): Flow<Resource<RemoteImage>>
}