package com.example.sendymapdemo.model.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.dao.UserDao

@Database(entities = [UserData::class], version = 2, exportSchema = false)
abstract class UserRoomDataBase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        private var INSTANCE: UserRoomDataBase ?= null

        fun getInstance(context: Context): UserRoomDataBase? {
            return INSTANCE ?: synchronized(UserRoomDataBase::class) {
                INSTANCE ?: Room.databaseBuilder(context.applicationContext,
                        UserRoomDataBase::class.java, "userdata.db")
                        .fallbackToDestructiveMigration()
                        .build().also { INSTANCE = it }
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}