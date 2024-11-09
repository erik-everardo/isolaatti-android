package com.isolaatti.images.common.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.isolaatti.images.common.data.entity.ImageDraftEntity

@Dao
interface ImagesDraftsDao {

    @Query("SELECT * FROM image_drafts WHERE postId IS NULL")
    fun getDetachedImages(): List<ImageDraftEntity>

    @Insert
    fun insertImageDraft(imageDraftEntity: ImageDraftEntity)

    @Query("DELETE FROM image_drafts WHERE id = :id")
    fun deleteById(id: Long)
}