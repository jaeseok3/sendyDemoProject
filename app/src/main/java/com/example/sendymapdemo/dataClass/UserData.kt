package com.example.sendymapdemo.dataClass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class AllUserData(
        val result: ArrayList<UserData> ?= null
)

@Entity(tableName = "userdata")
data class UserData(
        @ColumnInfo(name = "car", defaultValue = "default")
        val car: String = "default",
        @ColumnInfo(name = "credit", defaultValue = "default")
        val credit: String = "default",
        @PrimaryKey
        val id: String = "default",
        @ColumnInfo(name = "rank", defaultValue = "default")
        val rank: String = "default",
        @ColumnInfo(name = "property", defaultValue = "default")
        val property: String = "default"
)