package com.example.dydemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room 数据库中的用户实体，对应数据库表结构
 */
@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val nickname: String,
    val avatarUrl: String,
    val customRemark: String?,
    val isPinned: Boolean = false,
    val lastMessageTimestamp: Long? = null
)
