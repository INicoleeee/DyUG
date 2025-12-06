package com.example.dydemo.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.dydemo.data.local.database.AppDatabase
import com.example.dydemo.data.local.database.MessageDao
import com.example.dydemo.data.local.database.UserDao
import com.example.dydemo.data.local.entity.MessageEntity
import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.domain.mapper.MessageMapper.toMessage
import com.example.dydemo.domain.mapper.UserMapper.toUser
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Conversation
import com.example.dydemo.domain.model.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class InitialData(
    val users: List<UserEntity>,
    val messages: List<MessageEntity>
)

class AppRepository @Inject constructor(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val messageDao: MessageDao,
    @ApplicationContext private val context: Context
) {

    fun getConversations(): Flow<PagingData<Conversation>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { userDao.getConversationPagingSource() }
        ).flow.map { pagingData: PagingData<UserEntity> ->
            pagingData.map { userEntity ->
                val latestMessageEntity = messageDao.getLatestMessageForUser(userEntity.id)
                val unreadCount = messageDao.getUnreadCountForUser(userEntity.id)
                Conversation(
                    user = userEntity.toUser(),
                    latestMessage = latestMessageEntity?.toMessage(),
                    unreadCount = unreadCount
                )
            }
        }
    }

    // 修复：添加缺失的方法
    suspend fun getUserById(userId: Int): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun getAllUserIds(): List<Int> = userDao.getAllUserIds()

    suspend fun insertNewMessage(message: MessageEntity) {
        db.withTransaction {
            messageDao.insert(message)
            userDao.updateLastMessageTimestamp(message.senderId, message.timestamp)
        }
    }

    fun getChatMessages(userId: Int): Flow<List<Message>> {
        return messageDao.getMessagesForUser(userId).map { it.map { entity -> entity.toMessage() } }
    }

    suspend fun searchConversations(query: String): List<Conversation> {
        val messagesByContent = messageDao.searchMessagesByContent(query)
        val userIdsFromMessages = messagesByContent.map { it.senderId }.toSet()

        val conversationsFromMessages = messagesByContent.mapNotNull { message ->
            userDao.getUserById(message.senderId)?.let {
                Conversation(
                    user = it.toUser(),
                    latestMessage = message.toMessage(),
                    unreadCount = 0
                )
            }
        }

        val usersByName = userDao.searchUsersByNameOrRemark(query)
            .filterNot { it.id in userIdsFromMessages }

        val conversationsFromUsers = usersByName.map { userEntity ->
            val latestMessage = messageDao.getLatestMessageForUser(userEntity.id)
            Conversation(
                user = userEntity.toUser(),
                latestMessage = latestMessage?.toMessage(),
                unreadCount = 0
            )
        }
        
        return (conversationsFromMessages + conversationsFromUsers).sortedByDescending {
            it.latestMessage?.timestamp ?: 0
        }
    }

    suspend fun initializeDatabase() {
        if (userDao.countUsers() == 0 && messageDao.countMessages() == 0) {
            withContext(Dispatchers.IO) {
                val jsonString = context.assets.open("messages.json").bufferedReader().use { it.readText() }
                val initialData = Json.decodeFromString<InitialData>(jsonString)
                userDao.insertAll(initialData.users)
                messageDao.insertAll(initialData.messages)

                val allUsers = initialData.users
                allUsers.forEach { user ->
                    messageDao.getLatestMessageForUser(user.id)?.let { latestMessage ->
                        userDao.updateLastMessageTimestamp(user.id, latestMessage.timestamp)
                    }
                }
            }
        }
    }

    suspend fun updateRemark(userId: Int, remark: String?) { userDao.updateRemark(userId, remark) }
    suspend fun setPinnedStatus(userId: Int, isPinned: Boolean) { userDao.updatePinnedStatus(userId, isPinned) }
    suspend fun updateCardInteraction(messageId: Long, state: CardInteractionState) { messageDao.updateCardState(messageId, state.name) }
    suspend fun markMessagesAsRead(userId: Int) { messageDao.markMessagesAsRead(userId) }
}
