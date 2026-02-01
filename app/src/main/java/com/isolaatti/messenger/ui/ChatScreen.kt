package com.isolaatti.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

// Data classes for UI state
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "me",
    val recipientName: String = "John Doe",
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = uiState.recipientName, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Online", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write a message...") },
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(message = message)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (message.isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp
            )
            Text(
                text = "12:00 PM", // Mocked time
                color = textColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End),
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val mockMessages = listOf(
        ChatMessage(text = "Hello!", senderId = "other", isFromMe = false, timestamp = 1L),
        ChatMessage(text = "Hi there! How are you?", senderId = "me", isFromMe = true, timestamp = 2L),
        ChatMessage(text = "I'm doing great, thanks for asking. What about you?", senderId = "other", isFromMe = false, timestamp = 3L),
        ChatMessage(text = "Pretty good! Just working on this new chat screen.", senderId = "me", isFromMe = true, timestamp = 4L),
        ChatMessage(text = "That sounds interesting. Show me more!", senderId = "other", isFromMe = false, timestamp = 5L),
        ChatMessage(text = "Sure, here is the messages loading from bottom to top.", senderId = "me", isFromMe = true, timestamp = 6L)
    )
    
    MaterialTheme {
        ChatScreen(
            uiState = ChatUiState(messages = mockMessages),
            onSendMessage = {},
            onBackClick = {}
        )
    }
}
