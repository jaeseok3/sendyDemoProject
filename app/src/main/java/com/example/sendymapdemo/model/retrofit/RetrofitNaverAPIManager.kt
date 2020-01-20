package com.example.sendymapdemo.model.retrofit

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitNaverAPIManager {
    private val ALL_TIMEOUT = 10L
    private val NAVER_HOST = "https://naveropenapi.apigw.ntruss.com"

    private var okHttpClient: OkHttpClient
    private var retrofit: Retrofit

    init {
        val httpLogging = HttpLoggingInterceptor()
        httpLogging.level = HttpLoggingInterceptor.Level.BASIC

        okHttpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLogging)
            addInterceptor(HeaderSettingInterceptor())
            connectTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
        }.build()

        retrofit = Retrofit.Builder().apply {
            baseUrl(NAVER_HOST)
            client(okHttpClient)
            addConverterFactory(GsonConverterFactory.create())
        }.build()
    }

    private class HeaderSettingInterceptor: Interceptor{
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val chainRequest = chain.request()
            val request = chainRequest.newBuilder().apply {
            }.build()
            return chain.proceed(request)
        }
    }

    internal fun <T> getRetrofitService(restClass: Class<T>): T{
        return retrofit.create(restClass)
    }
}