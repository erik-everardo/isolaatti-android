package com.isolaatti.squads.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.utils.UrlGen

@Composable
fun SquadItemCard(modifier: Modifier = Modifier, squad: SquadDto) {
    Card(modifier = modifier) {
        AsyncImage(
            model = squad.imageId?.let { UrlGen.imageUrl(it) },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(200.dp)
        )
        Text(
            text = squad.name,
            modifier = Modifier.padding(16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

// Preview composable for SquadItemCard
@Composable
@Preview(showBackground = true)
fun SquadItemCardPreview() {
    SquadItemCard(squad = SquadDto(
        id = "1",
        name = "Test squad",
        description = "Test description",
        extendedDescription = "Test extended description",
        userId = 1,
        privacy = 1,
        creationTime = "2023-01-01T00:00:00Z",
        imageId = ""
    ))
}