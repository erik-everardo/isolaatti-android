package com.isolaatti.video_streaming.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.unit.dp

class VideoStreamingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Video Streaming") }, navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.Filled.Close, "close")
                        }
                    })
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        startActivity(Intent(this, StreamerActivity::class.java))
                    }) {
                        Icon(Icons.Filled.Add, "add")
                    }
                }
            ) { paddingValues ->
                LazyHorizontalGrid(
                    rows = GridCells.Adaptive(500.dp),
                    contentPadding = paddingValues
                ) {

                }
            }
        }
    }
}