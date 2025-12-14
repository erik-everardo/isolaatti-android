package com.isolaatti.squads.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.isolaatti.squads.presentation.MySquadsScreenEvent
import com.isolaatti.squads.presentation.MySquadsViewModel
import com.isolaatti.squads.ui.components.SquadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySquadsScreen(mySquadsViewModel: MySquadsViewModel, onSquadClick: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mySquadsViewModel.showNewSquadDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        val state by mySquadsViewModel.state.collectAsState()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                withContext(Dispatchers.Main.immediate) {
                    mySquadsViewModel.events.collect { event ->
                        when(event) {
                            is MySquadsScreenEvent.Created -> {
                                sheetState.hide()
                                mySquadsViewModel.hideNewSquadDialog()
                            }
                            is MySquadsScreenEvent.Error -> {
                                snackbarHostState.showSnackbar(event.error)
                            }
                        }
                    }
                }
            }
        }

        if(state.showNewSquadDialog) {
            val newSquadDialogState by mySquadsViewModel.newSquadState.collectAsState()

            ModalBottomSheet(
                onDismissRequest = { mySquadsViewModel.hideNewSquadDialog() },
                sheetState = sheetState
            ) {
                NewSquadDialog(
                    state = newSquadDialogState,
                    onAction = { mySquadsViewModel.onNewSquadAction(it) }
                )
            }
        }


        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxHeight()) {
            PullToRefreshBox(state.refreshing, onRefresh = { mySquadsViewModel.getSquads() }, modifier = Modifier.fillMaxHeight()) {
                LazyColumn(Modifier.fillMaxHeight()) {
                    items(state.squads.size) { index ->
                        SquadItem(
                            squad = state.squads[index],
                            onClick = { onSquadClick(state.squads[index].id) })
                    }

                }
            }

        }

    }
}