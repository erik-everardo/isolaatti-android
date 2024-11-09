package com.isolaatti.images.common.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_drafts")
data class ImageDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val uri: String,
    val postId: Long? = null
)
