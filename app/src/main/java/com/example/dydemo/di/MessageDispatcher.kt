package com.example.dydemo.di

import com.example.dydemo.data.repository.AppRepository
import com.example.dydemo.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 模拟的消息分发中心
 * 定期向数据库中插入新的随机消息
 */
@Singleton
class MessageDispatcher @Inject constructor(
    private val repository: AppRepository
) {
    private var dispatchJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        if (dispatchJob?.isActive == true) return

        dispatchJob = scope.launch {
            // 确保在开始分发前，我们有可用的用户ID
            var userIds = repository.getAllUserIds()
            while (userIds.isEmpty()) {
                delay(1000) // 如果数据库还没准备好，稍等一下
                userIds = repository.getAllUserIds()
            }

            while (isActive) {
                delay(10000) // 每 10 秒分发一次
                dispatchNewMessage(userIds)
            }
        }
    }

    fun stop() {
        dispatchJob?.cancel()
    }

    private suspend fun dispatchNewMessage(userIds: List<Int>) {
        // 从真实的用户ID列表中随机选择一个
        val randomUserId = userIds.random()
        
        val newMessage = MessageEntity(
            senderId = randomUserId,
            timestamp = System.currentTimeMillis(),
            messageType = "TEXT",
            textContent = "这是一条来自用户 ${randomUserId} 的新消息 ${Random.nextInt(100, 999)}",
            isRead = false
        )
        
        repository.insertNewMessage(newMessage)
    }
}
