package com.example.sendymapdemo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.model.repository.UserRepository

class LoginViewModel (private val userRepository: UserRepository): ViewModel() {
    fun getData(userID: String){
        userRepository.getData(userID)
    }
}