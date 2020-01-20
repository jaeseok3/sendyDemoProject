package com.example.sendymapdemo.dataClass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class AllUserData(
        val result: List<UserData>
)

@Entity(tableName = "userdata")
data class UserData(
        @ColumnInfo(name = "Car", defaultValue = "default")
        val Car: String = "default",
        @ColumnInfo(name = "Credit", defaultValue = "default")
        val Credit: String = "default",
        @PrimaryKey
        val ID: String = "default",
        @ColumnInfo(name = "rank", defaultValue = "default")
        val rank: String = "default",
        @ColumnInfo(name = "Property", defaultValue = "default")
        val Property: String = "default"
)