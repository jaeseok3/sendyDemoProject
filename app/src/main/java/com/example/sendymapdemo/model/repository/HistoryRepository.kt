package com.example.sendymapdemo.model.repository

import android.util.Log
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.ui.adapters.HistoryListAdapter
import io.reactivex.Observable

class HistoryRepository (private val retrofitInterface: RetrofitServerInterface) {

    fun getHistory(userID: String): Observable<List<HistoryData>>? {
        return retrofitInterface.getHistory(userID)
                .doOnNext {
                    Log.e("request", "${it.size}")
                }
    }

    fun insertHistory(userID: String, time: String, source: String, destination: String, distance: String, reward: String, htime: String, hdate: String) {
        val resultRequest = retrofitInterface.insertHistory(userID, time, source, destination, distance, reward, htime, hdate)
        val thread = Thread(Runnable {
            resultRequest.execute()
        })
        thread.start()
    }
}