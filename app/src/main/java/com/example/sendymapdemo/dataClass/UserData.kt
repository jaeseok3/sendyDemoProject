package com.example.sendymapdemo.dataClass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userdata")
data class UserData(
        @ColumnInfo(name = "Car")
        val Car: String,
        @ColumnInfo(name = "Credit")
        val Credit: String,
        @PrimaryKey
        val ID: String,
        @ColumnInfo(name = "rank")
        val rank: String,
        @ColumnInfo(name = "Property")
        val Property: String
)