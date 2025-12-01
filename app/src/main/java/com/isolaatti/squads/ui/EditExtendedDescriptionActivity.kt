package com.isolaatti.squads.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.isolaatti.common.IsolaattiTheme
import com.isolaatti.squads.presentation.EditExtendedDescriptionScreenViewModel
import com.isolaatti.squads.ui.screens.EditExtendedDescriptionScreen
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon

@AndroidEntryPoint
class EditExtendedDescriptionActivity : ComponentActivity() {

    private val viewModel: EditExtendedDescriptionScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            IsolaattiTheme {
                val state by viewModel.state.collectAsState()
                CompositionLocalProvider(LocalMarkwon provides Markwon.builder(LocalContext.current).build()) {
                    EditExtendedDescriptionScreen(
                        state = state,
                        onNavigateBack = { finish() },
                        onExtendedDescriptionTextChanged = {
                            viewModel.onUpdateDescription(it)
                        },
                        onSave = {
                            viewModel.saveDescription()
                            val resultIntent = Intent().apply {
                                putExtra(EXTRA_DESCRIPTION_RESULT, viewModel.state.value.squad?.description)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        setIsInPreview = viewModel::setIsInPreview,
                        shouldShowPendingChangedDialog = viewModel.pendingChanges()
                    )
                }

            }
        }
    }

    companion object {
        const val EXTRA_SQUAD_ID = "squad_id"
        const val EXTRA_DESCRIPTION_RESULT = "description_result"
    }
}
