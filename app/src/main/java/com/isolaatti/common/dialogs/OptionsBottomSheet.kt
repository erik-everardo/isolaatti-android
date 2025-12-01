package com.isolaatti.common.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isolaatti.R

data class OptionsBottomSheetState(
    val title: String = "",
    val options: List<BottomSheetOption> = emptyList()
)



data class BottomSheetOption(
    val title: String,
    val icon: Painter?,
    val popUpOptions: List<PopUpOption>? = null,
) {
    data class PopUpOption(
        val title: String
    )
}

@Composable
fun rememberOptionsBottomSheetState(
    title: String,
    options: List<BottomSheetOption>
): OptionsBottomSheetState {
    return remember { OptionsBottomSheetState(title, options) }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    state: OptionsBottomSheetState,
    onAction: (Int, Int?) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column {
            Text(
                text = state.title,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )

            state.options.forEachIndexed { optionIndexed, option ->
                Box{
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(
                        onClick = {
                            expanded = true
                            onAction(optionIndexed, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option.title)
                    }

                    if(!option.popUpOptions.isNullOrEmpty()) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            option.popUpOptions.forEachIndexed { popUpOptionIndex, popUpOption ->
                                DropdownMenuItem(
                                    text = { Text(popUpOption.title) },
                                    onClick = {
                                        onAction(optionIndexed, popUpOptionIndex)
                                        expanded = false
                                    }
                                )
                            }

                        }


                    }
                }
            }
        }
    }
}