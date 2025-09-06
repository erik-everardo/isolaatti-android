package com.isolaatti.squads.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.squads.data.SquadDto
import com.isolaatti.utils.UrlGen

@Composable
fun SquadItem(squad: SquadDto, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().clickable(enabled = true, onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = squad.imageId?.let { UrlGen.imageUrl(it) },
                placeholder = ColorPainter(Color.Gray),
                error = ColorPainter(Color.Gray),
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(64.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
                Text(text = squad.name, modifier = Modifier)
                Text(text = squad.description, modifier = Modifier)
            }
        }

    }
}

@Composable
@Preview(showBackground = true)
fun SquadItemPreview() {
    SquadItem(squad = SquadDto(
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