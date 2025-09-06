package com.isolaatti.squads.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.isolaatti.R

@Composable
fun SquadType.stringValue(): String {
    return when(this) {
        SquadType.Private -> stringResource(R.string.private_privacy)
        SquadType.Public -> stringResource(R.string.public_privacy)
    }
}

enum class SquadType {
    Private, Public
}