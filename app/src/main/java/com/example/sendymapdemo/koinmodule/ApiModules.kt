package com.example.sendymapdemo.koinmodule

import androidx.room.Room
import com.example.sendymapdemo.dataclass.AllUserData
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.*
import com.example.sendymapdemo.model.retrofit.AuthInterceptor
import com.example.sendymapdemo.model.retrofit.RetrofitNaverInterface
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.model.roomdb.UserRoomDataBase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val SERVER_HOST = "http://15.164.103.195"
private val NAVER_HOST = "https://naveropenapi.apigw.ntruss.com"

val networkServerModule = module {
    single { AuthInterceptor() }
    single { provideOkHttpClient(get()) }
    single { provideServerApi(provideServerRetrofit(get())) }
    single { provideNaverApi(provideNaverRetrofit(get())) }
}

val repositoryModule = module {
    single { AllUserData() }
    single { HistoryData() }
    single { UserData() }
    single { LocationRepository(get()) }
    single { PathDataRepository(get()) }
    single { HistoryRepository(get()) }
    single { UserRepository(androidApplication(), get()) }
    single { MapsRepository() }
}

val roomDataBaseModule = module {
    single { Room.databaseBuilder(androidApplication(), UserRoomDataBase::class.java, "userDB").build() }
}

private fun provideNaverRetrofit(okHttpClient: OkHttpClient): Retrofit {
    val retrofit1 = Retrofit.Builder().baseUrl(NAVER_HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    return retrofit1
}

private fun provideServerRetrofit(okHttpClient: OkHttpClient): Retrofit {
    val retrofit2 = Retrofit.Builder().baseUrl(SERVER_HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    return retrofit2
}

private fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
    return OkHttpClient().newBuilder().addInterceptor(authInterceptor).
            addInterceptor(HttpLoggingInterceptor()).build()
}

private fun provideNaverApi(retrofit: Retrofit): RetrofitNaverInterface =
        retrofit.create(RetrofitNaverInterface::class.java)

private fun provideServerApi(retrofit: Retrofit): RetrofitServerInterface =
        retrofit.create(RetrofitServerInterface::class.java)