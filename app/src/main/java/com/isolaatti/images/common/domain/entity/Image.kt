package com.isolaatti.images.common.domain.entity

import android.net.Uri
import android.os.Parcelable

/**
 * Marker interface to identify images
 */
sealed class Image : Parcelable {
    abstract val defaultUri: Uri
    abstract val thumbnailUri: Uri
}