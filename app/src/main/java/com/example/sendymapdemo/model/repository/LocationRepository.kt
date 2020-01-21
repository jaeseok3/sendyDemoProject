package com.example.sendymapdemo.model.repository

import com.example.sendymapdemo.dataclass.LocationData
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface

class LocationRepository (private val retrofitInterface: RetrofitServerInterface){

    fun getLocationFromDB(): LocationData {
        val requestLocationData = retrofitInterface.getLocationDB()
        return requestLocationData.execute().body()!!
    }
}