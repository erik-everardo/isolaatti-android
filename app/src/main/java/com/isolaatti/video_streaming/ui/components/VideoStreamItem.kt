package com.isolaatti.video_streaming.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.common.IsolaattiTheme

@Composable
fun VideoStreamItem(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(16f/9f)
    ) {
        Column {
            AsyncImage(
                modifier = Modifier.weight(1f),
                model = painterResource(id = R.drawable.baseline_play_circle_24),
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.baseline_play_circle_24),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Video Title"
            )
        }
    }
}

@Preview
@Composable
private fun VideoStreamItemPreview() {
    IsolaattiTheme {
        VideoStreamItem(

        )
    }
}