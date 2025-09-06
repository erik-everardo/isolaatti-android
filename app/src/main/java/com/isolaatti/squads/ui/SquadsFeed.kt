package com.isolaatti.squads.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isolaatti.R
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.squads.ui.components.SquadItem
import com.isolaatti.squads.ui.components.SquadItemCard

@Composable
fun SquadsFeed() {
    LazyColumn {
        item {
            OutlinedCard(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
                Text(
                    text = stringResource(R.string.discover_squads),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                LazyRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    items(10) {
                        SquadItemCard(modifier = Modifier.padding(horizontal = 8.dp).clickable {  }, SquadDto(
                            id = "1",
                            name = "Test squad",
                            description = "Test description",
                            extendedDescription = "Test extended description",
                            userId = 1,
                            privacy = 1,
                            creationTime = "2023-01-01T00:00:00Z",
                            imageId = "042fe037-cc34-4f31-a3e2-52126bf31250"
                        ))
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SquadsFeedPreview() {
    SquadsFeed()
}