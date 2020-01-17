package com.example.sendymapdemo.repositories.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sendymapdemo.dataClass.HistoryData
import com.example.sendymapdemo.dataClass.UserData

@Dao
interface UserDao {
    @Query("SELECT * FROM userdata")
    fun findUserInfo(): LiveData<UserData>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateHistory(historyDataList: List<HistoryData>)
}