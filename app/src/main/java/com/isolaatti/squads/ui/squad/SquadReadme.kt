package com.isolaatti.squads.ui.squad

import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.isolaatti.R
import com.isolaatti.markdown.MarkdownComponent
import com.isolaatti.squads.presentation.SquadState
import com.isolaatti.squads.ui.LocalMarkwon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadReadme(
    state: SquadState,
    onRefresh: () -> Unit,

) {

    val markwon = LocalMarkwon.current


    Box(Modifier.fillMaxWidth()) {
        if(state.loadingSquad) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if(state.squad?.extendedDescription.isNullOrBlank()) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.heart_broken_24px), null, modifier = Modifier.size(50.dp))
                Text(
                    text = "Use Edit button to create a new description",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(64.dp)
                )
            }
        }
        PullToRefreshBox(isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {




                state.squad?.extendedDescription?.takeIf { it.isNotBlank() }?.let { extendedDescription ->
                    MarkdownComponent(
                        text = extendedDescription,
                        markwon = markwon,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

        }
    }

}