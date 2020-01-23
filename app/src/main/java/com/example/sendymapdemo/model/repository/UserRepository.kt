package com.example.sendymapdemo.model.repository

import android.app.Application
import android.util.Log
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.model.roomdb.UserRoomDataBase
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import io.reactivex.Observable

class UserRepository(application: Application, private val retrofitInterface: RetrofitServerInterface) {
    private val userDatabase = UserRoomDataBase.getInstance(application)!!
    private val userDao = userDatabase.userDao()
    lateinit var allUserDataList: List<UserData>
    lateinit var userID: String
    lateinit var boardAdapter: LeaderBoardAdapter

    fun getFromRoom(userID: String): UserData {
        return userDao.getData(userID)
    }

    fun updateCredit(userID: String, credit: Double){
        retrofitInterface.updateCredit(userID, credit)
        userDao.updateCredit(userID, credit)
    }

    fun getAllUsers(): List<UserData> {
        val requestAllUser = retrofitInterface.httpConnect()
        allUserDataList = requestAllUser.execute().body()!!
        return allUserDataList
    }

    fun getData(userID: String) {
        val requestUserData = retrofitInterface.getUserInfo(userID)
        val r = Runnable { val serverUserData = requestUserData.execute().body()
            userDao.insert(serverUserData!!)
            Log.e("서버에서 가져온 값", "$serverUserData")
            this.userID = serverUserData.id
        }
        val thread = Thread(r)
        thread.start()
    }
}