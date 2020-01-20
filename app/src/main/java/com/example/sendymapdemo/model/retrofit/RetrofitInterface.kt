package com.example.sendymapdemo.model.retrofit

import com.example.sendymapdemo.dataClass.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("/map-direction/v1/driving")
    fun requestPath(
            @Query("start") startPosition: String,
            @Query("goal") goalPosition: String,
            @Query("waypoints") wayPointPosition: String,
            @Query("options") options: String,
            @Header("X-NCP-APIGW-API-KEY-ID") apiID: String,
            @Header("X-NCP-APIGW-API-KEY") apiSecret: String
    ): Call<PathData>

    @GET("httpUpdateCredit.php")
    fun updateCredit(
            @Query("user") userID: String,
            @Query("reward") reward: Double
    )

    @GET("login2.php")
    fun login(
            @Query("user") userID: String
    ): Call<UserData>

    @GET("httpHistoryInsert.php")
    fun insertHistory(
            @Query("user") userID: String,
            @Query("time") time: String,
            @Query("src") source: String,
            @Query("dest") destination: String,
            @Query("distance") distance: String,
            @Query("reward") reward: String,
            @Query("htime") htime: String,
            @Query("hdate") hdate: String
    )

    @GET("httpHistory.php")
    fun getHistory(
            @Query("user") userID: String
    ): Call<HistoryDataList>

    @GET("httpLocation2.php")
    fun getLocationDB(): Call<LocationData>

    @GET("httpConnection.php")
    fun httpConnect(): Call<AllUserData>

}