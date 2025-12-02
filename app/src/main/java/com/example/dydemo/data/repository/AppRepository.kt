package com.example.dydemo.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.dydemo.data.local.database.MessageDao
import com.example.dydemo.data.local.database.UserDao
import com.example.dydemo.data.local.entity.MessageEntity
import com.example.dydemo.data.local.entity.UserEntity
import com.example.dydemo.data.paging.ConversationPagingSource
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
    private val userDao: UserDao,
    private val messageDao: MessageDao,
    @ApplicationContext private val context: Context
) {

    fun getConversations(): Flow<PagingData<Conversation>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            // 修复：每次都创建一个新的 PagingSource 实例
            pagingSourceFactory = { ConversationPagingSource(userDao, messageDao) }
        ).flow
    }

    fun getChatMessages(userId: Int): Flow<List<Message>> {
        return messageDao.getMessagesForUser(userId).map {
            it.map { entity -> entity.toMessage() }
        }
    }

    suspend fun searchConversations(query: String): List<Conversation> {
        val userIdsFromName = userDao.searchUserIdsByNameOrRemark(query)
        val userIdsFromMessage = messageDao.searchUserIdsByMessageContent(query)
        val allUserIds = (userIdsFromName + userIdsFromMessage).distinct()
        val userEntities = userDao.getUsersByIds(allUserIds)

        return userEntities.map { userEntity ->
            val latestMessageEntity = messageDao.getLatestMessageForUser(userEntity.id)
            val unreadCount = messageDao.getUnreadCountForUser(userEntity.id)
            Conversation(
                user = userEntity.toUser(),
                latestMessage = latestMessageEntity?.toMessage(),
                unreadCount = unreadCount
            )
        }
    }

    suspend fun initializeDatabase() {
        if (userDao.countUsers() == 0 && messageDao.countMessages() == 0) {
            withContext(Dispatchers.IO) {
                val jsonString = context.assets.open("messages.json").bufferedReader().use { it.readText() }
                val initialData = Json.decodeFromString<InitialData>(jsonString)
                userDao.insertAll(initialData.users)
                messageDao.insertAll(initialData.messages)
            }
        }
    }

    suspend fun updateRemark(userId: Int, remark: String?) {
        userDao.updateRemark(userId, remark)
    }

    suspend fun setPinnedStatus(userId: Int, isPinned: Boolean) {
        userDao.updatePinnedStatus(userId, isPinned)
    }

    suspend fun updateCardInteraction(messageId: Long, state: CardInteractionState) {
        messageDao.updateCardState(messageId, state.name)
    }
    
    suspend fun markMessagesAsRead(userId: Int) {
        messageDao.markMessagesAsRead(userId)
    }
}
