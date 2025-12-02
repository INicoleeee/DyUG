package com.example.dydemo.domain.model

/**
 * 用户数据模型 (领域模型)
 */
data class User(
    val id: Int,
    val nickname: String,
    val avatarUrl: String,
    var customRemark: String?,
    val isPinned: Boolean
)
