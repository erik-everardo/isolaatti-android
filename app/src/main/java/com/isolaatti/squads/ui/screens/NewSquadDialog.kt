package com.isolaatti.squads.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isolaatti.R
import com.isolaatti.squads.domain.SquadType
import com.isolaatti.squads.presentation.NewSquadAction

data class NewSquadDialogState(
    val creating: Boolean = false,
    val name: String = "",
    val description: String = "",
    val extendedDescription: String = "",
) {
    val isValid: Boolean
        get() = name.isNotBlank() && description.isNotBlank()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSquadDialog(
    modifier: Modifier = Modifier,
    state: NewSquadDialogState = NewSquadDialogState(),
    onAction: (NewSquadAction) -> Unit = {},
) {

    Column(modifier = modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(stringResource(R.string.new_squad), fontSize = 32.sp, modifier = Modifier.padding(16.dp))

        Text(stringResource(R.string.what_is_a_squad), modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.name,
            onValueChange = { onAction(NewSquadAction.NameChanged(it)) },
            placeholder = { Text(stringResource(id = R.string.squad_name_placeholder)) },
            supportingText = {
                Text(stringResource(R.string.required))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.description,
            onValueChange = { onAction(NewSquadAction.DescriptionChanged(it)) },
            minLines = 2,
            maxLines = 3,
            placeholder = { Text(stringResource(id = R.string.squad_description_placeholder)) },
            supportingText = {
                Text(stringResource(R.string.required))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.extendedDescription,
            onValueChange = { onAction(NewSquadAction.ExtendedDescriptionChanged(it)) },
            placeholder = { Text(stringResource(id = R.string.squad_extended_description_placeholder)) },
            minLines = 4, maxLines = 5,
            supportingText = {
                Text(stringResource(R.string.optional))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onAction(NewSquadAction.Created) }, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
            enabled = !state.creating && state.isValid) {
            if(state.creating) {
                CircularProgressIndicator(Modifier.size(20.dp))
            } else {
                Text(stringResource(R.string.create_squad))
            }
        }
    }
}

@Composable
@Preview
fun NewSquadDialogPreview() {
    NewSquadDialog()
}