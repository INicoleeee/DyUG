package com.example.dydemo.domain.model

sealed class Message(
    open val id: Long,
    open val senderId: Int,
    open val timestamp: Long,
    open val isRead: Boolean
) {
    data class Text(
        override val id: Long,
        override val senderId: Int,
        override val timestamp: Long,
        override val isRead: Boolean,
        val content: String
    ) : Message(id, senderId, timestamp, isRead)

    data class Image(
        override val id: Long,
        override val senderId: Int,
        override val timestamp: Long,
        override val isRead: Boolean,
        val imageUrl: String
    ) : Message(id, senderId, timestamp, isRead)

    data class Card(
        override val id: Long,
        override val senderId: Int,
        override val timestamp: Long,
        override val isRead: Boolean,
        val text: String,
        val buttonText: String,
        val interactionState: CardInteractionState
    ) : Message(id, senderId, timestamp, isRead)
}

enum class CardInteractionState {
    NONE, CONFIRMED, CANCELLED
}
