package com.example.dydemo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 消息的数据库实体
 * 新增 @Serializable 注解以支持从 JSON 文件初始化
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["senderId"])]
)
@Serializable // <--- 已添加注解
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderId: Int, // Foreign key to UserEntity
    val timestamp: Long,
    val messageType: String, // "TEXT", "IMAGE", "CARD"
    val isRead: Boolean = false,

    // Content for different message types
    val textContent: String? = null,      // For TEXT type
    val imageUrl: String? = null,         // For IMAGE type
    val cardText: String? = null,         // For CARD type
    val cardButtonText: String? = null, // For CARD type
    val cardInteractionState: String = "NONE" // "NONE", "CONFIRMED", "CANCELLED"
)
