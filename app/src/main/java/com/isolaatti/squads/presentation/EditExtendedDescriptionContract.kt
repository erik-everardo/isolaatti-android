package com.isolaatti.squads.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.isolaatti.squads.ui.EditExtendedDescriptionActivity

class EditExtendedDescriptionContract : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, input: String): Intent {
        return Intent(context, EditExtendedDescriptionActivity::class.java).apply {
            putExtra(EditExtendedDescriptionActivity.EXTRA_SQUAD_ID, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getStringExtra(EditExtendedDescriptionActivity.EXTRA_DESCRIPTION_RESULT)
    }
}
