package com.example.dydemo.data.local.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dydemo.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    @Query("SELECT id FROM users")
    suspend fun getAllUserIds(): List<Int>

    @Query("UPDATE users SET customRemark = :remark WHERE id = :userId")
    suspend fun updateRemark(userId: Int, remark: String?)

    @Query("UPDATE users SET isPinned = :isPinned WHERE id = :userId")
    suspend fun updatePinnedStatus(userId: Int, isPinned: Boolean)

    @Query("SELECT * FROM users ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getConversationPagingSource(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM users ORDER BY isPinned DESC, lastMessageTimestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getUsersForPaging(limit: Int, offset: Int): List<UserEntity>
    
    @Query("SELECT COUNT(id) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM users WHERE nickname LIKE '%' || :query || '%' OR customRemark LIKE '%' || :query || '%'")
    suspend fun searchUsersByNameOrRemark(query: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<Int>): List<UserEntity>

    @Query("UPDATE users SET lastMessageTimestamp = :timestamp WHERE id = :userId")
    suspend fun updateLastMessageTimestamp(userId: Int, timestamp: Long)
}
