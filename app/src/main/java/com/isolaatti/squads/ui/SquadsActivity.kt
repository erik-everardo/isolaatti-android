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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.isolaatti.common.IsolaattiTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.isolaatti.squads.ui.squad.SquadScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SquadsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
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
                        SquadScreen(hiltViewModel(), route.id)
                    }
                }

            }
        }
    }
}

// Data class to represent a tab, including its type-safe route
data class TabItem(val title: String, val route: SquadsScreenRoute)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadsScreenWithNavigation(onSquadClick: (String) -> Unit = {}) {
    val navController = rememberNavController()

    // List of tabs
    val tabs = listOf(
        TabItem("Home", SquadsScreenRoute.SquadsHome),
        TabItem("Squads", SquadsScreenRoute.MySquads),
        TabItem("Invitations", SquadsScreenRoute.Invitations),
        TabItem("Requests", SquadsScreenRoute.Requests),
    )

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
            // Determine selected tab index based on current route
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Find the index of the tab that matches the current route
            // This is important for keeping the TabRow selection in sync with navigation
            val selectedTabIndex = tabs.indexOfFirst { tabItem ->
                // For type-safe navigation, you might need a more robust way to match
                // the current route string to your type-safe objects if the default
                // string representation from NavHost isn't directly usable.
                // However, with simple routes, this often works:
                currentDestination?.route?.endsWith(tabItem.route::class.simpleName ?: "") == true ||
                        // Fallback for initial state or when the route might be slightly different
                        (currentDestination?.route == null && tabItem.route is SquadsScreenRoute.MySquads)
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

            // Navigation Host for the content of the tabs
            NavHost(
                navController = navController,
                startDestination = SquadsScreenRoute.SquadsHome,
                modifier = Modifier.fillMaxSize()
            ) {
                composable<SquadsScreenRoute.SquadsHome> { SquadsFeed() }
                composable<SquadsScreenRoute.MySquads> {
                    MySquadsScreen( hiltViewModel(), onSquadClick = onSquadClick )
                }
                composable<SquadsScreenRoute.Invitations> { InvitationScreen() }
                composable<SquadsScreenRoute.Requests> { RequestsScreen() }
            }
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