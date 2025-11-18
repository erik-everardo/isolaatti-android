package com.isolaatti.squads.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.squads.presentation.EditExtendedDescriptionScreenViewModel
import com.isolaatti.squads.ui.screens.EditExtendedDescriptionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditExtendedDescriptionActivity : ComponentActivity() {

    private val viewModel: EditExtendedDescriptionScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            IsolaattiTheme {
                EditExtendedDescriptionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onSave = {
                        viewModel.saveDescription()
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_DESCRIPTION_RESULT, viewModel.description)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_SQUAD_ID = "squad_id"
        const val EXTRA_DESCRIPTION_RESULT = "description_result"
    }
}
