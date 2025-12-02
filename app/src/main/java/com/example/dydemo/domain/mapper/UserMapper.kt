package com.example.dydemo.domain.mapper

import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.domain.model.User

object UserMapper {

    /**
     * 将领域模型 (User) 转换为数据库实体 (UserEntity)。
     */
    fun User.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            nickname = this.nickname,
            avatarUrl = this.avatarUrl,
            customRemark = this.customRemark,
            isPinned = this.isPinned,
            lastMessageTimestamp = null // This will be updated separately
        )
    }

    /**
     * 将数据库实体 (UserEntity) 转换为领域模型 (User)。
     */
    fun UserEntity.toUser(): User {
        return User(
            id = this.id,
            nickname = this.nickname,
            avatarUrl = this.avatarUrl,
            customRemark = this.customRemark,
            isPinned = this.isPinned
        )
    }
}
