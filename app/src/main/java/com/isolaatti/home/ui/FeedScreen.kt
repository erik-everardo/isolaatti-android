package com.isolaatti.home.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Person
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
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.about.AboutActivity
import com.isolaatti.articles.ui.ArticlesActivity
import com.isolaatti.auth.domain.AuthenticationState
import com.isolaatti.auth.ui.AuthDialog
import com.isolaatti.auth.ui.AuthenticationRequiredDialog
import com.isolaatti.auth.ui.LocalAuth
import com.isolaatti.posting.post_creator.presentation.CreatePostContract
import com.isolaatti.posting.posts.presentation.PostListingViewModel
import com.isolaatti.posting.posts.ui.PostListingScreen
import com.isolaatti.posting.posts.ui.PostListingScreenEvent
import com.isolaatti.posting.viewer.ui.PostViewerActivity
import com.isolaatti.profile.domain.entity.UserProfile
import com.isolaatti.profile.ui.ProfileActivity
import com.isolaatti.settings.ui.SettingsActivity
import com.isolaatti.sign_up.ui.SignUpActivity
import com.isolaatti.squads.ui.SquadsActivity
import com.isolaatti.video_streaming.ui.VideoStreamingActivity
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

    val coroutineScope = rememberCoroutineScope()


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
                    authenticationState = auth.authState,
                    onSignIn = {
                        coroutineScope.launch {
                            drawerState.close()
                            auth.showAuthDialog()
                        }
                    },
                    onClickProfile = {
                        ProfileActivity.startActivity(context, it.userId)
                    }
                )
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_people_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.squads)) },
                    selected = false,
                    onClick = {
                        context.startActivity(Intent(context, SquadsActivity::class.java))
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.outline_live_tv_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.lives)) },
                    selected = false,
                    onClick = {
                        context.startActivity(Intent(context, VideoStreamingActivity::class.java))
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_article_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.articles)) },
                    selected = false,
                    onClick = {
                        context.startActivity(Intent(context, ArticlesActivity::class.java))
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_settings_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = false,
                    onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.baseline_info_24), contentDescription = null) },
                    label = { Text(stringResource(R.string.about)) },
                    selected = false,
                    onClick = {
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    }
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
                            if(auth.authState is AuthenticationState.Authenticated) {
                                val image = auth.authState.userInfo?.profileImage
                                if(image != null) {
                                    AsyncImage(image.imageUrl, null)
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Menu"
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Menu"
                                )
                            }

                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                auth.withAuth {
                                    makePostLauncher.launch(CreatePostContract.Input(null, null))
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
                            auth.withAuth {
                                postListingViewModel.unLikePost(event.post.id)
                            }
                        }
                        is PostListingScreenEvent.PostHashtagClicked -> {

                        }
                        is PostListingScreenEvent.PostImageClicked -> {

                        }
                        is PostListingScreenEvent.PostInfoClicked -> {

                        }
                        is PostListingScreenEvent.PostLikeClicked -> {
                            auth.withAuth {
                                postListingViewModel.likePost(event.post.id)
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
private fun DrawerHeader(authenticationState: AuthenticationState<UserProfile>, onSignIn: () -> Unit, onClickProfile: (UserProfile) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF673AB7)) // Replicating @color/purple from XML
            .padding(16.dp)
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if(authenticationState is AuthenticationState.Authenticated) {
                    authenticationState.userInfo?.let {
                        onClickProfile(it)
                    }
                }
            },
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                if(authenticationState is AuthenticationState.Authenticated) {
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
                            text = authenticationState.userInfo?.name ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@${authenticationState.userInfo?.uniqueUsername ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
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
