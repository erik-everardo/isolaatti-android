package com.isolaatti.squads.ui.squad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.common.dialogs.BottomSheetOption
import com.isolaatti.common.dialogs.OptionsBottomSheet
import com.isolaatti.common.dialogs.rememberOptionsBottomSheetState
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.profile.ui.components.ConfirmChangeProfilePictureBottomSheet
import com.isolaatti.squads.SquadAction
import com.isolaatti.squads.domain.Squad
import com.isolaatti.squads.presentation.SquadState
import com.isolaatti.squads.presentation.SquadTabs
import com.isolaatti.squads.ui.LocalMarkwon
import com.isolaatti.squads.ui.SquadScreenRoute
import io.noties.markwon.Markwon
import kotlinx.coroutines.channels.Channel

data class SquadTabItem(val title: String, val route: SquadScreenRoute)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(
    state: SquadState,
    addedPosts: Channel<Post>,
    onAction: (SquadAction) -> Unit = {},
    onNavigateToSettingsScreen: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditDescription: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {

    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = state.squad?.image?.thumbnailUri,
                            placeholder = ColorPainter(Color.Gray),
                            fallback = painterResource(R.drawable.baseline_people_24),
                            error = painterResource(R.drawable.baseline_people_24),
                            modifier = Modifier
                                .padding(16.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(2.dp, Color.White), shape = CircleShape)
                                .size(50.dp)
                                .clickable {
                                    onAction(SquadAction.ClickOnSquadImage)
                                },
                            contentDescription = null,
                        )
                        Column(
                            modifier = Modifier
                                .clickable { onNavigateToSettingsScreen() }
                        ) {
                            Text(state.squad?.name ?: "", modifier = Modifier.basicMarquee())
                            state.squad?.description?.let {
                                Text(it, style = MaterialTheme.typography.titleSmall, modifier = Modifier.basicMarquee())
                            }

                        }
                    }

                },
                actions = {
                }
            )
        },
        floatingActionButton = {
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            val currentDestination = currentBackStackEntry?.destination

            if(currentDestination?.route?.endsWith(SquadScreenRoute.Readme::class.simpleName ?: "", true) == true) {
                ExtendedFloatingActionButton(onClick = onNavigateToEditDescription) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(text = "Edit")
                }
            } else if(currentDestination?.route?.endsWith(SquadScreenRoute.Posts::class.simpleName ?: "", true) == true) {
                ExtendedFloatingActionButton(onClick = {onAction(SquadAction.NewPost)}) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(text = "Add")
                }
            }


        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->

        val tabs = state.tabs.map {
            when(it) {
                SquadTabs.Description -> SquadTabItem("Description", SquadScreenRoute.Readme)
                SquadTabs.Posts -> SquadTabItem("Posts", SquadScreenRoute.Posts)
                SquadTabs.Members -> SquadTabItem("Members", SquadScreenRoute.Members)
            }
        }

        if(state.showImageDialog) {
            val optionsModalState = rememberOptionsBottomSheetState(
                title = "Squad image",
                options = buildList {
                    add(
                        // index 0
                        BottomSheetOption(
                            title = stringResource(R.string.change_photo),
                            icon = null,
                            popUpOptions = listOf(
                                BottomSheetOption.PopUpOption(stringResource(R.string.upload_photo)), // index 0
                                BottomSheetOption.PopUpOption(stringResource(R.string.take_a_photo)) // index 1
                            )
                        )
                    )

                    if(state.squad?.image != null) {
                        add(
                            // index 1
                            BottomSheetOption(
                                title = stringResource(R.string.view_photo),
                                icon = null,
                            )
                        )
                    }
                }
            )
            OptionsBottomSheet(
                onDismissRequest = {
                    onAction(SquadAction.DismissPictureDialog)
                },
                sheetState = rememberModalBottomSheetState(),
                state = optionsModalState,
                onAction = {
                    optionIndex, popUpOptionIndex ->
                    when(optionIndex) {
                        0 -> when(popUpOptionIndex) {
                            0 -> { onAction(SquadAction.UploadSquadPictureFromGallery) }
                            1 -> { onAction(SquadAction.TakeSquadPictureFromCamera) }
                        }
                        1 -> {
                            onAction(SquadAction.DismissPictureDialog)
                            state.squad?.image?.let {
                                onAction(SquadAction.OpenPictures(it, listOf(it)))
                            }
                        }
                    }
                })
        }

        if(state.showConfirmImageChange) {
            ConfirmChangeProfilePictureBottomSheet(
                onDismiss = {
                    onAction(SquadAction.DismissConfirmChangePictureDialog)
                },
                oldImage = state.squad?.image?.defaultUri,
                newImage = state.squadPictureToUpload?.defaultUri,
                onAccept = {
                    onAction(SquadAction.AcceptChangePictureDialog)
                },
                loading = state.uploadingSquadPicture
            )
        }



        // Determine selected tab index based on current route
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        if (state.loadingSquad) {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()) {
                if(state.tabs.isNotEmpty()) {
                    val selectedTabIndex = tabs.indexOfFirst { tabItem ->
                        currentDestination?.route?.endsWith(
                            tabItem.route::class.simpleName ?: ""
                        ) == true ||
                                (currentDestination?.route == null && tabItem.route is SquadScreenRoute.Readme)
                    }.coerceAtLeast(0) // Ensure index is not -1

                    PrimaryScrollableTabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
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
                }


                NavHost(navController, startDestination = SquadScreenRoute.Readme) {
                    composable<SquadScreenRoute.Readme> {
                        SquadReadme(
                            state,
                            onRefresh = {
                                onAction(SquadAction.RefreshSquad)
                            }
                        )
                    }
                    composable<SquadScreenRoute.Members> {
                        SquadMembers()
                    }
                    composable<SquadScreenRoute.Posts> {


                        SquadPostsScreen(
                            squadId = state.squad?.id ?: "",
                            addedPosts = addedPosts
                        )
                    }
                }
            }


        }

    }
}

