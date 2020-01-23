package com.example.sendymapdemo.koinmodule

import androidx.room.Room
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.*
import com.example.sendymapdemo.model.retrofit.AuthInterceptor
import com.example.sendymapdemo.model.retrofit.RetrofitApiInterface
import com.example.sendymapdemo.model.retrofit.RetrofitNaverInterface
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.model.roomdb.UserRoomDataBase
import com.example.sendymapdemo.viewmodel.HistoryViewModel
import com.example.sendymapdemo.viewmodel.LoginViewModel
import com.example.sendymapdemo.viewmodel.RankingViewModel
import com.example.sendymapdemo.viewmodel.RequestViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val SERVER_HOST = "http://15.164.103.195"
private val NAVER_HOST = "https://naveropenapi.apigw.ntruss.com"
private val API_HOST = "http://apis.data.go.kr"

val networkServerModule = module {
    single { AuthInterceptor() }
    single { provideOkHttpClient(get()) }
    single { provideServerApi(provideServerRetrofit(get())) }
    single { provideNaverApi(provideNaverRetrofit(get())) }
    single { provideAPI(provideAPIRetrofit(get())) }
}

val repositoryModule = module {
    single { HistoryData() }
    single { UserData() }
    single { RequestRepository(provideServerApi(provideServerRetrofit(get())), provideNaverApi(provideNaverRetrofit(get()))) }
    single { HistoryRepository(get()) }
    single { UserRepository(androidApplication(), get()) }
    single { MapsRepository() }
}

val viewModelModule = module {
    viewModel { RequestViewModel(get(),get()) }
    viewModel { HistoryViewModel(get(),get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RankingViewModel(get()) }
}

val roomDataBaseModule = module {
    single { Room.databaseBuilder(androidApplication(), UserRoomDataBase::class.java, "userDB").build() }
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

private fun provideAPIRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(API_HOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

private fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
    return OkHttpClient().newBuilder().addInterceptor(authInterceptor).
            addInterceptor(HttpLoggingInterceptor()).build()
}

private fun provideAPI(retrofit: Retrofit): RetrofitApiInterface =
        retrofit.create(RetrofitApiInterface::class.java)

private fun provideNaverApi(retrofit: Retrofit): RetrofitNaverInterface =
        retrofit.create(RetrofitNaverInterface::class.java)

private fun provideServerApi(retrofit: Retrofit): RetrofitServerInterface =
        retrofit.create(RetrofitServerInterface::class.java)