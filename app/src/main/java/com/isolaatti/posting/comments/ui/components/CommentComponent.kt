package com.isolaatti.posting.comments.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.isolaatti.R
import com.isolaatti.posting.comments.domain.model.Comment
import com.isolaatti.utils.UrlGen
import io.noties.markwon.syntax.SyntaxHighlight
import java.time.ZonedDateTime

@Composable
fun CommentComponent(
    comment: Comment,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onReport: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    highligth: Boolean,
    finishedHighlight: () -> Unit
) {
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

    val arrangement = if(comment.isFromUser) {
        Arrangement.End
    } else {
        Arrangement.Start
    }

    val borderColor by animateColorAsState(
        targetValue = if(highligth) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = repeatable(
            iterations = 2,
            animation = tween(easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        finishedListener = {
            finishedHighlight()
        }
    )


    Row(modifier = modifier, horizontalArrangement = arrangement, verticalAlignment = Alignment.CenterVertically){
        if(comment.isFromUser) {
            Row(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
        Card(modifier = Modifier.weight(1f, true),
            shape = roundedCornerShape,
            border = BorderStroke(width = 3.dp, color = borderColor)){

            Column() {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = UrlGen.userProfileImage(comment.userId),
                        contentDescription = null,

                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                onProfileClick()
                            },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = comment.username,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                onProfileClick()
                            }
                    )
                }
                Text(
                    text = comment.textContent,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable{
                            onProfileClick()
                        }
                )
            }


        }

        if(!comment.isFromUser) {
            Row {
                var expanded by remember { mutableStateOf(false) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }){
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.report)) },
                        onClick = {
                            expanded = false
                            onReport()
                        }
                    )
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, null)
                }
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
            textContent = """
                At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis 
                praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias 
                excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui 
                officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem 
                rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est 
                eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere 
                possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus 
                autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet 
                ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum 
                rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores 
                alias consequatur aut perferendis doloribus asperiores repellat.
            """.trimIndent().replace("\n", " "),
            userId = 1,
            date = ZonedDateTime.now(),
            postId = 2L,
            username = "Erik",
            isFromUser = false
        ),
        modifier = Modifier.fillMaxWidth(),
        highligth = true,
        finishedHighlight = {}
    )
}