package com.example.sendymapdemo.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sendymapdemo.dataClass.UserData

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userData: UserData)

    @Query("SELECT * FROM userdata WHERE id = :userID")
    fun getData(userID: String): LiveData<UserData>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(userData: UserData)
}
