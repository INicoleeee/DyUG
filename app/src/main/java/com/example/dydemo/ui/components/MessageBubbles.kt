package com.example.dydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Message

@Composable
fun MessageBubble(message: Message, onCardInteraction: (Long, CardInteractionState) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start // All messages are from the other user
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 64.dp) // Leave space on the right
        ) {
            when (message) {
                is Message.Text -> TextMessageBubble(message)
                is Message.Image -> ImageMessageBubble(message)
                is Message.Card -> CardMessageBubble(message, onCardInteraction)
            }
        }
    }
}

@Composable
private fun TextMessageBubble(message: Message.Text) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ImageMessageBubble(message: Message.Image) {
    AsyncImage(
        model = message.imageUrl,
        contentDescription = "图片消息",
        modifier = Modifier
            .heightIn(max = 300.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
private fun CardMessageBubble(message: Message.Card, onInteraction: (Long, CardInteractionState) -> Unit) {
    var interactionState by remember { mutableStateOf(message.interactionState) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = message.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            when (interactionState) {
                CardInteractionState.NONE -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                interactionState = CardInteractionState.CONFIRMED
                                onInteraction(message.id, CardInteractionState.CONFIRMED)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(message.buttonText)
                        }
                        OutlinedButton(
                            onClick = {
                                interactionState = CardInteractionState.CANCELLED
                                onInteraction(message.id, CardInteractionState.CANCELLED)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                    }
                }
                CardInteractionState.CONFIRMED -> {
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(0.1f), RoundedCornerShape(8.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                        Text("已确认", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                CardInteractionState.CANCELLED -> {
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(0.1f), RoundedCornerShape(8.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                        Text("已取消", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
