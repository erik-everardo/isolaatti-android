package com.isolaatti.squads.ui.squad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.isolaatti.squads.presentation.SquadViewModel
import com.isolaatti.squads.ui.SquadScreenRoute

@Composable
fun SquadHeader(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    name: String,
    description: String
) {
    Box(
        modifier
            .background(Color.Gray)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = imageUrl,
                placeholder = ColorPainter(Color.Gray),
                error = ColorPainter(Color.Gray),
                modifier = Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Color.White), shape = CircleShape)
                    .size(100.dp),
                contentDescription = null,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(text = name, style = MaterialTheme.typography.displayMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }


        }
    }
}

data class SquadTabItem(val title: String, val route: SquadScreenRoute)

// List of tabs
val tabs = listOf(
    SquadTabItem("Home", SquadScreenRoute.Home),
    SquadTabItem("Invitations", SquadScreenRoute.Invitations),
    SquadTabItem("Requests", SquadScreenRoute.Requests),
    SquadTabItem("Members", SquadScreenRoute.Members),
    SquadTabItem("Settings", SquadScreenRoute.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(viewModel: SquadViewModel, squadId: String) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        if (!viewModel.requested) {
            viewModel.getSquad(squadId)
            viewModel.getPosts(squadId)
            viewModel.requested = true
        }
    }
    var headerIsVisible by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                title = {
                    Text(state.squad?.name ?: "")
                },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            )
        }
    ) { padding ->


        // Determine selected tab index based on current route
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        if (state.loadingSquad) {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize()) {
                val selectedTabIndex = tabs.indexOfFirst { tabItem ->
                    currentDestination?.route?.endsWith(
                        tabItem.route::class.simpleName ?: ""
                    ) == true ||
                            (currentDestination?.route == null && tabItem.route is SquadScreenRoute.Home)
                }.coerceAtLeast(0) // Ensure index is not -1

                PrimaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, tabItem ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                // Navigate to the tab's route
                                // Use launchSingleTop to avoid multiple copies of the same destination
                                navController.navigate(tabItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            text = { Text(tabItem.title) }
                        )
                    }
                }

                NavHost(navController, startDestination = SquadScreenRoute.Home) {
                    composable<SquadScreenRoute.Home> {
                        SquadHome(
                            state,
                            onRefresh = {
                                viewModel.getSquad(squadId, true)
                            }
                        )
                    }
                    composable<SquadScreenRoute.Invitations> {}
                    composable<SquadScreenRoute.Requests> {}
                    composable<SquadScreenRoute.Members> {}
                    composable<SquadScreenRoute.Settings> {}
                }
            }


        }

    }
}

