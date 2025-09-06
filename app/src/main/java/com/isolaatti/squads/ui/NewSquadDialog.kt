package com.isolaatti.squads.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isolaatti.R
import com.isolaatti.squads.domain.SquadType
import com.isolaatti.squads.domain.stringValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSquadDialog(
    modifier: Modifier = Modifier,
    onCreated: (name: String, description: String, extendedDescription: String, type: SquadType) -> Unit = { _, _, _, _ -> },
    creating: Boolean = false
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var extendedDescription by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var privacy by rememberSaveable { mutableStateOf(SquadType.Private) }

    val options = listOf(SquadType.Private, SquadType.Public)

    Column(modifier = modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {
        Text(stringResource(R.string.new_squad), fontSize = 32.sp, modifier = Modifier.padding(16.dp))

        Text(stringResource(R.string.what_is_a_squad), modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(stringResource(id = R.string.squad_name_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            minLines = 2,
            maxLines = 3,
            placeholder = { Text(stringResource(id = R.string.squad_description_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = extendedDescription,
            onValueChange = { extendedDescription = it },
            placeholder = { Text(stringResource(id = R.string.squad_extended_description_placeholder)) },
            minLines = 4, maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = privacy.stringValue(),
                onValueChange = {},
                label = { Text(stringResource(R.string.privacy))},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.stringValue()) },
                        onClick = {
                            privacy = selectionOption
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onCreated(name, description, extendedDescription, privacy) }, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
            enabled = !creating) {
            if(creating) {
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