package com.isolaatti.profile.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.isolaatti.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmChangeProfilePictureBottomSheet(
    onDismiss: () -> Unit,
    oldImage: Uri?,
    newImage: Uri?,
    onAccept: () -> Unit,
    loading: Boolean
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.change_profile_photo_question),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )

            if(!loading) {
                Box(Modifier.padding(8.dp).fillMaxWidth()) {
                    if(oldImage != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = oldImage,
                                contentDescription = stringResource(R.string.old_picture),
                                modifier = Modifier.size(70.dp)
                                    .clip(RoundedCornerShape(50))
                            )

                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(40.dp))

                            AsyncImage(
                                model = newImage,
                                contentDescription = stringResource(R.string.old_picture),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .size(70.dp)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            AsyncImage(
                                model = newImage,
                                contentDescription = stringResource(R.string.old_picture),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .size(70.dp)
                            )
                        }
                    }
                }


                Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FilledTonalButton(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAccept()
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                }
            } else {
                CircularProgressIndicator()
                Text(stringResource(R.string.uploading_photo))
            }
        }

    }
}