package com.isolaatti.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

// Data classes for UI state
data class ConversationListItem(
    val id: String = UUID.randomUUID().toString(),
    val recipientName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

data class ConversationsUiState(
    val conversations: List<ConversationListItem> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    uiState: ConversationsUiState,
    onConversationClick: (ConversationListItem) -> Unit,
    onNewChatClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Messages") },
                actions = {
                    IconButton(onClick = { /* Search functionality mocked */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(uiState.conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationListItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image Placeholder
        Box(modifier = Modifier.size(48.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.recipientName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color.Green)
                        .background(MaterialTheme.colorScheme.surface) // Border effect
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.recipientName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "10:30 AM", // Mocked time
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationsScreenPreview() {
    val mockConversations = listOf(
        ConversationListItem(
            recipientName = "Alice Smith",
            lastMessage = "Hey, are we still meeting today?",
            timestamp = 1L,
            unreadCount = 2,
            isOnline = true
        ),
        ConversationListItem(
            recipientName = "Bob Johnson",
            lastMessage = "The report is ready for review.",
            timestamp = 2L,
            unreadCount = 0,
            isOnline = false
        ),
        ConversationListItem(
            recipientName = "Charlie Brown",
            lastMessage = "Haha, that's hilarious!",
            timestamp = 3L,
            unreadCount = 0,
            isOnline = true
        ),
        ConversationListItem(
            recipientName = "Diana Prince",
            lastMessage = "See you later!",
            timestamp = 4L,
            unreadCount = 5,
            isOnline = false
        )
    )

    MaterialTheme {
        ConversationsScreen(
            uiState = ConversationsUiState(conversations = mockConversations),
            onConversationClick = {},
            onNewChatClick = {}
        )
    }
}
