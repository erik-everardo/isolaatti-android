package com.isolaatti.audio.recorder.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.isolaatti.audio.drafts.domain.AudioDraft

class AudioRecorderContract : ActivityResultContract<Long?, AudioDraft?>() {

    override fun createIntent(context: Context, input: Long?): Intent {
        val intent = Intent(context, AudioRecorderActivity::class.java).apply {
            if(input != null) {
                putExtra(AudioRecorderActivity.IN_EXTRA_DRAFT_ID, input)
            }
        }

        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AudioDraft? {
        return when(resultCode){
            Activity.RESULT_OK -> {
                intent?.getSerializableExtra(AudioRecorderActivity.OUT_EXTRA_DRAFT) as? AudioDraft
            }
            else -> null
        }
    }
}