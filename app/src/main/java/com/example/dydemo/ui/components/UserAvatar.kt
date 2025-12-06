package com.example.dydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.dydemo.domain.model.User
import com.example.dydemo.ui.utils.rememberUserAvatarColor

@Composable
fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    size: Dp
) {
    var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    val avatarColor = rememberUserAvatarColor(userId = user.id)

    Box(modifier = modifier.size(size)) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "用户头像",
            modifier = Modifier.clip(CircleShape),
            onState = { state -> imageState = state },
            contentScale = ContentScale.Crop
        )

        // Show placeholder only on loading or error states
        if (imageState is AsyncImagePainter.State.Loading || imageState is AsyncImagePainter.State.Error) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                // 修改：显示昵称的后三个字符
                val placeholderText = user.nickname.takeLast(3)
                Text(
                    text = placeholderText,
                    color = Color.White,
                    fontSize = size.value.sp / 3, // 调整字体大小以适应更多字符
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
