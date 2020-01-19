package com.example.sendymapdemo.model.retrofit

import com.example.sendymapdemo.dataClass.PathData
import com.example.sendymapdemo.dataClass.UserData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("/map-direction/v1/driving")
    fun requestPath(
            @Query("start") startPosition: String,
            @Query("goal") goalPosition: String,
            @Query("waypoints") wayPointPosition: String,
            @Query("options") options: String
    ): Call<PathData>

    @POST("httpUpdateCredit.php")
    fun updateCredit(
            @Query("user") userID: String,
            @Query("reward") reward: Double
    )

    @GET("login.php")
    fun login(
            @Query("user") userID: String
    ): Call<UserData>
}