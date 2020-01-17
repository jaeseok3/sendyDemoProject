package com.example.sendymapdemo.dataClass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userdata")
data class UserData(
        @PrimaryKey
        val ID: String,
        @ColumnInfo(name = "rank")
        val rank: Int,
        @ColumnInfo(name = "accumulatedCredit")
        val accumulatedCredit: Int,
        @ColumnInfo(name = "ColumnInfo")
        val credit: Int
)
