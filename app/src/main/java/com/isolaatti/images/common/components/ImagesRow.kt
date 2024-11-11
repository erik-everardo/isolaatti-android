package com.isolaatti.images.common.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.images.common.domain.entity.Image

@Composable
fun ImagesRow(
    modifier: Modifier = Modifier,
    images: List<Uri>,
    deletable: Boolean,
    addable: Boolean,
    onClick: (index: Int) -> Unit,
    onDeleteClick: (index: Int) -> Unit = {},
    onTakePicture: () -> Unit = {},
    onUploadPicture: () -> Unit = {}
) {
    var showAddPhotoPopUp by remember { mutableStateOf(false) }
    LazyRow(modifier, contentPadding = PaddingValues(horizontal = 8.dp)) {
        if(addable) {
            item {
                Card(modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(120.dp), onClick = {
                    showAddPhotoPopUp = true
                }) {


                    DropdownMenu(expanded = showAddPhotoPopUp, onDismissRequest = { showAddPhotoPopUp = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.take_a_photo )) },
                            onClick = {
                                showAddPhotoPopUp = false
                                onTakePicture()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.upload_a_picture )) },
                            onClick = {
                                showAddPhotoPopUp = false
                                onUploadPicture()
                            }
                        )
                    }

                    Image(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                        painter = painterResource(id = R.drawable.baseline_add_a_photo_24),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        contentDescription = null)
                }
            }
        }
        items(count = images.size) { index ->

            Card(modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(120.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    FilledTonalIconButton(
                        onClick = { onDeleteClick(index) },
                        modifier = Modifier.zIndex(2f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }

                    AsyncImage(
                        model = images[index],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().zIndex(1f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_5)
@Composable
fun ImagesRowPreview() {
    ImagesRow(images = emptyList(), deletable = false, addable = true, onClick = {})
}