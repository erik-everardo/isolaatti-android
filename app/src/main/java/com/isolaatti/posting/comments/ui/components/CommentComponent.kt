package com.isolaatti.posting.comments.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.utils.UrlGen
import java.time.ZonedDateTime

@Composable
fun CommentComponent(comment: Comment, modifier: Modifier = Modifier) {
    val roundedCornerShape = if(comment.isFromUser) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 0.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        )
    }

    val alignment = if(comment.isFromUser) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }

    Box(modifier = modifier, contentAlignment = alignment){
        Card(modifier = Modifier
            .fillMaxWidth(0.7f),
            shape = roundedCornerShape) {
            Column() {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = UrlGen.userProfileImage(comment.userId),
                        contentDescription = null,

                        modifier = Modifier.size(20.dp).clip(RoundedCornerShape(50)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(comment.username, fontWeight = FontWeight.Bold)
                }
                Text(comment.textContent, modifier = Modifier.padding(8.dp))
            }
        }
    }


}

@Composable
@Preview
fun CommentComponentPreview() {
    CommentComponent(
        comment = Comment(
            id = 1,
            textContent = "Test comment",
            userId = 1,
            date = ZonedDateTime.now(),
            postId = 2L,
            username = "Erik",
            isFromUser = false
        ),
        modifier = Modifier.fillMaxWidth()
    )
}