package com.isolaatti.images.common.domain.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * An image that is stored in disk. This cannot be deleted by app,
 * but only is to be uploaded to the server
 */
@Parcelize
data class LocalImage(
    val uri: Uri
) : Image(), Parcelable {
    override val defaultUri: Uri
        get() = uri

    override val thumbnailUri: Uri
        get() = uri
}
