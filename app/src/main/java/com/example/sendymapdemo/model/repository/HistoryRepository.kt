package com.example.sendymapdemo.model.repository

import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.ui.adapters.HistoryListAdapter

class HistoryRepository (private val retrofitInterface: RetrofitServerInterface) {
    lateinit var requestResult: List<HistoryData>

    fun getHistory(userID: String): List<HistoryData> {
        try{
            val requestHistory = retrofitInterface.getHistory(userID)
            requestResult = requestHistory.execute().body()!!
            Log.e("request", "${requestResult.size}")
        }catch (e: Exception){
            e.printStackTrace()
        }
        return requestResult
    }

    fun insertHistory(userID: String, time: String, source: String, destination: String, distance: String, reward: String, htime: String, hdate: String) {
        val resultRequest = retrofitInterface.insertHistory(userID, time, source, destination, distance, reward, htime, hdate)
        val thread = Thread(Runnable {
            resultRequest.execute()
        })
        thread.start()
    }
}