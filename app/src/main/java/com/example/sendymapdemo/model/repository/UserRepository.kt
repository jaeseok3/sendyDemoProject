package com.example.sendymapdemo.model.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.dao.UserDao
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.retrofit.RetrofitServerManager
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository(application: Application) {
    val restClient: RetrofitInterface = RetrofitServerManager.getRetrofitService(RetrofitInterface::class.java)

    private val userDao: UserDao by lazy {
        val db = UserRoomDataBase.getInstance(application)!!
        db.userDao()
    }

    fun update(userData: UserData): Observable<Unit> {
        return Observable.fromCallable { userDao.update(userData) }
    }

    fun insert(userData: UserData): Observable<Unit> {
        return Observable.fromCallable { userDao.insert(userData) }
    }
    
    fun getData(userID: String): LiveData<UserData> {
        if(userDao.getData(userID) == null){
            val requestUserData = restClient.login(userID)
            requestUserData.enqueue(object : Callback<UserData> {
                override fun onFailure(call: Call<UserData>, t: Throwable) {
                    error(message = t.toString())
                }
                override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                    if(response.isSuccessful){
                        val serverUserData = response.body()
                        userDao.insert(serverUserData!!)
                    }
                }
            })
            return userDao.getData(userID)
        }
        return userDao.getData(userID)
    }
}