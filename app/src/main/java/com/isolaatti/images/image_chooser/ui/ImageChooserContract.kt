package com.isolaatti.images.image_chooser.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import com.isolaatti.images.common.data.entity.ImageDraftEntity
import com.isolaatti.images.common.domain.entity.Image

class ImageChooserContract : ActivityResultContract<ImageChooserContract.Requester, ImageDraftEntity?>() {

    enum class Requester {
        UserPost, SquadPost
    }
    override fun createIntent(context: Context, input: Requester): Intent {
        return Intent(context, ImageChooserActivity::class.java).apply {
            putExtra(ImageChooserActivity.INPUT_EXTRA, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ImageDraftEntity? {
        if(resultCode != Activity.RESULT_OK) { return null }

        if(intent == null) {
            return null
        }

        return IntentCompat.getSerializableExtra(intent, ImageChooserActivity.OUTPUT_EXTRA_IMAGE, ImageDraftEntity::class.java)
    }
}