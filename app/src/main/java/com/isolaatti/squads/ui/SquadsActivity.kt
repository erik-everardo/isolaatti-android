package com.isolaatti.squads.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.isolaatti.common.IsolaattiTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.isolaatti.posting.post_creator.presentation.CreatePostContract
import com.isolaatti.squads.presentation.EditExtendedDescriptionContract
import com.isolaatti.squads.presentation.SquadViewModel
import com.isolaatti.squads.ui.screens.SettingsScreen
import com.isolaatti.squads.ui.squad.SquadScreen
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import javax.inject.Inject

@AndroidEntryPoint
class SquadsActivity : ComponentActivity() {

    private val createSquadPostLauncher = registerForActivityResult(CreatePostContract()) {

    }

    private val editExtendedDescriptionLauncher = registerForActivityResult(EditExtendedDescriptionContract()) {

    }

    @Inject lateinit var markwon: Markwon
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalMarkwon provides markwon) {
                IsolaattiTheme { // Apply your app's theme
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = SquadMainNavigation.SquadsHome, modifier = Modifier.fillMaxSize()) {
                        composable<SquadMainNavigation.SquadsHome> {
                            SquadsScreenWithNavigation(
                                onSquadClick = {
                                    navController.navigate(SquadMainNavigation.Squad(it))
                                }
                            )
                        }
                        composable<SquadMainNavigation.Squad> {
                            val route = it.toRoute<SquadMainNavigation.Squad>()
                            val squadId = route.id
                            val viewModel = hiltViewModel<SquadViewModel>()
                            LaunchedEffect(Unit) {
                                if (!viewModel.requested) {
                                    viewModel.getSquad(squadId)
                                    viewModel.getPosts(squadId)
                                    viewModel.requested = true
                                }
                            }
                            val state by viewModel.state.collectAsState()
                            SquadScreen(
                                state = state,
                                onNavigateToSettingsScreen = {
                                    navController.navigate(SquadMainNavigation.SquadsSettings(route.id))
                                },
                                onRefresh = {
                                    viewModel.getSquad(squadId, refresh = true)
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onClickImage = {

                                },
                                onNavigateToAddPost = {
                                    createSquadPostLauncher.launch(CreatePostContract.Input(squadId))
                                },
                                onNavigateToEditDescription = {
                                    editExtendedDescriptionLauncher.launch(squadId)
                                }
                            )
                        }
                        composable<SquadMainNavigation.SquadsSettings> {
                            val route = it.toRoute<SquadMainNavigation.SquadsSettings>()
                            SettingsScreen(route.id)
                        }
                    }

                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadsScreenWithNavigation(onSquadClick: (String) -> Unit = {}) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Squads") },
                navigationIcon = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
            MySquadsScreen( hiltViewModel(), onSquadClick = onSquadClick )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IsolaattiTheme {
        SquadsScreenWithNavigation()
    }
}