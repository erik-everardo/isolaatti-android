package com.isolaatti.images.common.domain.entity

import android.os.Parcel
import android.os.Parcelable
import com.isolaatti.common.Deletable
import com.isolaatti.images.common.data.remote.ImageDto
import com.isolaatti.markdown.Generators
import com.isolaatti.utils.UrlGen
import java.io.Serializable

data class Image(
    val id: String
): Deletable(), Parcelable {
    val imageUrl: String get() = UrlGen.imageUrl(id)
    val smallImageUrl : String get() = UrlGen.imageUrl(id, UrlGen.IMAGE_MODE_SMALL)
    val reducedImageUrl: String get() = UrlGen.imageUrl(id, UrlGen.IMAGE_MODE_REDUCED)

    val markdown: String get() = Generators.generateImage(imageUrl)

    constructor(parcel: Parcel) : this(parcel.readString()!!)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

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

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }
}