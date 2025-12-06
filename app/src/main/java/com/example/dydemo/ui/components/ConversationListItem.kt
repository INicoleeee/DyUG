package com.example.dydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Conversation
import com.example.dydemo.domain.model.Message
import com.example.dydemo.ui.theme.DY_PrimaryRed
import com.example.dydemo.ui.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListItem(
    conversation: Conversation,
    onItemClick: (Int) -> Unit,
    onAvatarClick: (Int) -> Unit,
    onCardActionClick: (messageId: Long) -> Unit,
    highlightQuery: String? = null
) {
    val user = conversation.user
    val latestMessage = conversation.latestMessage

    val displayName = remember(user.customRemark, user.nickname) {
        user.customRemark?.takeIf { it.isNotBlank() } ?: user.nickname
    }

    val backgroundColor = if (user.isPinned) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onItemClick(user.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.clickable(onClick = { onAvatarClick(user.id) })) {
            UserAvatar(user = user, size = 56.dp)
            if (conversation.unreadCount > 0) {
                Badge(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp), containerColor = DY_PrimaryRed) { 
                    Text(text = conversation.unreadCount.toString(), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            HighlightedText(text = displayName, query = highlightQuery, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            val summary = when (latestMessage) {
                is Message.Text -> latestMessage.content
                is Message.Image -> "[图片]"
                is Message.Card -> "[卡片消息] ${latestMessage.text}"
                null -> ""
            }
            HighlightedText(text = summary, query = highlightQuery, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.width(8.dp))

        latestMessage?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = TimeUtils.formatMessageTimestamp(it.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                MessageIndicator(message = it, onCardActionClick = { onCardActionClick(it.id) })
            }
        }
    }
}

@Composable
private fun HighlightedText(text: String, query: String?, style: TextStyle, color: Color = Color.Unspecified) {
    if (query.isNullOrBlank()) {
        Text(text = text, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis, color = color)
        return
    }

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val regex = query.toRegex(RegexOption.IGNORE_CASE)
        regex.findAll(text).forEach { matchResult ->
            withStyle(style = style.toSpanStyle().copy(color = color)) { append(text.substring(lastIndex, matchResult.range.first)) }
            withStyle(style = style.toSpanStyle().copy(background = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), color=color)) { append(matchResult.value) }
            lastIndex = matchResult.range.last + 1
        }
        if (lastIndex < text.length) {
            withStyle(style = style.toSpanStyle().copy(color = color)) { append(text.substring(lastIndex)) }
        }
    }

    Text(annotatedString, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun MessageIndicator(message: Message, onCardActionClick: () -> Unit) {
    Box(modifier = Modifier.height(24.dp)) {
        when (message) {
            is Message.Image -> {
                // This AsyncImage can remain as it is a small thumbnail
            }
            is Message.Card -> {
                val (buttonText, isEnabled, buttonColor) = when (message.interactionState) {
                    CardInteractionState.NONE -> Triple(message.buttonText, true, MaterialTheme.colorScheme.primary)
                    CardInteractionState.CONFIRMED -> Triple("已确认", false, MaterialTheme.colorScheme.secondaryContainer)
                    CardInteractionState.CANCELLED -> Triple("已取消", false, MaterialTheme.colorScheme.secondaryContainer)
                }
                Button(
                    onClick = onCardActionClick,
                    enabled = isEnabled,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = if (message.interactionState == CardInteractionState.NONE) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(text = buttonText, fontSize = 10.sp)
                }
            }
            else -> {}
        }
    }
}
