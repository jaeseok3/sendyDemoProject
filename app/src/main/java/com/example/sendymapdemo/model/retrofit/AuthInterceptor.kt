package com.example.sendymapdemo.model.retrofit

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val chainRequest = chain.request()
        val request = chainRequest.newBuilder().build()
        return chain.proceed(request)
    }
}