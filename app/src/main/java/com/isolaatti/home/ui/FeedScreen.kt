package com.isolaatti.home.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isolaatti.R
import com.isolaatti.auth.ui.AuthDialog
import com.isolaatti.auth.ui.AuthenticationRequiredDialog
import com.isolaatti.auth.ui.LocalAuth
import com.isolaatti.posting.post_creator.presentation.CreatePostContract
import com.isolaatti.posting.posts.presentation.PostListingViewModel
import com.isolaatti.posting.posts.ui.PostListingScreen
import com.isolaatti.posting.posts.ui.PostListingScreenEvent
import com.isolaatti.posting.viewer.ui.PostViewerActivity
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.sign_up.ui.SignUpActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(postListingViewModel: PostListingViewModel = hiltViewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val auth = LocalAuth.current

    BackHandler(drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    var showAuthDialog by remember { mutableStateOf(false) }

    var showAuthRequiredDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    if(showAuthDialog) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    showAuthDialog = false
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
                        showAuthDialog = false
                    }
                },
                onCreateAccount = {}
            )
        }
    }

    if(showAuthRequiredDialog) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    showAuthRequiredDialog = false
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
                        showAuthRequiredDialog = false
                    }
                },
                onCreateAccount = {
                    SignUpActivity.startActivity(context)
                }
            )
        }
    }



    val makePostLauncher = rememberLauncherForActivityResult(CreatePostContract()) {
        if(it != null) {
            Toast.makeText(context, R.string.posted_successfully, Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(
                    isSignedIn = auth.isAuthenticated,
                    onSignIn = {
                        coroutineScope.launch {
                            drawerState.close()
                            showAuthDialog = true
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_people_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.squads)) },
                    selected = false,
                    onClick = { /* Handle squads click */ }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.outline_live_tv_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.lives)) },
                    selected = false,
                    onClick = { /* Handle lives click */ }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_article_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.articles)) },
                    selected = false,
                    onClick = { /* Handle articles click */ }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_settings_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = false,
                    onClick = { /* Handle settings click */ }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_info_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.about)) },
                    selected = false,
                    onClick = { /* Handle about click */ }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = FontFamily(
                                Font(
                                    R.font.zen_dots_regular
                                )
                            ),
                            fontSize = 30.sp,
                            color = colorResource(R.color.purple)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_menu_24),
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if(auth.isAuthenticated) {
                                    makePostLauncher.launch(CreatePostContract.Input(null, null))
                                } else {
                                    showAuthRequiredDialog = true
                                }
                            }
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = "Add Post")
                        }
                    }
                )
            }
        ) { paddingValues ->
            val state by postListingViewModel.postListingScreenState.collectAsState()
            PostListingScreen(
                modifier = Modifier.
                padding(paddingValues)
                    .fillMaxSize(),
                state = state,
                onEvent = { event ->
                    when(event) {
                        is PostListingScreenEvent.PostClicked -> {
                            PostViewerActivity.startActivity(context, event.post.id)
                        }
                        is PostListingScreenEvent.PostComments -> {
                            PostViewerActivity.startActivity(context, event.post.id, true)
                        }
                        is PostListingScreenEvent.PostDislikeClicked -> {
                            if(auth.isAuthenticated) {
                                postListingViewModel.unLikePost(event.post.id)
                            } else {
                                showAuthRequiredDialog = true
                            }
                        }
                        is PostListingScreenEvent.PostHashtagClicked -> {

                        }
                        is PostListingScreenEvent.PostImageClicked -> {

                        }
                        is PostListingScreenEvent.PostInfoClicked -> {

                        }
                        is PostListingScreenEvent.PostLikeClicked -> {
                            if(auth.isAuthenticated) {
                                postListingViewModel.likePost(event.post.id)
                            } else {
                                showAuthRequiredDialog = true
                            }
                        }
                        is PostListingScreenEvent.PostOptionsClicked -> {

                        }
                        is PostListingScreenEvent.PostPauseClicked -> {

                        }
                        is PostListingScreenEvent.PostPlayClicked -> {

                        }
                        is PostListingScreenEvent.PostSeekClicked -> {

                        }
                        is PostListingScreenEvent.PostShareClicked -> {

                        }
                        is PostListingScreenEvent.PostUsernameClicked -> {

                        }
                        PostListingScreenEvent.Refresh ->  {
                            postListingViewModel.getFeed(true)
                        }

                    }
                }
            )
        }
    }
}

@Composable
private fun DrawerHeader(isSignedIn: Boolean = false, userProfile: UserProfile? = null, onSignIn: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF673AB7)) // Replicating @color/purple from XML
            .padding(16.dp)
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                if(isSignedIn) {
                    // Profile Image Placeholder
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Name",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@username",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "example@example.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {

                    FilledTonalButton(
                        onClick = onSignIn,
                        modifier = Modifier
                    ) {
                        Icon(Icons.AutoMirrored.Default.Login, contentDescription = "Sign in")
                        Text(stringResource(R.string.sign_in))
                    }
                }

            }
        }
    }
}
