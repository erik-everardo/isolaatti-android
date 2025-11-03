package com.isolaatti.squads.ui

import androidx.compose.runtime.staticCompositionLocalOf
import io.noties.markwon.Markwon

val LocalMarkwon = staticCompositionLocalOf<Markwon>{ throw NotImplementedError("Markwon is not provided.") }