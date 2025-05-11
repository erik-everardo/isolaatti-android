package com.isolaatti.images.common.domain.entity

import android.os.Parcel
import android.os.Parcelable
import com.isolaatti.common.Deletable
import com.isolaatti.markdown.Generators
import com.isolaatti.utils.UrlGen

/**
 * Represents an image that can be accessed using a remote uri.
 *
 * @property imageUrl The full resolution image url
 * @property  smallImageUrl A thumbnail url
 * @property reducedImageUrl A squared reduced version of the image url
 * @property markdown Markdown to insert this image in full resolution
 */
data class RemoteImage(
    val id: String
): Parcelable, Image {
    val imageUrl: String get() = UrlGen.imageUrl(id)
    val smallImageUrl : String get() = UrlGen.imageUrl(id, UrlGen.IMAGE_MODE_SMALL)
    val reducedImageUrl: String get() = UrlGen.imageUrl(id, UrlGen.IMAGE_MODE_REDUCED)

    val markdown: String get() = Generators.generateImage(imageUrl)

    constructor(parcel: Parcel) : this(parcel.readString()!!)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteImage

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RemoteImage> {
        override fun createFromParcel(parcel: Parcel): RemoteImage {
            return RemoteImage(parcel)
        }

        override fun newArray(size: Int): Array<RemoteImage?> {
            return arrayOfNulls(size)
        }
    }
}