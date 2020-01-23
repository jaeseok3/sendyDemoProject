package com.example.sendymapdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.UserRepository

class RankingViewModel (private val userRepository: UserRepository) : ViewModel() {
    var userList: MutableLiveData<List<UserData>> ?= MutableLiveData()

    fun getAllUserData() {
        Thread(Runnable {
            try {
                val userMutableList = userRepository.getAllUsers()
                userList!!.postValue(userMutableList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }
}