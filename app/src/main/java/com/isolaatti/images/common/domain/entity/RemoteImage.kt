package com.isolaatti.images.common.domain.entity

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.core.net.toUri
import com.isolaatti.common.Deletable
import com.isolaatti.markdown.Generators
import com.isolaatti.utils.UrlGen
import kotlinx.parcelize.Parcelize

/**
 * Represents an image that can be accessed using a remote uri.
 *
 * @property imageUrl The full resolution image url
 * @property  smallImageUrl A thumbnail url
 * @property reducedImageUrl A squared reduced version of the image url
 * @property markdown Markdown to insert this image in full resolution
 */
@Parcelize
data class RemoteImage(
    val id: String
): Parcelable, Image() {
    val imageUrl: String get() = UrlGen.imageUrl(id)
    val smallImageUrl : String get() = UrlGen.imageUrl(id, UrlGen.IMAGE_MODE_SMALL)
    val reducedImageUrl: String get() = UrlGen.imageUrl(id, UrlGen.IMAGE_MODE_REDUCED)

    override val defaultUri: Uri
        get() = imageUrl.toUri()

    override val thumbnailUri: Uri
        get() = smallImageUrl.toUri()



    val markdown: String get() = Generators.generateImage(imageUrl)
}