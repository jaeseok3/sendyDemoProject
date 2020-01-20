package com.example.sendymapdemo.model.repository

import android.app.Application
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository(private val application: Application, private val retrofitInterface: RetrofitInterface) {

    private val userDatabase = UserRoomDataBase.getInstance(application)!!
    private val userDao = userDatabase.userDao()

    fun updateRoom(userData: UserData): Observable<Unit> {
        return Observable.fromCallable { userDao.update(userData) }
    }

    fun insertRoom(userData: UserData): Observable<Unit> {
        return Observable.fromCallable { userDao.insert(userData) }
    }

    fun updateCredit(userID: String, credit: Double){
        retrofitInterface.updateCredit(userID, credit)
        userDao.updateCredit(userID, credit)
    }
    
    fun getData(userID: String) {
        val requestUserData = retrofitInterface.login(userID)
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
    }
}