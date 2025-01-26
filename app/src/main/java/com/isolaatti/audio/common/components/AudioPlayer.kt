package com.isolaatti.audio.common.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.isolaatti.R
import com.isolaatti.utils.clockFormat
import kotlin.time.Duration.Companion.milliseconds


@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun AudioPlayer(
    modifier: Modifier,
    positionMs: Long,
    isPlaying: Boolean,
    isLoading: Boolean,
    durationMs: Long,
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {}
) {
    val sliderValue = positionMs.coerceAtLeast(1).toFloat() / durationMs.coerceAtLeast(1).toFloat()



    Card(modifier = modifier) {

        Row(modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = {if(isPlaying) onPause() else onPlay()}) {
                if(isPlaying) {
                    Icon(painterResource(R.drawable.baseline_pause_24), null)
                } else {
                    Icon(Icons.Default.PlayArrow, null)
                }
            }
            if(isLoading) {
                LinearProgressIndicator(modifier = Modifier.padding(horizontal = 4.dp).weight(1f))
            } else {
                Slider(value = sliderValue, onValueChange = {}, modifier = Modifier.padding(horizontal = 4.dp).weight(1f))
            }

            Text("${positionMs.milliseconds.clockFormat()}/${durationMs.milliseconds.clockFormat()}")
        }

    }

}