package com.example.dydemo.domain.mapper

import com.example.dydemo.data.local.entity.MessageEntity
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Message

object MessageMapper {

    fun MessageEntity.toMessage(): Message {
        return when (this.messageType) {
            "TEXT" -> Message.Text(
                id = this.id,
                senderId = this.senderId,
                timestamp = this.timestamp,
                isRead = this.isRead,
                content = this.textContent ?: ""
            )
            "IMAGE" -> Message.Image(
                id = this.id,
                senderId = this.senderId,
                timestamp = this.timestamp,
                isRead = this.isRead,
                imageUrl = this.imageUrl ?: ""
            )
            "CARD" -> Message.Card(
                id = this.id,
                senderId = this.senderId,
                timestamp = this.timestamp,
                isRead = this.isRead,
                text = this.cardText ?: "",
                buttonText = this.cardButtonText ?: "",
                interactionState = CardInteractionState.valueOf(this.cardInteractionState)
            )
            else -> throw IllegalArgumentException("Unknown message type: ${this.messageType}")
        }
    }
}
