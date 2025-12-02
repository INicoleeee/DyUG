package com.example.dydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    onCardActionClick: (messageId: Long) -> Unit // <-- 新增回调
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
        Box {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick(user.id) },
                contentScale = ContentScale.Crop
            )
            if (conversation.unreadCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                    containerColor = DY_PrimaryRed
                ) { 
                    Text(text = conversation.unreadCount.toString(), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = displayName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            val summary = when (latestMessage) {
                is Message.Text -> latestMessage.content
                is Message.Image -> "[图片]"
                is Message.Card -> "[卡片消息] ${latestMessage.text}"
                null -> ""
            }
            Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Spacer(modifier = Modifier.width(8.dp))

        latestMessage?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = TimeUtils.formatMessageTimestamp(it.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                // 将回调传递给 MessageIndicator
                MessageIndicator(message = it, onCardActionClick = { onCardActionClick(it.id) })
            }
        }
    }
}

@Composable
private fun MessageIndicator(message: Message, onCardActionClick: () -> Unit) {
    Box(modifier = Modifier.height(24.dp)) {
        when (message) {
            is Message.Image -> {
                AsyncImage(model = message.imageUrl, contentDescription = "消息缩略图", modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
            }
            is Message.Card -> {
                val (buttonText, isEnabled) = when (message.interactionState) {
                    CardInteractionState.NONE -> message.buttonText to true
                    CardInteractionState.CONFIRMED -> "已确认" to false
                    CardInteractionState.CANCELLED -> "已取消" to false
                }

                Button(
                    onClick = onCardActionClick, // <-- 关联点击事件
                    enabled = isEnabled,      // <-- 控制按钮是否可点击
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(text = buttonText, fontSize = 10.sp) // <-- 显示动态文本
                }
            }
            else -> { /* For Text messages, we leave this space empty */ }
        }
    }
}
