package com.isolaatti.profile.domain.entity

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.isolaatti.profile.data.remote.ProfileListItemDto
import com.isolaatti.search.data.ProfileSearchDto



class ProfileListItem(
    val id: Int,
    val name: String,
    var following: Boolean?
) : Parcelable {

    var updatingFollowState = false

    companion object {
        fun fromDto(dto: ProfileListItemDto) = ProfileListItem(
            dto.id,dto.name, dto.following
        )

        fun fromDto(dto: ProfileSearchDto) = ProfileListItem(
            dto.id, dto.name, dto.following
        )
    }

    @JvmField
    val CREATOR = object: Creator<ProfileListItem> {
        override fun createFromParcel(source: Parcel?): ProfileListItem? {
            if(source == null) {
                return null
            }
            return ProfileListItem(
                source.readInt(),
                source.readString()!!,
                source.readInt() == 1
            )
        }

        override fun newArray(size: Int): Array<out ProfileListItem?>? {
            return arrayOfNulls(size)
        }

    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileListItem

        if (id != other.id) return false
        if (name != other.name) return false
        return following == other.following
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + (following?.hashCode() ?: 0)
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeInt(if(following == true) 1 else 0)
    }
}