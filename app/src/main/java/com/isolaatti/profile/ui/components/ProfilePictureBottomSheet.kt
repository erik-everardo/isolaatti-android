package com.isolaatti.profile.ui.components

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isolaatti.R


data class ProfilePictureState(
    val showViewImage: Boolean = true,
    val showChangeProfileImage: Boolean = true,
    val showRemoveImage: Boolean = true
)

enum class ProfilePictureBottomSheetActions {
    ViewImage, ChangeImage, RemoveImage, TakeAPicture
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    state: ProfilePictureState,
    onAction: (ProfilePictureBottomSheetActions) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column {
            Text(
                text = stringResource(R.string.profile_photo),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )

            if(state.showViewImage) {
                TextButton(
                    onClick = { onAction(ProfilePictureBottomSheetActions.ViewImage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.view_photo))
                }
            }

            if(state.showChangeProfileImage) {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(
                        onClick = {
                            expanded = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.change_profile_photo))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.upload_a_picture)) },
                            onClick = {
                                onAction(ProfilePictureBottomSheetActions.ChangeImage)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.take_a_photo)) },
                            onClick = {
                                onAction(ProfilePictureBottomSheetActions.TakeAPicture)
                            }
                        )
                    }
                }

            }
            if(state.showRemoveImage) {
                TextButton(
                    onClick = { onAction(ProfilePictureBottomSheetActions.RemoveImage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.remove_photo))
                }
            }
        }
    }
}