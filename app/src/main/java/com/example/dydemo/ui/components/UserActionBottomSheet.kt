package com.example.dydemo.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dydemo.domain.model.User

@SuppressLint("LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActionBottomSheet(
    user: User,
    onDismiss: () -> Unit,
    onSetPinned: (Boolean) -> Unit,
    onEditRemark: () -> Unit
) {

    val displayName = remember(user.customRemark, user.nickname) {
        user.customRemark?.takeIf { it.isNotBlank() } ?: user.nickname
    }
    val subtitleText = remember(user.nickname) {
        "抖音号: ${user.nickname}"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(top = 24.dp, end = 24.dp)
                .size(32.dp)
                .align(Alignment.End)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(end=16.dp, start = 16.dp, bottom = 16.dp)) {
            Spacer(Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(user = user, size = 80.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitleText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                ) {
                    PinnedChatItem(initialIsPinned = user.isPinned, onSetPinned = onSetPinned)
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray.copy(alpha = 0.6f))
                )

                ActionItem(
                    icon = Icons.Default.Edit,
                    text = "设置备注",
                    textColor = MaterialTheme.colorScheme.onBackground,
                    iconPositionRight = true,
                    onClick = {
                        onDismiss()
                        onEditRemark()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPositionRight: Boolean = false
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (iconPositionRight) Arrangement.SpaceBetween else Arrangement.Start
    ) {
        if (!iconPositionRight) {
            Icon(imageVector = icon, contentDescription = text, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (iconPositionRight) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(imageVector = icon, contentDescription = text, tint = textColor, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun PinnedChatItem(
    initialIsPinned: Boolean,
    onSetPinned: (Boolean) -> Unit
) {
    // 使用本地状态驱动 UI，保证即时响应
    var isPinned by remember { mutableStateOf(initialIsPinned) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.height(64.dp)) {
            Text(
                text = "置顶聊天",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "置顶后，该聊天会固定在列表顶部",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "信息提示",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Switch(
            checked = isPinned, 
            onCheckedChange = { newStatus ->
                isPinned = newStatus
                onSetPinned(newStatus)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary, // 修复：滑块使用 onPrimary 颜色 (白色)
                checkedTrackColor = MaterialTheme.colorScheme.primary, // 修复：轨道使用 primary 颜色 (粉色)
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
