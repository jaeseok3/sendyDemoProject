package com.example.sendymapdemo

import com.naver.maps.geometry.LatLng
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {
    @GET("/map-direction/v1/driving")
    fun requestPath(
            @Query("start") startPosition: String,
            @Query("goal") goalPosition: String,
            @Query("option") option: String
    ): Call<PathData>
}