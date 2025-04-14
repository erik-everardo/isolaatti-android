package com.isolaatti.audio.common.components

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.isolaatti.R
import com.isolaatti.utils.clockFormat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


class AudioRecorderState(
    isRecording: Boolean = false,
    recordIsStopped: Boolean = false,
    recordIsPaused: Boolean = false,
    isPlaying: Boolean = false,
    isLoading: Boolean = false,
    durationSeconds: Long = 0L,
    position: Long = 0L
) {
    var isRecording by mutableStateOf(isRecording)
    var recordIsStopped by mutableStateOf(recordIsStopped)
    var recordIsPaused by mutableStateOf(recordIsPaused)
    var isPlaying by mutableStateOf(isPlaying)
    var isLoading by mutableStateOf(isLoading)
    var durationMilliseconds by mutableStateOf(durationSeconds)
    var position by mutableStateOf(position)

    fun clearState() {
        recordIsStopped = false
        isRecording = false
        recordIsPaused = false
        isPlaying = false
        durationMilliseconds = 0L
        position = 0L
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorder(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {},
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onStartRecording: (fromPaused: Boolean) -> Unit,
    state: AudioRecorderState = AudioRecorderState(),
    onSeek: (position: Float) -> Unit = {},
) {




    val audioRecordPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var duration by remember { mutableLongStateOf(0L) }


    LaunchedEffect(state.isRecording, state.recordIsPaused) {
        coroutineScope {
            while(state.isRecording && !state.recordIsPaused) {
                duration += 500
                delay(500.milliseconds)
            }
        }
    }

    Card(modifier = modifier
        .fillMaxWidth()
        .height(120.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = onDismiss,
                modifier = Modifier.zIndex(2f)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)

            }
            Text(stringResource(R.string.record_new_audio))
        }

        when {
            audioRecordPermissionState.status.isGranted -> {
                when {
                     !state.isRecording && !state.recordIsStopped-> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = {
                                    onStartRecording(false)
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_circle_24),
                                    null,
                                    tint = Color.Red
                                )
                            }
                        }
                    }

                    state.isRecording && !state.recordIsStopped -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(duration.milliseconds.clockFormat(), modifier = Modifier.padding(horizontal = 4.dp))
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                if(state.recordIsPaused) {
                                    onStartRecording(true)

                                } else {
                                    onPauseRecording()
                                }

                            }) {
                                if(state.recordIsPaused) {
                                    Icon(painterResource(R.drawable.baseline_circle_24), null, tint = Color.Red)
                                } else {
                                    Icon(painterResource(R.drawable.baseline_pause_24), null)
                                }
                            }
                            IconButton(onClick = {
                                onStopRecording()
                            }) {
                                Icon(painterResource(R.drawable.baseline_stop_24), null)
                            }
                        }
                    }

                    else -> {
                        AudioPlayer(
                            modifier = Modifier.fillMaxWidth(),
                            positionMs = state.position,
                            isLoading = state.isLoading,
                            isPlaying = state.isPlaying,
                            durationMs = state.durationMilliseconds,
                            onPlay = onPlay,
                            onPause = onPause,
                            onSeek = onSeek
                        )
                    }
                }
            }

            audioRecordPermissionState.status.shouldShowRationale -> {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(stringResource(R.string.record_audio_permission_required_rationale))
                    Button(
                        onClick = {
                            audioRecordPermissionState.launchPermissionRequest()
                        }
                    ) {
                        Text(stringResource(R.string.request_permission))
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalAlignment = Alignment.Start) {
                    Text(stringResource(R.string.record_audio_permission_required))
                    Button(
                        onClick = {
                            audioRecordPermissionState.launchPermissionRequest()
                        }
                    ) {
                        Text(stringResource(R.string.request_permission))
                    }
                }
            }
        }
    }
}