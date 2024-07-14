package com.isolaatti.audio.drafts.domain

import android.net.Uri
import androidx.core.net.toUri
import com.isolaatti.MyApplication
import com.isolaatti.audio.common.domain.Playable
import com.isolaatti.audio.drafts.data.AudioDraftEntity
import java.io.File
import java.io.Serializable

data class AudioDraft(val id: Long, val name: String, val localStorageRelativePath: String, val size: Long) : Playable(), Serializable {
    override val thumbnail: String?
        get() = null

    override val uri: Uri
        get() {
            val appFiles = MyApplication.myApp.applicationContext.filesDir
            return File(appFiles, localStorageRelativePath).toUri()
        }

    companion object {
        fun fromEntity(audioDraftEntity: AudioDraftEntity): AudioDraft {
            return AudioDraft(audioDraftEntity.id, audioDraftEntity.name, audioDraftEntity.audioLocalPath, audioDraftEntity.sizeInBytes)
        }
    }
}