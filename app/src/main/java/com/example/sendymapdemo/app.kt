package com.example.sendymapdemo

import android.app.Application
import android.content.Context
import com.example.sendymapdemo.dataClass.nMapModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application(){
    companion object{
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(nMapModule)
        }
    }

    fun Context() : Context = applicationContext
}