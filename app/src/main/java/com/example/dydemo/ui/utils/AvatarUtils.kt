package com.example.dydemo.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

// A predefined list of pleasant-looking, Material-style colors for avatars.
private val avatarColors = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0),
    Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3),
    Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
    Color(0xFF4CAF50), Color.Gray, Color(0xFFCDDC39),
    Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
    Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B)
)

/**
 * Remembers a stable color for a user's avatar placeholder based on their ID.
 * This ensures the color is consistent across recompositions and for the same user.
 */
@Composable
fun rememberUserAvatarColor(userId: Int): Color {
    return remember(userId) {
        // Use the user's ID to deterministically "randomize" the color index.
        val randomIndex = Random(userId).nextInt(avatarColors.size)
        avatarColors[randomIndex]
    }
}
