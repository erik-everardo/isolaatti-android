package com.isolaatti.audio.common.components

import android.media.MediaRecorder
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AudioRecorder(
    onAudioRecorded: (Uri) -> Unit,
    dismissible: Boolean,
    onDismiss: () -> Unit,
) {

    var context = LocalContext.current
    var mediaRecorder: MediaRecorder?

    DisposableEffect(Unit) {
        mediaRecorder = MediaRecorder()

        onDispose {
            mediaRecorder?.release()
        }
    }
}

@Preview
@Composable
fun AudioRecorderPreview() {

}