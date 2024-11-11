package com.isolaatti.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.isolaatti.R

@Composable
fun IsolaattiTheme(content: @Composable () -> Unit) {
    val colorScheme = if(isSystemInDarkTheme()) {
        darkColorScheme(
            primary = colorResource(R.color.purple),
            onSurface = colorResource(R.color.on_surface)
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.purple),
            onSurface = colorResource(R.color.on_surface)
        )
    }
    MaterialTheme(colorScheme = colorScheme) {
        content()
    }
}