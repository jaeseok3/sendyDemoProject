package com.example.sendymapdemo.model.retrofit

import com.example.sendymapdemo.dataclass.PathData
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RetrofitNaverInterface {
    @GET("/map-direction/v1/driving")
    fun requestPath(
            @Query("start") startPosition: String,
            @Query("goal") goalPosition: String,
            @Query("waypoints") wayPointPosition: String,
            @Query("options") options: String,
            @Header("X-NCP-APIGW-API-KEY-ID") apiID: String,
            @Header("X-NCP-APIGW-API-KEY") apiSecret: String
    ): Call<PathData>
}