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
            var userIds = repository.getAllUserIds()
            while (userIds.isEmpty()) {
                delay(1000) 
                userIds = repository.getAllUserIds()
            }

            while (isActive) {
                delay(10000) 
                dispatchNewMessage(userIds)
            }
        }
    }

    fun stop() {
        dispatchJob?.cancel()
    }

    private suspend fun dispatchNewMessage(userIds: List<Int>) {
        val randomUserId = userIds.random()
        val timestamp = System.currentTimeMillis()
        
        // 随机选择一个消息类型 (0=TEXT, 1=IMAGE, 2=CARD)
        val messageType = Random.nextInt(0, 3) 

        val newMessage = when (messageType) {
            // Case 0: Image Message
            0 -> MessageEntity(
                senderId = randomUserId,
                timestamp = timestamp,
                messageType = "IMAGE",
                imageUrl = "https://picsum.photos/id/${Random.nextInt(200, 300)}/600/800.webp",
                isRead = false
            )
            // Case 1: Card Message
            1 -> MessageEntity(
                senderId = randomUserId,
                timestamp = timestamp,
                messageType = "CARD",
                cardText = "您有一份新的惊喜盲盒！",
                cardButtonText = "立即开启",
                isRead = false
            )
            // Case 2 (default): Text Message
            else -> MessageEntity(
                senderId = randomUserId,
                timestamp = timestamp,
                messageType = "TEXT",
                textContent = "这是一条发给用户 ${randomUserId} 的新消息 ${Random.nextInt(100, 999)}",
                isRead = false
            )
        }
        
        repository.insertNewMessage(newMessage)
    }
}
