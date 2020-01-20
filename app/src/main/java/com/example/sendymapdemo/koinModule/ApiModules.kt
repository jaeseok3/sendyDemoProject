package com.example.sendymapdemo.koinModule

import androidx.room.Room
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.repository.UserRepository
import com.example.sendymapdemo.model.retrofit.AuthInterceptor
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val SERVER_HOST = "http://15.164.103.195"
private val NAVER_HOST = "https://naveropenapi.apigw.ntruss.com"

val networkModule = module {
    single { AuthInterceptor() }
    single { provideOkHttpClient(get()) }
    single { provideApi(get()) }
    single { provideServerRetrofit(get()) }
    single { provideNaverRetrofit(get()) }
}

val userRepositoryModule = module {
    single { UserRepository(androidApplication(), get()) }
}

val roomDataBaseModule = module {
    single { Room.databaseBuilder(androidApplication(), UserRoomDataBase::class.java, "userDB").build() }
}

val userDataModule = module {
    single { UserData() }
}

private fun provideNaverRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(NAVER_HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

private fun provideServerRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(SERVER_HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

private fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
    return OkHttpClient().newBuilder().addInterceptor(authInterceptor).build()
}

private fun provideApi(retrofit: Retrofit): RetrofitInterface =
        retrofit.create(RetrofitInterface::class.java)