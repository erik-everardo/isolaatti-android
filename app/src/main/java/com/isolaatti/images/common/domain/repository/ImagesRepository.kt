package com.isolaatti.images.common.domain.repository

import android.net.Uri
import com.isolaatti.images.common.data.entity.ImageDraftEntity
import com.isolaatti.images.common.domain.entity.Image
import com.isolaatti.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ImagesRepository {
    fun getImagesOfUser(userId: Int, lastId: String? = null): Flow<Resource<List<Image>>>
    fun deleteImages(images: List<Image>): Flow<Resource<Boolean>>
    fun uploadImage(name: String, imageUri: Uri, squadId: String?): Flow<Resource<Image>>
    fun getDetachedDraftImages(): Flow<Resource<List<ImageDraftEntity>>>
    fun getDraftImagesOfPost(postId: Long): Flow<Resource<List<ImageDraftEntity>>>
}