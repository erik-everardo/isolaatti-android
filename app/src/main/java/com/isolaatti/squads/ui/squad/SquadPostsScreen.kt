package com.isolaatti.squads.ui.squad

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.FragmentState
import androidx.fragment.compose.rememberFragmentState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isolaatti.posting.posts.components.PostComponent
import com.isolaatti.posting.posts.domain.entity.Post
import com.isolaatti.posting.posts.ui.PostListingFragment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun SquadPostsScreen(squadId: String, addedPosts: Channel<Post>) {
    var fragment: PostListingFragment? by remember { mutableStateOf(null) }

    LaunchedEffect(fragment) {
        if(fragment != null) {
            addedPosts.receiveAsFlow().collect { post ->
                fragment?.addPost(post)
            }
        }
    }

    AndroidFragment<PostListingFragment>(
        arguments = bundleOf(PostListingFragment.ARG_SQUAD_ID to squadId),
        onUpdate = {
            fragment = it

        }
    )
}