package com.example.sendymapdemo.koinModule

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

//로그인 했을때 회원정보를 koin으로?
class ApplicationMain : Application(){
    companion object{
        lateinit var instance: ApplicationMain private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }

    fun Context() : Context = applicationContext
}