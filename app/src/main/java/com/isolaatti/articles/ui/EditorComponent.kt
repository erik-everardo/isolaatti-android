package com.isolaatti.articles.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import jp.wasabeef.richeditor.RichEditor

@Composable
fun EditorComponent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val richEditor = remember {
        RichEditor(context).apply {
            this.setOnTextChangeListener {
                Log.d("****", it)
            }

        }
    }
    Column() {
        Row() {
            IconButton(onClick = {
                richEditor.setBold()
            }) {
                Icon(Icons.Default.FormatBold, "Bold")
            }
            IconButton(onClick = {
                richEditor.setItalic()
            }) {
                Icon(Icons.Default.FormatItalic, "Italic")
            }
        }
        AndroidView(
            modifier = modifier,
            factory = { context ->
                richEditor
            }
        )
    }
}