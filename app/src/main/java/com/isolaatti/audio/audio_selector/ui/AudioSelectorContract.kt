package com.isolaatti.audio.audio_selector.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.isolaatti.audio.common.domain.Audio
import com.isolaatti.audio.common.domain.Playable
import java.io.Serializable

/**
 * Contract to select audio. Use SelectorConfig class to specify the context to use to select audio.
 * Output: if user selected an audio or draft, it will be returned as result. Check type as convenient. Null if user cancelled
 */
class AudioSelectorContract : ActivityResultContract<AudioSelectorContract.SelectorConfig, Playable?>() {

    /**
     * @param forSquad audios source will be a specified squad in the id param
     * @param id squad id, should be non null if forSquad is true
     */
    data class SelectorConfig(val forSquad: Boolean, val id: String?): Serializable

    override fun createIntent(context: Context, input: SelectorConfig): Intent {
        val intent = Intent(context, AudioSelectorActivity::class.java)
        intent.apply {
            putExtra(AudioSelectorActivity.EXTRA_FOR_SQUAD, input.forSquad)
            if(input.forSquad) {
                putExtra(AudioSelectorActivity.EXTRA_ID, input.id as String)
            }

        }

        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Playable? {
        if(resultCode == Activity.RESULT_OK) {
            return intent?.extras?.getSerializable(AudioSelectorActivity.OUT_EXTRA_PLAYABLE) as? Playable
        }

        return null
    }
}