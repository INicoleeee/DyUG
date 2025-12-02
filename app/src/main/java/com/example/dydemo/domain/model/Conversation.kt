package com.example.dydemo.domain.model

/**
 * 会话模型，用于主消息列表
 *
 * @param user 用户信息
 * @param latestMessage 最新一条消息
 * @param unreadCount 未读消息数
 */
data class Conversation(
    val user: User,
    val latestMessage: Message?,
    val unreadCount: Int
)
