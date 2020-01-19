package com.example.sendymapdemo.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.repository.UserRepository
import io.reactivex.disposables.CompositeDisposable

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val disposable: CompositeDisposable = CompositeDisposable()

    private val repository: UserRepository by lazy {
        UserRepository(application)
    }

    fun getUserData(userID: String): LiveData<UserData> {
        return repository.getData(userID)
    }

    fun updateData(userData: UserData) {
        repository.update(userData)
    }

    fun insertData(userData: UserData) {
        repository.insert(userData)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}