package com.isolaatti.articles.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.isolaatti.common.IsolaattiTheme

class ArticlesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IsolaattiTheme {
                Scaffold() {
                    EditorComponent(Modifier.padding(it))
                }
            }
        }
    }

}