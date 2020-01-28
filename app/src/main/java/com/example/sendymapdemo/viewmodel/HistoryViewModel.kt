package com.example.sendymapdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.model.repository.HistoryRepository
import com.example.sendymapdemo.model.repository.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HistoryViewModel (private val historyRepository: HistoryRepository, userRepository: UserRepository) : ViewModel() {
    var historyList: MutableLiveData<List<HistoryData>>? = MutableLiveData()
    private val userID = userRepository.userID

    fun getHistory(){
        historyRepository.getHistory(userID)!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    historyList!!.postValue(it)
                }
    }
}