package com.example.sendymapdemo.model.retrofit

import com.example.sendymapdemo.dataclass.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitServerInterface {
    @GET("httpUpdateCredit.php")
    fun updateCredit(
            @Query("user") userID: String,
            @Query("reward") reward: Double
    ): Call<Int>

    @GET("login2.php")
    fun getUserInfo(
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
    ): Call<Int>

    @GET("httpHistory2.php")
    fun getHistory(
            @Query("user") userID: String
    ): Call<List<HistoryData>>

    @GET("httpLocation2.php")
    fun getLocationDB(): Call<LocationData>

    @GET("httpConnection.php")
    fun httpConnect(): Call<List<UserData>>
}