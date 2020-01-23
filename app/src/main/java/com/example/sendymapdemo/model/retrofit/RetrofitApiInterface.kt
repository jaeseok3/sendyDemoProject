package com.example.sendymapdemo.model.retrofit

import com.example.sendymapdemo.dataclass.DangerGradeData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitApiInterface {
    @GET("/B552061/roadDgdgrLink/getRestRoadDgdgrLink")
    fun getDangerGrade(
        @Query("serviceKey") serviceKey: String,
        @Query("searchLineString") searchLineString: String,
        @Query("vhctyCd") vehicleType: String,
        @Query("type") type: String,
        @Query("numOfRows") numOfRows: String,
        @Query("pageNo") pageNo: String
    ): Call<DangerGradeData>
}