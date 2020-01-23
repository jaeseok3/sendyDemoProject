package com.example.sendymapdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.model.repository.HistoryRepository
import com.example.sendymapdemo.model.repository.UserRepository

class HistoryViewModel (private val historyRepository: HistoryRepository, userRepository: UserRepository) : ViewModel() {
    var historyList: MutableLiveData<List<HistoryData>>? = MutableLiveData()
    private val userID = userRepository.userID

    fun getHistory(){
        val getHistoryThread = Thread( Runnable {
            try{
                val historyMutableList = historyRepository.getHistory(userID)
                historyList!!.postValue(historyMutableList)
            }catch (e: Exception){
                e.printStackTrace()
            }
        })
        getHistoryThread.start()
    }
}