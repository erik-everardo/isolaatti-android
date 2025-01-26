package com.isolaatti.images.common.domain.entity

import android.net.Uri

/**
 * An image that is stored in disk. This cannot be deleted by app,
 * but only is to be uploaded to the server
 */
data class LocalImage(
    val uri: Uri
) : Image
