package com.example.sendymapdemo.model.repository

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.model.roomdb.UserRoomDataBase
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class UserRepository(application: Application, private val retrofitInterface: RetrofitServerInterface) {
    private val userDatabase = UserRoomDataBase.getInstance(application)!!
    private val userDao = userDatabase.userDao()
    lateinit var allUserDataList: List<UserData>
    lateinit var userID: String

    fun getFromRoom(userID: String): Observable<UserData> {
        return userDao.getData(userID)
    }

    fun updateCredit(userID: String, credit: Double){
        Thread(Runnable {
            Log.e("UpdateCredit", "$userID, $credit")
            retrofitInterface.updateCredit(userID, credit).execute()
        }).start()
    }

    fun getAllUsers(): List<UserData> {
        val requestAllUser = retrofitInterface.httpConnect()
        allUserDataList = requestAllUser.execute().body()!!
        return allUserDataList
    }

    fun getData(userID: String): Observable<UserData> {
        return retrofitInterface.getUserInfo(userID)
                .doOnNext {
                    Log.e("유저", "$it")
                    this.userID = it.id
                }
    }
}