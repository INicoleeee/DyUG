package com.example.dydemo.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dydemo.data.local.entity.MessageEntity
import com.example.dydemo.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    
    abstract fun messageDao(): MessageDao
}
