package com.example.sendymapdemo.model.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.sendymapdemo.dataClass.AllUserData
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository(application: Application, private val retrofitInterface: RetrofitInterface) {
    private val userDatabase = UserRoomDataBase.getInstance(application)!!
    private val userDao = userDatabase.userDao()
    lateinit var userID: String

    fun updateRoom(userData: UserData): Observable<Unit> {
        return Observable.fromCallable { userDao.update(userData) }
    }

    fun insertRoom(userData: UserData): Observable<Unit> {
        return Observable.fromCallable { userDao.insert(userData) }
    }

    fun getFromRoom(userID: String): UserData {
        return userDao.getData(userID)
    }

    fun updateCredit(userID: String, credit: Double){
        retrofitInterface.updateCredit(userID, credit)
        userDao.updateCredit(userID, credit)
    }

    fun getAllUsers(): AllUserData {
        val requestAllUser = retrofitInterface.httpConnect()

        return requestAllUser.execute().body()!!
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