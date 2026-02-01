package com.isolaatti.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.profile.domain.entity.ProfileListItem
import com.isolaatti.search.data.SearchResultDto
import com.isolaatti.search.data.SearchResultType
import com.isolaatti.search.presentation.SearchViewModel
import com.isolaatti.utils.UrlGen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToHashtags: () -> Unit,
    onNavigateToBrowseProfiles: () -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToPost: (Long) -> Unit,
    onNavigateToHashtagPosts: (String) -> Unit
) {
    val searchSuggestions by viewModel.searchSuggestions.observeAsState(emptyList())
    val searchResultsState by viewModel.searchResults.observeAsState()
    val trendingHashtags by viewModel.trendingHashtags.observeAsState()
    val newestUsers by viewModel.newestUsers.observeAsState(emptyList())

    var active by rememberSaveable { mutableStateOf(false) }
    var showingResults by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadTendingHashtags()
        viewModel.loadNewestUsers()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (active) 0.dp else 16.dp)
            ) {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = query,
                    onQueryChange = {
                        query = it
                        viewModel.getSuggestions(it)
                    },
                    onSearch = {
                        viewModel.searchQuery = it
                        viewModel.search()
                        showingResults = true
                        active = false
                    },
                    active = active,
                    onActiveChange = { active = it },
                    placeholder = { Text(stringResource(R.string.searchbar_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (showingResults || active || query.isNotEmpty()) {
                            IconButton(onClick = {
                                if (active) {
                                    if (query.isNotEmpty()) {
                                        query = ""
                                        viewModel.getSuggestions("")
                                    } else {
                                        active = false
                                    }
                                } else {
                                    showingResults = false
                                    query = ""
                                    viewModel.searchQuery = ""
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchSuggestions) { suggestion ->
                            ListItem(
                                headlineContent = { Text(suggestion.query) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.deleteSuggestion(suggestion) }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.clickable {
                                    query = suggestion.query
                                    viewModel.searchQuery = suggestion.query
                                    viewModel.search()
                                    showingResults = true
                                    active = false
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (showingResults) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResultsState?.result ?: emptyList()) { result ->
                        SearchResultItem(result) {
                            when (result.type) {
                                SearchResultType.Profile -> {
                                    result.resourceId.toIntOrNull()?.let(onNavigateToProfile)
                                }
                                SearchResultType.Post -> {
                                    result.resourceId.toLongOrNull()?.let(onNavigateToPost)
                                }
                                SearchResultType.Hashtag -> {
                                    onNavigateToHashtagPosts(result.resourceId)
                                }
                                SearchResultType.Unknown -> {}
                            }
                        }
                    }
                }
            } else {
                DiscoveryContent(
                    trendingHashtags = trendingHashtags?.result ?: emptyList(),
                    newestUsers = newestUsers,
                    onHashtagClick = onNavigateToHashtagPosts,
                    onOpenHashtagsClick = onNavigateToHashtags,
                    onUserClick = onNavigateToProfile,
                    onBrowseProfilesClick = onNavigateToBrowseProfiles
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoveryContent(
    trendingHashtags: List<String>,
    newestUsers: List<ProfileListItem>,
    onHashtagClick: (String) -> Unit,
    onOpenHashtagsClick: () -> Unit,
    onUserClick: (Int) -> Unit,
    onBrowseProfilesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hashtags Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.hashtags),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trendingHashtags.forEach { hashtag ->
                        SuggestionChip(
                            onClick = { onHashtagClick(hashtag) },
                            label = { Text("#$hashtag") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenHashtagsClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = trendingHashtags.isNotEmpty()
                ) {
                    Text(stringResource(R.string.go_to_hashtags))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Newest Users Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.newest_profiles),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(newestUsers) { user ->
                        UserCarouselItem(user = user, onClick = { onUserClick(user.id) })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBrowseProfilesClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.browse_profiles))
                }
            }
        }
    }
}

@Composable
fun UserCarouselItem(user: ProfileListItem, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
    ) {
        Column {
            AsyncImage(
                model = UrlGen.userProfileImage(user.id),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.baseline_search_24)
            )
            Text(
                text = user.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SearchResultItem(item: SearchResultDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val image = when (item.type) {
                SearchResultType.Profile -> {
                    val userId = item.resourceId.toIntOrNull()
                    if (userId != null) UrlGen.userProfileImage(userId) else R.drawable.baseline_search_24
                }
                SearchResultType.Hashtag -> R.drawable.hashtag_solid
                SearchResultType.Post -> R.drawable.baseline_search_24
                else -> R.drawable.baseline_search_24
            }

            AsyncImage(
                model = image,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                placeholder = painterResource(R.drawable.baseline_search_24)
            )

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
