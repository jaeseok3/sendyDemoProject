package com.example.sendymapdemo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LoginViewModel (private val userRepository: UserRepository): ViewModel() {
    var userData: MutableLiveData<UserData>?= MutableLiveData()
    fun getData(userID: String){
        Log.e("ViewModel", "$userID, ViewModel")
        userRepository.getData(userID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {userData!!.postValue(it)
                            Log.e("userDATA in LoginViewModel", "$it")},
                        {userData!!.postValue(null)
                            Log.e("failed", "failed to fetch")},
                        { Log.e("user data condition", "${userData!!.value}")}
                )
    }
}