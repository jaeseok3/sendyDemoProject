package com.example.sendymapdemo.model.repository

import android.app.Application
import android.util.Log
import com.example.sendymapdemo.dataClass.AllUserData
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import io.reactivex.Observable

class UserRepository(application: Application, private val retrofitInterface: RetrofitInterface) {
    private val userDatabase = UserRoomDataBase.getInstance(application)!!
    private val userDao = userDatabase.userDao()
    lateinit var allUserData: AllUserData
    lateinit var userID: String
    lateinit var boardAdapter: LeaderBoardAdapter

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

    fun getAllUsers() {
        val requestAllUser = retrofitInterface.httpConnect()
        val r = Runnable {
            allUserData = requestAllUser.execute().body()!!
            //리더보드 어댑터 초기화
            boardAdapter = LeaderBoardAdapter(allUserData)
            //리더보드 레이아웃 매니저
            boardAdapter.notifyDataSetChanged()
        }
        val thread = Thread(r)
        thread.start()
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