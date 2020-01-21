package com.example.sendymapdemo.model.dao

import androidx.room.*
import com.example.sendymapdemo.dataclass.UserData

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userData: UserData)

    @Query("SELECT * FROM userdata WHERE id = :userID")
    fun getData(userID: String): UserData

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(userData: UserData)

    @Query("UPDATE userdata SET credit = :credit WHERE id = :userID")
    fun updateCredit(userID: String, credit: Double)
}
