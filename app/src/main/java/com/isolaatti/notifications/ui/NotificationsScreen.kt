package com.isolaatti.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.notifications.domain.FollowNotification
import com.isolaatti.notifications.domain.GenericNotification
import com.isolaatti.notifications.domain.LikeNotification
import com.isolaatti.notifications.domain.Notification
import com.isolaatti.notifications.presentation.NotificationsViewModel
import com.isolaatti.utils.UrlGen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onNotificationClick: (Notification) -> Unit,
    onDeleteNotification: (Notification) -> Unit,
    onRefresh: () -> Unit
) {
    val notifications by viewModel.notifications.observeAsState(emptyList())
    val isLoading by viewModel.loading.observeAsState(false)
    val hasError by viewModel.error.observeAsState(false)

    var notificationToDelete by remember { mutableStateOf<Notification?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val errorText = stringResource(R.string.error_making_request)

    LaunchedEffect(Unit) {
        onRefresh()
    }

    LaunchedEffect(hasError) {
        if (hasError) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.error.value = false
        }
    }

    if (notificationToDelete != null) {
        AlertDialog(
            onDismissRequest = { notificationToDelete = null },
            title = { Text(stringResource(R.string.delete_notification)) },
            text = { Text(stringResource(R.string.delete_notification_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    notificationToDelete?.let(onDeleteNotification)
                    notificationToDelete = null
                }) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { notificationToDelete = null }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.notifications)) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notifications.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.there_is_no_more_content_to_show))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { onNotificationClick(notification) },
                            onDeleteClick = { notificationToDelete = notification }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(60.dp)) {
                val mainImage = when (notification) {
                    is LikeNotification -> notification.authorId?.let { UrlGen.userProfileImage(it, false) }
                    is FollowNotification -> notification.followerUserId?.let { UrlGen.userProfileImage(it, false) }
                    else -> null
                }

                AsyncImage(
                    model = mainImage ?: R.drawable.baseline_person_24,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.baseline_person_24),
                    error = painterResource(R.drawable.baseline_person_24)
                )

                val secondaryIcon = when (notification) {
                    is LikeNotification -> R.drawable.hands_clapping_solid
                    is FollowNotification -> R.drawable.baseline_star_24
                    else -> null
                }

                if (secondaryIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFFE1BEE7)) // purple_lighter approx
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(secondaryIcon),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                val title = when (notification) {
                    is LikeNotification -> stringResource(R.string.like_notification_title, notification.authorName ?: "")
                    is FollowNotification -> stringResource(R.string.new_follower_notification_title, notification.followerName ?: "")
                    is GenericNotification -> notification.title ?: ""
                    else -> ""
                }
                val message = when (notification) {
                    is LikeNotification -> stringResource(R.string.like_notification_text)
                    is FollowNotification -> stringResource(R.string.new_follower_notification_text)
                    is GenericNotification -> notification.message ?: ""
                    else -> ""
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}