@Preview
@Composable
private fun SquadScreenPreview() {

    val extendedDescriptionText = """
                        # Ejemplo de Texto en Markdown

                        El lenguaje Markdown permite dar formato a textos de manera sencilla y legible, sin necesidad de usar un editor visual. Con unos pocos símbolos puedes crear negritas, cursivas, listas, enlaces o encabezados como este. Es especialmente útil para documentación, archivos README o notas rápidas en proyectos de software.

                        A continuación se muestra una pequeña lista con ejemplos comunes:

                        Negritas: **texto**

                        Cursivas: _texto_

                        Enlaces

                        El amanecer se filtraba entre las hojas de los árboles, trayendo consigo un aire fresco y una sensación de calma. Las calles todavía estaban vacías, y el silencio solo era interrumpido por el canto de algún ave tempranera. Era el tipo de momento en el que el tiempo parecía detenerse por un instante, permitiendo apreciar los pequeños detalles que normalmente pasan desapercibidos.

                        Con el paso de las horas, la ciudad comenzaba a cobrar vida. Las voces, los pasos y el movimiento se entrelazaban en una rutina familiar. Aun así, entre el bullicio y la prisa, siempre quedaba espacio para una pausa breve, un respiro que recordaba la importancia de observar, escuchar y simplemente estar presente.
                    """.trimIndent()

    CompositionLocalProvider(LocalMarkwon provides Markwon.builder(LocalContext.current).build()) {
        SquadScreen(
            state = SquadState(
                squad = Squad(
                    id = "squad-1",
                    name = "Squad de prueba",
                    description = "Prueba",
                    extendedDescription = extendedDescriptionText,
                    image = null,
                ),
                showImageDialog = true
            ),
            onNavigateToSettingsScreen = {},
            onNavigateBack = {},
            onNavigateToEditDescription = {},
            onNavigateToSearch = {},
            addedPosts = Channel()
        )
    }

}
