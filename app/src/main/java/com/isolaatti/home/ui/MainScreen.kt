package com.isolaatti.home.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.isolaatti.R
import com.isolaatti.auth.ui.AuthDialog
import com.isolaatti.auth.ui.AuthenticationRequiredDialog
import com.isolaatti.auth.ui.LocalAuth
import com.isolaatti.home.presentation.MainViewModel
import com.isolaatti.notifications.presentation.NotificationsViewModel
import com.isolaatti.notifications.ui.NotificationsScreen
import com.isolaatti.search.presentation.SearchViewModel
import com.isolaatti.search.ui.SearchScreen
import com.isolaatti.sign_up.ui.SignUpActivity
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object SearchRoute

@Serializable
object NotificationsRoute

data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: Int)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val topLevelRoutes = listOf(
        TopLevelRoute(stringResource(R.string.home), HomeRoute, R.drawable.baseline_home_24),
        TopLevelRoute(stringResource(R.string.search), SearchRoute, R.drawable.baseline_search_24),
        TopLevelRoute(stringResource(R.string.notifications), NotificationsRoute, R.drawable.baseline_notifications_24)
    )

    val state by viewModel.authViewModelHelper.authDialogsState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val auth = LocalAuth.current

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelRoutes.forEach { topLevelRoute ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(topLevelRoute.icon), contentDescription = topLevelRoute.name) },
                        label = { Text(topLevelRoute.name) },
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                        onClick = {
                            navController.navigate(topLevelRoute.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {

        if(state.showAuthDialog) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()
                        viewModel.authViewModelHelper.onCloseAuth()
                    }

                }
            ) {
                AuthDialog(
                    onContinue = {

                        auth.doAuth()
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            viewModel.authViewModelHelper.onCloseAuth()
                        }
                    },
                    onCreateAccount = {}
                )
            }
        }

        if(state.showAuthRequiredDialog) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()
                        viewModel.authViewModelHelper.closeAuthRequiredDialog()
                    }
                }
            ) {
                AuthenticationRequiredDialog(
                    onContinue = {
                        auth.doAuth()
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            viewModel.authViewModelHelper.closeAuthRequiredDialog()
                        }
                    },
                    onCreateAccount = {
                        SignUpActivity.startActivity(context)
                    }
                )
            }
        }

        NavHost(
            navController = navController,
            startDestination = HomeRoute
        ) {
            composable<HomeRoute> {
                FeedScreen()
            }
            composable<SearchRoute> {
                val searchViewModel = hiltViewModel<SearchViewModel>()
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToHashtags = {

                    },
                    onNavigateToBrowseProfiles = {

                    },
                    onNavigateToProfile = {

                    },
                    onNavigateToPost = {

                    },
                    onNavigateToHashtagPosts = {

                    }
                )


            }
            composable<NotificationsRoute> {
                val notificationsViewModel = hiltViewModel<NotificationsViewModel>()
                NotificationsScreen(
                    viewModel = notificationsViewModel,
                    onNotificationClick = {

                    },
                    onDeleteNotification = {

                    },
                    onRefresh = {

                    }
                )
            }
        }
    }
}
