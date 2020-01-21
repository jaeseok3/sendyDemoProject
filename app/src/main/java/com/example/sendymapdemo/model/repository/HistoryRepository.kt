package com.example.sendymapdemo.model.repository

import android.util.Log
import com.example.sendymapdemo.dataClass.HistoryData
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.ui.adapters.HistoryListAdapter

class HistoryRepository (private val retrofitInterface: RetrofitInterface) {
    lateinit var historyAdapter: HistoryListAdapter
    lateinit var requestResult: HistoryData

    fun getHistory(userID: String) {
        val r = Runnable {
            val requestHistory = retrofitInterface.getHistory(userID)
            requestResult = requestHistory.execute().body()!!
            Log.e("request", "${requestResult.result!!.size}")
            historyAdapter = HistoryListAdapter(requestResult)
            historyAdapter.notifyDataSetChanged()
        }
        val thread = Thread(r)
        thread.start()
    }

    fun insertHistory(userID: String, time: String, source: String, destination: String, distance: String, reward: String, htime: String, hdate: String) {
        retrofitInterface.insertHistory(userID, time, source, destination, distance, reward, htime, hdate)
    }
}