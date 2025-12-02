package com.example.dydemo.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dydemo.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE senderId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE senderId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageForUser(userId: Int): MessageEntity?

    @Query("SELECT COUNT(id) FROM messages WHERE senderId = :userId AND isRead = 0")
    suspend fun getUnreadCountForUser(userId: Int): Int

    @Query("UPDATE messages SET isRead = 1 WHERE senderId = :userId")
    suspend fun markMessagesAsRead(userId: Int)

    @Query("UPDATE messages SET cardInteractionState = :state WHERE id = :messageId")
    suspend fun updateCardState(messageId: Long, state: String)
    
    @Query("SELECT COUNT(id) FROM messages")
    suspend fun countMessages(): Int

    /**
     * 根据消息内容搜索，返回匹配的发送者ID列表
     */
    @Query("SELECT DISTINCT senderId FROM messages WHERE textContent LIKE '%' || :query || '%' OR cardText LIKE '%' || :query || '%'")
    suspend fun searchUserIdsByMessageContent(query: String): List<Int>
}
