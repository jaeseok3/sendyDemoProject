package com.example.sendymapdemo.koinModule

import android.app.Application
import androidx.room.Room
import com.example.sendymapdemo.BuildConfig
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.repository.UserRepository
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private val SERVER_HOST = "http://15.164.103.195"
private val NAVER_HOST = "https://naveropenapi.apigw.ntruss.com"

val apiModule = module {
    single {
        Retrofit.Builder()
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(SERVER_HOST)
                .build()
                .create(RetrofitInterface::class.java)
    }
}
val userDataModule = module {
    single { UserData() }
}